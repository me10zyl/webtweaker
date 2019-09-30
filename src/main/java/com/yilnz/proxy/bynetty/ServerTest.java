package com.yilnz.proxy.bynetty;

public class ServerTest {
    public static void main(String[] args) {
        int port = 8000;
        final HttpNewServer httpNewServer = new HttpNewServer(port);
        httpNewServer.start();
        httpNewServer.join();
    }
}
