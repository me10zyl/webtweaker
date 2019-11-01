package com.yilnz;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MainController extends AbstractController {
	private Button browse;
	private TextField input;
	private WebView main;
	private Accordion requests;
	@Override
	public void init() {
		this.input = (TextField) find("input");
		this.browse = (Button) find("browse");
		this.main = (WebView) find("main");
		this.requests = (Accordion) find("requests");

		input.setText("http://www.baidu.com");

		final WebEngine engine = main.getEngine();
		this.browse.setOnAction(e->{
			engine.load(this.input.getText());
		});
		engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
			if(newValue == Worker.State.SUCCEEDED){
				//System.out.println(observable.getValue());
			}
		});
		engine.getLoadWorker().exceptionProperty().addListener(e->{
			System.out.println(e);
		});
		this.requests.setPrefWidth(((Pane)this.requests.getParent()).getPrefWidth());

	}

	public void addRequest(String title, String req){
		final FilteredList<TitledPane> filtered = this.requests.getPanes().filtered(e -> e.getText().equals(title));
		if(filtered.size() > 0){
			final ListView<String> content = (ListView<String>) filtered.get(0).getContent();
			content.getItems().add(req);
		}else {
			final ListView<String> stringListView = new ListView<>(FXCollections.observableArrayList(req));
			stringListView.prefHeightProperty().bind(Bindings.size(stringListView.getItems()).multiply(25));
			final TitledPane e = new TitledPane(title, stringListView);
			final Pane pane = (Pane) this.requests.getParent();

			pane.setMinWidth(400);
			this.requests.getPanes().add(e);
		}
	}
}
