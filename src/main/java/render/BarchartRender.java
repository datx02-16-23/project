package render;

import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import javafx.scene.paint.Color;
import render.element.VisualElement;

public class BarchartRender extends AbsRender {

	public BarchartRender(DataStructure struct, double width, double height, double hspace, double vspace) {
		super(struct, width, height, hspace, vspace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getX(Element e) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY(Element e) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;

	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		// TODO Auto-generated method stub
		
	}

}
