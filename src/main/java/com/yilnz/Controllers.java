package com.yilnz;

import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Controllers {

	private static List<Controller> controllerList = new ArrayList<>();
	private static Logger logger = LoggerFactory.getLogger(Controllers.class);

	public static MainController getMainController(){
		return (MainController) controllerList.stream().filter(e -> e.getClass().equals(MainController.class)).collect(Collectors.toList()).get(0);
	}

	public static void doControllers(Parent main, Class<? extends Controller>... controllers){
		for (Class<? extends Controller> controller : controllers) {
			try {
				final AbstractController newInstance = (AbstractController) controller.newInstance();
				newInstance.setParent(main);
				newInstance.init();
				controllerList.add(newInstance);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("controllers error", e);
			}
		}
	}
}
