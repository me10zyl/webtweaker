package com.yilnz.proxy.bynetty;

public class ClientTest {
    public static void main(String[] args) {
        HttpNewClient httpNewClient = new HttpNewClient("127.0.0.1", 8000, null);
        if (true) {
            httpNewClient.write("hello");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
