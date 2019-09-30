package com.yilnz.proxy;




import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpProxyServer {

	private int port;

	public HttpProxyServer(int port) {
		this.port = port;
	}

	private String read(InputStream is) throws IOException {
		byte[] chunk = new byte[4 * 1024];
		int len = -1;
		StringBuilder sb = new StringBuilder();
		while ((len = is.read(chunk)) != -1) {
			final String str = new String(chunk, 0, len);
			sb.append(str);
			if(is.available() < 1){
				break;
			}
		}
		return sb.toString();
	}


	public String getReqType(String sb){
		final Matcher matcher = Pattern.compile("^CONNECT").matcher(sb);
		if(matcher.find()){
			return "https";
		}
		return "http";
	}

	private String forward(Socket inputSocket, Socket outputSocket){
		//System.out.println("forward" + inputSocket + " to " + outputSocket);
		StringBuilder sb = new StringBuilder();
		try{
			byte[] buffer = new byte[1024 * 10];
			int len = 0;
			final InputStream inputStream = inputSocket.getInputStream();
			final OutputStream outputStream = outputSocket.getOutputStream();

			while((len = inputStream.read(buffer)) != -1){
				outputStream.write(buffer, 0, len);
				sb.append(new String(buffer, 0, len));
				/*if(inputStream.available() < 1){
					break;
				}*/
				//System.out.println(new String(buffer, 0, len));
			}
			outputStream.flush();
		} catch(IOException e){
			System.out.println("error: " + inputSocket + " to " + outputSocket);
			e.printStackTrace();
		}finally {
			if(!inputSocket.isInputShutdown()) {
				try {
					inputSocket.shutdownInput();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(!outputSocket.isOutputShutdown()) {
				try {
					outputSocket.shutdownOutput();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	private void write(byte[] forwardBytes, Socket outputSocket) throws IOException {
		final OutputStream out = outputSocket.getOutputStream();
		out.write(forwardBytes);
		out.flush();
	}

	private String getHost(String read){
		Matcher matcher = Pattern.compile("Host: ([^:\\s]+)(:\\d+)?", Pattern.UNIX_LINES).matcher(read);
		matcher.find();
		return matcher.group(1);
	}

    private int getPort(String read){
        final Matcher matcher;
        if(getReqType(read).equals("https")){
            matcher = Pattern.compile("CONNECT (?:.+):(.+) ").matcher(read);
        }else {
            matcher = Pattern.compile("Host: (?:.+):(.+)").matcher(read);
        }
        final boolean b = matcher.find();
        if(b) {
            return Integer.parseInt(matcher.group(1));
        }else{
            return 80;
        }
    }

	public String getHttpVersion(String read){
		final Matcher matcher = Pattern.compile("HTTP/(.+)").matcher(read);
		matcher.find();
		return matcher.group(1);
	}

	public void start(){
		final Thread thread = new Thread(() -> {
			try {
				final ServerSocket ss = new ServerSocket(port);
				Socket s = null;
				while (true) {
					s = ss.accept();
					Socket clientSocket = s;
					new Thread(()->{
						try {
							final InputStream is = clientSocket.getInputStream();
							final String read = read(is);


							final String host = getHost(read);
                            final int port = getPort(read);


							Socket remoteSocket = new Socket(host, port);
                            final Thread remoteToClient = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final String forwardString = forward(remoteSocket, clientSocket);
                                   /* try {
                                        remoteSocket.close();
                                        clientSocket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }*/
                                    System.out.println("[client-in]" + forwardString);

                                }
                            });
                            remoteToClient.setName("remoteToClient");
                            remoteToClient.start();

							//https
							if(port == 443){
								String response1 = "HTTP/" + getHttpVersion(read) + " 200 Connection established\r\n"  +
										"Proxy-agent: Simple/0.1\r\n" +
										"\r\n";
								//Server.Handler.forwardData(response1, clientSocket);
								//final String secondRead = read(is);
								//System.out.println(secondRead);
								write(response1.getBytes(), clientSocket);
								forward(clientSocket, remoteSocket);
							} else { // http
								System.out.println("[client-out]" + read);
								write(read.getBytes(), remoteSocket);
							}


							remoteToClient.join();
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}).start();


				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
