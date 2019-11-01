package com.yilnz;

import com.yilnz.proxy.ProxyServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class WebTweaker extends Application {

	private static final Logger logger = LoggerFactory.getLogger(WebTweaker.class);

	@Override
	public void start(Stage primaryStage) {
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final Parent main = fxmlLoader.load(this.getClass().getResource("/fxml/main.fxml").openStream());
			final Scene scene = new Scene(main);
			primaryStage.setScene(scene);
			primaryStage.show();
			Controllers.doControllers(main, MainController.class);
			ProxyServer.start(57463);
			primaryStage.setOnCloseRequest((e)->{
				ProxyServer.close();
			});
		} catch (IOException e) {
			logger.error("primary error", e);
		}

	}
}
