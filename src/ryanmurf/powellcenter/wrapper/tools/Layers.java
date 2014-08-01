package ryanmurf.powellcenter.wrapper.tools;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Layers extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel display;
	private List<Layer> Layers;
	
	public Layers(List<Layer> layers) {
		super();
		Layers = layers;
		updateDisplay();
		this.getContentPane().add(display);
	}
	
	public void updateDisplay() {
		display = new JPanel();
		
		for(Layer l : Layers) {
			display.add(l.LayerPanel);
		}
	}
}
