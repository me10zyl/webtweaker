package com.yilnz.proxy.bynetty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import sun.nio.cs.US_ASCII;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectHandler extends ChannelInboundHandlerAdapter {
    private final Consumer<String> clientToRemote; // (1)
    private final Consumer<String> remoteToClient;

    private ConcurrentHashMap<Object, Map<String, Object>> cached = new ConcurrentHashMap<>();

    public RedirectHandler(Consumer<String> clientToRemote, Consumer<String> remoteToClient) {
        this.clientToRemote = clientToRemote;
        this.remoteToClient = remoteToClient;
    }

    private Map<String, Object> analyse(String http) {
        Map<String, Object> map = new HashMap<>();
        final Pattern compileHttps = Pattern.compile("CONNECT ([^:\\s]+):(\\d+)?");
        final Matcher httpsMatcher = compileHttps.matcher(http);
        if (httpsMatcher.find()) {
            final String host = httpsMatcher.group(1);
            final String port = httpsMatcher.group(2);
            map.put("host", host);
            map.put("port", port);
            map.put("isHttps", "true");
        }else {
            final Pattern compile = Pattern.compile("Host: ([^:\\s]+)(:(\\d+))?");
            final Matcher matcher = compile.matcher(http);
            if (matcher.find()) {
                map.put("host", matcher.group(1));
                final String group = matcher.group(3);
                if(group != null && !group.equals("")) {
                    map.put("port", group);
                }else{
                    map.put("port", String.valueOf(80));
                }
            }else{
                map.put("isSSL", "true");
                //throw new RuntimeException("analyse error: " +  http);
            }
        }

        Matcher matcher = Pattern.compile("HTTP/(.+)").matcher(http);
        if (matcher.find()) {
            map.put("httpVersion", matcher.group(1));
        }


        return map;
    }

  /*  @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        final ByteBuf in = (ByteBuf) msg;
        String s = in.toString(CharsetUtil.US_ASCII);
        // Discard the received data silently.
        Map<String, Object> analyse = analyse(s);

        if (clientToRemote != null) {
            clientToRemote.accept(s);
        }

        if (analyse.get("isHttps") != null) {

            System.out.print(s);
            String response1 = "HTTP/" + analyse.get("httpVersion") + " 200 Connection established\r\n"  +
                    "Proxy-agent: Simple/0.1\r\n" +
                    "\r\n";
            final ByteBuf buffer = ctx.alloc().buffer(response1.length());
            buffer.writeBytes(response1.getBytes());
            ctx.writeAndFlush(buffer);
            cached.put(ctx.channel(), analyse);
        }else{
            final HttpNewClient client;
            if (analyse.get("isSSL") != null) {
                analyse = cached.get(ctx.channel());
                Map<String, Object> finalAnalyse = analyse;
                client = (HttpNewClient) analyse.computeIfAbsent("client", (String a)->{
                    return new HttpNewClient((String) finalAnalyse.get("host"), Integer.parseInt((String) finalAnalyse.get("port")), (channelHandlerContext, message) ->{
                       ctx.writeAndFlush(message);
                        remoteToClient.accept(message.toString());
                    });
                });

                //cached.remove(ctx.channel());
            }else{
                System.out.print(s);
                client = new HttpNewClient((String) analyse.get("host"), Integer.parseInt(String.valueOf(analyse.get("port"))), (channelHandlerContext, message) ->{
                    ctx.writeAndFlush(message);
                   System.out.println(message);
                    if (remoteToClient != null) {
                        try {
                             remoteToClient.accept(message.toString(CharsetUtil.US_ASCII));
                        }catch (Exception e){
                            remoteToClient.accept(message.toString());
                             //e.printStackTrace();
                        }
                    }
                });
            }
            client.write(in);
        }



        //final Channel channel = ctx.channel();
        //ctx.writeAndFlush(msg);

        //in.release(); // (3)


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        //cause.printStackTrace();
        ctx.close();
    }
}