package com.yilnz.proxy;

import com.yilnz.Controllers;
import com.yilnz.MainController;
import com.yilnz.proxy.bynetty.HttpNewServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import javafx.application.Platform;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class ProxyServer {

	private static HttpNewServer httpNewServer;

	public static void close(){
		httpNewServer.close();
	}

	public static void start(int port){
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(port));
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyPort", String.valueOf(port));
		httpNewServer = new HttpNewServer(port);
		httpNewServer.setClientToRemoteListener(s -> {
			Platform.runLater(() -> {
				Controllers.getMainController().addRequest("请求", s);
			});
		});

		httpNewServer.setRemoteToClientListener(s -> {
			Platform.runLater(() -> {
				Controllers.getMainController().addRequest("响应", s);
			});
		});

		httpNewServer.start();
		//new HttpProxyServer(port).start();
		/*new Thread(()->{
			Proxy myProxy = new Proxy(port);
			myProxy.listen();
		}).start();*/

		//new Server(port).start();
		/*org.littleshoot.proxy.HttpProxyServer server =
				DefaultHttpProxyServer.bootstrap()
						.withPort(port).withFiltersSource(new HttpFiltersSourceAdapter() {
					public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
						return new HttpFiltersAdapter(originalRequest) {
							@Override
							public HttpResponse clientToProxyRequest(HttpObject httpObject) {
								System.out.println("----");
								System.out.println(httpObject);
								return null;
							}

							@Override
							public HttpObject serverToProxyResponse(HttpObject httpObject) {
								System.out.println("----");
								//System.out.println(((DefaultHttpResponse)httpObject));

								System.out.println(httpObject);
								return httpObject;
							}
						};
					}
				}).start();*/


	}
}
