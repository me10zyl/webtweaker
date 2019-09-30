package com.yilnz.proxy.bynetty;

import com.yilnz.proxy.Server;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.function.BiConsumer;

public class HttpNewClient {
    private final String host;
    private int port;
    private Channel channel;
    private NioEventLoopGroup group;
    private BiConsumer<ChannelHandlerContext, ByteBuf> handler;


    public HttpNewClient(String host, int port, BiConsumer<ChannelHandlerContext, ByteBuf> handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
        init();
    }

    private void init(){
        Bootstrap bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                final ByteBuf in = (ByteBuf) msg;
                                handler.accept(ctx, in);
                                //in.release();
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                //group.shutdownGracefully();
                            }
                        });
                    }
                });
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();
            //channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.channel = channel;
    }

    public void write(ByteBuf msg){
        channel.writeAndFlush(msg);
    }

    public void write(String msg){
       channel.writeAndFlush(msg);
    }
}
