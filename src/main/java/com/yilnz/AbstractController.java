package com.yilnz;

import javafx.scene.Node;
import javafx.scene.Parent;

public abstract class AbstractController implements Controller{
	private Parent parent;

	public void setParent(Parent parent) {
		this.parent = parent;
	}

	public Node find(String id){
		return parent.lookup("#" + id);
	}
}
