package ryanmurf.powellcenter.wrapper.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class Layers extends JFrame implements ListListener<Layer>, ActionListener {
	private static final long serialVersionUID = 1L;

	private JPanel display;
	private ListenableArrayList<Layer> Layers;
	private List<JRadioButton> selectedRadioButtons = new ArrayList<JRadioButton>();
	private ButtonGroup selectGroup = new ButtonGroup();
	private Map map;

	public Layers(ListenableArrayList<Layer> layers, Map map) {
		super();
		this.map = map;
		Layers = layers;
		Layers.setListener(this);
		updateDisplay();
		this.getContentPane().add(display);
	}

	public void updateDisplay() {
		display = new JPanel();
		display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));

		for (Layer l : Layers) {
			JPanel sel = new JPanel();
			JRadioButton bSel = new JRadioButton();
			bSel.addActionListener(this);
			bSel.setSelected(l.selected);
			sel.add(bSel);
			selectedRadioButtons.add(bSel);
			sel.add(l.LayerPanel);
			selectGroup.add(bSel);
			display.add(sel);
		}
	}

	@Override
	public void beforeAdd(Layer item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterAdd(Layer item) {
		selectedRadioButtons.clear();
		selectGroup  = new ButtonGroup();
		display.removeAll();

		for (Layer l : Layers) {
			JPanel sel = new JPanel();
			JRadioButton bSel = new JRadioButton();
			bSel.setSelected(l.selected);
			bSel.addActionListener(this);
			sel.add(bSel);
			selectedRadioButtons.add(bSel);
			sel.add(l.LayerPanel);
			selectGroup.add(bSel);
			display.add(sel);
		}
		display.revalidate();
		display.repaint();
		this.pack();
		this.revalidate();
		this.repaint();
	}

	@Override
	public void beforeRemove(Layer item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterRemove() {
		selectedRadioButtons.clear();
		selectGroup  = new ButtonGroup();
		display.removeAll();

		for (Layer l : Layers) {
			JPanel sel = new JPanel();
			JRadioButton bSel = new JRadioButton();
			bSel.setSelected(l.selected);
			bSel.addActionListener(this);
			sel.add(bSel);
			selectedRadioButtons.add(bSel);
			sel.add(l.LayerPanel);
			selectGroup.add(bSel);
			display.add(sel);
		}
		display.revalidate();
		display.repaint();
		this.pack();
		this.revalidate();
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//Object src = e.getSource();
		for(int i=0; i<selectedRadioButtons.size(); i++) {
			Layers.get(i).selected = selectedRadioButtons.get(i).isSelected();
			if(selectedRadioButtons.get(i).isSelected()) {
				map.updateColorsAndNumbers();
				map.updateCanvasScale();
			}
		}
		map.getMainMap().paintImmediately(map.getMainMap().getBounds());
	}
}
