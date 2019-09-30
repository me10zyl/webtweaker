package com.yilnz.proxy.bynetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.function.Consumer;

public class HttpNewServer{
    private int port;
    private Thread thread;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Consumer<String> clientToRemote, remoteToClient;

    public HttpNewServer(int port) {
        this.port = port;
    }

    public void join(){
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setClientToRemoteListener(Consumer<String> consumer){
        clientToRemote = consumer;
    }

    public void setRemoteToClientListener(Consumer<String> consumer){
        remoteToClient = consumer;
    }


    public void start(){
        thread = new Thread(() -> {
            // (1)
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap(); // (2)
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class) // (3)
                        .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new RedirectHandler(clientToRemote, remoteToClient));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                        .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

                // Bind and start to accept incoming connections.
                ChannelFuture f = null; // (7)
                f = b.bind(port).sync();

                // Wait until the server socket is closed.
                // In this example, this does not happen, but you can do that to gracefully
                // shut down your server.
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
              close();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
