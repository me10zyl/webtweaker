package com.yilnz;

import com.yilnz.surfing.core.SurfHttpRequest;
import com.yilnz.surfing.core.basic.Page;
import com.yilnz.surfing.core.client.SurfRawClient;
import com.yilnz.surfing.core.header.generators.ChromeHeaderGenerator;
import com.yilnz.surfing.core.parser.RequestParser;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class APIController implements Initializable {

	@FXML
	private Button send;
	@FXML
	private TextField url;
	@FXML
	private ChoiceBox<String> method;
	@FXML
	private TextArea request;
	@FXML
	private TextArea response;
	@FXML
	private TextField params;

	private StringProperty headers = new SimpleStringProperty();

	@FXML
	private void send(){
		try {
			final SurfHttpRequest request = RequestParser.parse(this.request.getText());
			final Page page = new SurfRawClient().request(request);
			response.setText(page.getRawText());
		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
		}
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		url.setText("https://httpbin.org/get");
		method.setItems(FXCollections.observableArrayList("GET", "POST"));
		method.setValue("GET");
		final ChromeHeaderGenerator chrome = new ChromeHeaderGenerator();
		headers.setValue(chrome.toString());
		params.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

			}
		});
		params.textProperty().bindBidirectional(method.valueProperty(), new StringConverter<String>() {
			@Override
			public String toString(String object) {
				if(object.equals("GET")){
					return "";
				}
				return params.getText();
			}

			@Override
			public String fromString(String string) {
				if(!string.equals("")){
					return "POST";
				}
				return "GET";
			}
		});
		Bindings.bindBidirectional(request.textProperty(), method.valueProperty(), new StringConverter<String>() {
			@Override
			public String toString(String object) {
				return getText();
			}

			@Override
			public String fromString(String string) {
				return  parse(string).getMethod();
			}
		});
		Bindings.bindBidirectional(request.textProperty(), url.textProperty(), new StringConverter<String>() {
			@Override
			public String toString(String object) {
				return getText();
			}

			@Override
			public String fromString(String string) {
				return parse(string).getUrl();
			}
		});
		Bindings.bindBidirectional(request.textProperty(), params.textProperty(), new StringConverter<String>() {
			@Override
			public String toString(String object) {
				return getText();
			}

			@Override
			public String fromString(String string) {
				return parse(string).getBody();
			}
		});

	}

	private SurfHttpRequest parse(String string) {
		final SurfHttpRequest parse = RequestParser.parse(string);
		APIController.this.headers.setValue(parse.getHeadersString());
		return parse;
	}

	private String getText(){
		return method.getValue() + " " + url.getText() + " HTTP/1.1\n" + headers.get() + "\n" + params.getText();
	}
}
