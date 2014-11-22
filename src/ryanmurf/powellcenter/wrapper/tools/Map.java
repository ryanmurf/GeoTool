package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

public class Map extends JXMapKit implements ActionListener, ItemListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	
	final List<Site> sites = new ArrayList<Site>();
	//final LinearGradientPaint2 paint;
	final Integer valueIndex = new Integer(0);
	
	final ListenableArrayList<Layer> layers = new ListenableArrayList<Layer>();
	private CompoundPainter<Painter<JXMapViewer>> cp;
	private List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

	private Canvas canvas;
	
	private final Color[] colors = {new Color(16777215), new Color(12632256), new Color(8421504), new Color(0), new Color(16711680), new Color(8388608), new Color(16776960), new Color(8421376), new Color(65280), new Color(32768), new Color(65535), new Color(32896), new Color(255), new Color(128), new Color(16711935), new Color(8388736)};
	private JComboBox<Color> jcomboBoxHighValue = new JComboBox<Color>(colors);
	private final Color[] colorsmid = {new Color(255,255,255,0), Color.black, Color.blue, Color.cyan, Color.darkGray, Color.gray, Color.green, Color.lightGray, Color.magenta, Color.orange, Color.pink, Color.red, Color.white, Color.yellow};
	private JComboBox<Color> jcomboBoxMidValue = new JComboBox<Color>(colorsmid);
	private JComboBox<Color> jcomboBoxLowValue = new JComboBox<Color>(colors);
	
	private JTextField textFieldHigh = new JTextField(5);
	private JTextField textFieldMid = new JTextField(5);
	private JTextField textFieldLow = new JTextField(5);
	
	private JPanel left;
	private JPanel right;
	
	JSlider valueSlider;
	JCheckBox sameScale;
	
	private final JLabel valueLabel;
	
	class MyCellRenderer extends JLabel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = 1L;

		public MyCellRenderer() {
			setOpaque(true);
		}

		boolean b = false;

		@Override
		public void setBackground(Color bg) {
			if (!b) {
				return;
			}

			super.setBackground(bg);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Object> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			b = true;
			setPreferredSize(new Dimension(25,20));
			Color t = (Color) value;
			if(t.getAlpha() == 0) {
				setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
				setText("none");
				setBackground(new Color(175,175,175,255));
			} else
				setText("");
				setBackground((Color) value);
			b = false;
			return this;
		}
	}
	
	public Map() {
		super();
		
        TileFactoryInfo info = new TileFactoryInfo(0,17,17,
                256, true, true,
                "http://mt0.google.com/vt",
                "x","y","z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = 17-zoom;
                return this.baseURL +"/x="+x+"&y="+y+"&z="+zoom;
            }
        };
        info.setDefaultZoomLevel(14);
        
        TileFactory tf = new DefaultTileFactory(info);
        setTileFactory(tf);
        setCenterPosition(new GeoPosition(41.3167,-105.5833));
		setZoomSliderVisible(true);
		setZoom(14);
		
		cp = new CompoundPainter<Painter<JXMapViewer>>();
		cp.setCacheable(false);
		
		Painter<JXMapViewer> layerNameOverlay = new Painter<JXMapViewer>() {
			
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
				updateCanvasScale();
				for(Layer l : layers) {
					if(l.selected) {
						g.setPaint(new Color(0, 0, 0, 150));
						int width = g.getFontMetrics().stringWidth(l.layerName);
						g.fillRoundRect(10, 10, width+40, 30, 10, 10);
						g.setPaint(Color.WHITE);
						String layerName = l.layerName;
						if(valueSlider.isVisible())
							layerName = layerName.replace("*", String.valueOf(valueSlider.getValue()+1));
						g.drawString(layerName, 10 + 10, 10 + 20);
					}
				}
			}
		};
		
		valueLabel = new JLabel();
		valueLabel.setOpaque(true);
		valueLabel.setBackground(new Color(255,250,250,150));
		valueLabel.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		valueLabel.setText("");
		GridBagConstraints gbc_canvasL = new GridBagConstraints();
		gbc_canvasL.anchor = GridBagConstraints.NORTHEAST;
		gbc_canvasL.gridx = 1;
		gbc_canvasL.gridy = 0;
		this.getMainMap().add(valueLabel, gbc_canvasL);
		
		valueSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 1, 0);
		valueSlider.addChangeListener(this);
		valueSlider.setVisible(false);
		
		sameScale = new JCheckBox("Same Scale");
		sameScale.setSelected(false);
		sameScale.setVisible(false);
		sameScale.addActionListener(this);
		
		JPanel slider = new JPanel();
		slider.setOpaque(false);
		slider.setBackground(new Color(0,0,0,0));
		slider.add(valueSlider);
		slider.add(sameScale);
		
		GridBagConstraints gbc_canvasS = new GridBagConstraints();
		gbc_canvasS.anchor = GridBagConstraints.SOUTHWEST;
		gbc_canvasS.gridx = 1;
		gbc_canvasS.gridy = 0;
		this.getMainMap().add(slider, gbc_canvasS);
	
		JPanel scale = new JPanel();
		scale.setOpaque(false);
		scale.setBackground(new Color(0,0,0,0));
		
		GridBagConstraints gbc_canvas = new GridBagConstraints();
		gbc_canvas.anchor = GridBagConstraints.EAST;
		gbc_canvas.gridx = 1;
		gbc_canvas.gridy = 0;
		
		left = new JPanel();
		left.setOpaque(false);
		left.setBackground(new Color(0,0,0,0));
		left.setLayout(new GridLayout(3, 1, 0, 35));
		left.setSize(75, 150);
		jcomboBoxHighValue.setRenderer(new MyCellRenderer());
		jcomboBoxHighValue.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
		jcomboBoxHighValue.setSelectedIndex(5);
		jcomboBoxHighValue.addItemListener(this);
		left.add(jcomboBoxHighValue);
		jcomboBoxMidValue.setRenderer(new MyCellRenderer());
		jcomboBoxMidValue.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
		jcomboBoxMidValue.addItemListener(this);
		left.add(jcomboBoxMidValue);
		jcomboBoxLowValue.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
		jcomboBoxLowValue.setRenderer(new MyCellRenderer());
		jcomboBoxLowValue.setSelectedIndex(8);
		jcomboBoxLowValue.addItemListener(this);
		left.add(jcomboBoxLowValue);
		scale.add(left);
		
		this.canvas = new Canvas();
		canvas.setSize(15, 150);
		canvas.setBackground(Color.blue);
		scale.add(canvas);
		
		right = new JPanel();
		right.setOpaque(false);
		right.setBackground(new Color(0,0,0,0));
		right.setLayout(new GridLayout(3,1,0,46));
		right.setSize(75,150);
		textFieldHigh.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		textFieldHigh.addActionListener(this);
		right.add(textFieldHigh);
		textFieldMid.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		textFieldMid.addActionListener(this);
		right.add(textFieldMid);
		textFieldLow.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		textFieldLow.addActionListener(this);
		right.add(textFieldLow);
		scale.add(right);
		
		this.getMainMap().add(scale, gbc_canvas);
		
		addLayerPainter(layerNameOverlay);
	}
	
	public void setColorsHidden(boolean v) {
		left.setVisible(v);
	}
	
	public void setColorValuesHidden(boolean v) {
		right.setVisible(v);
	}
	
	public void addLayer(Layer layer) {
		this.getMainMap().addMouseListener(layer.getMouseListeners(this));
		this.getMainMap().addMouseMotionListener(layer.getMouseMotionListener(this, valueLabel));
		this.addLayerPainter(layer.LayerOverlay);
		for(Layer l : layers)
			l.selected = false;
		layer.generateLayerPanel(this);
		layer.selected = true;
		this.layers.add(layer);
		//this.layers.get(layers.size() - 1).setSelected();
		updateCanvasScale();
		updateColorsAndNumbers();
		int values = layer.getNumberOfValues();
		if(values > 1) {
			valueSlider.setVisible(true);
			sameScale.setVisible(true);
			valueSlider.setMinimum(0);
			valueSlider.setMaximum(values-1);
		} else {
			valueSlider.setVisible(false);
			sameScale.setVisible(false);
		}
	}
	
	public void deleteLayer(Layer layer) {
		this.getMainMap().removeMouseListener(layer.getMouseListeners(this));
		this.getMainMap().removeMouseMotionListener(layer.getMouseMotionListener(this, valueLabel));
		this.removeLayerPainter(layer.LayerOverlay);
		int ind = layers.indexOf(layer);
		if(layer.selected)
			if(ind < layers.size() - 1)
				layers.get(ind + 1).selected = true;
			else if(ind > 0)
				layers.get(ind - 1).selected = true;
		this.layers.remove(layer);
		updateCanvasScale();
		updateColorsAndNumbers();
		for(Layer l : layers) {
			if (l.selected) {
				int values = l.getNumberOfValues();
				if (values > 1) {
					valueSlider.setVisible(true);
					valueSlider.setMinimum(0);
					valueSlider.setMaximum(values - 1);
				} else {
					valueSlider.setVisible(false);
				}
			}
		}
	}
	
	private void addLayerPainter(Painter<JXMapViewer> painter) {
		this.painters.add(painter);
		@SuppressWarnings("unchecked")
		Painter<JXMapViewer>[] paints = painters.toArray(new Painter[painters.size()]);
		this.cp.setPainters(paints);
		getMainMap().setOverlayPainter(cp);
	}
	
	private void removeLayerPainter(Painter<JXMapViewer> painter) {
		this.painters.remove(painter);
		@SuppressWarnings("unchecked")
		Painter<JXMapViewer>[] paints = painters.toArray(new Painter[painters.size()]);
		this.cp.setPainters(paints);
		getMainMap().setOverlayPainter(cp);
	}
	public void updateCanvasScale() {
		Graphics g = canvas.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		for(Layer l : layers) {
			if(l.selected) {
				g2.setPaint(l.paint);
				g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				
				if (l.selected) {
					int values = l.getNumberOfValues();
					if (values > 1) {
						valueSlider.setVisible(true);
						sameScale.setVisible(true);
						valueSlider.setMinimum(0);
						valueSlider.setMaximum(values - 1);
					} else {
						valueSlider.setVisible(false);
						sameScale.setVisible(false);
					}
				}
			}
		}
	}
	
	public void updateColorsAndNumbers() {
		Layer selected = getSelectedLayer();
		
		selected.updateColors(null,getSelectedColors());
		
		Color[] colors = selected.paint.getColors();
		jcomboBoxHighValue.setSelectedItem(colors[0]);
		if(colors.length > 2) {
			jcomboBoxMidValue.setSelectedItem(colors[1]);
			jcomboBoxLowValue.setSelectedItem(colors[2]);
		} else {
			jcomboBoxLowValue.setSelectedItem(colors[1]);
		}
		
		textFieldHigh.setText(String.format("%.3f", selected.paint.max));
		if(colors.length > 2) {
			float midfrac = selected.paint.getFractions()[1];
			float p = (midfrac * (selected.paint.max-selected.paint.min)) + selected.paint.min;
			textFieldMid.setEnabled(true);
			textFieldMid.setText(String.format("%.3f", p));
			textFieldLow.setText(String.format("%.3f", selected.paint.min));
		} else {
			textFieldMid.setEnabled(false);
			textFieldLow.setText(String.format("%.3f", selected.paint.min));
		}
	}
	
	public Layer getSelectedLayer() {
		for(Layer l : layers) {
			if(l.selected) {
				return l;
			}
		}
		return null;
	}
	
	public Color[] getSelectedColors() {
		int alpha = ((Color) jcomboBoxMidValue.getSelectedItem()).getAlpha();
		if(alpha != 0)
			return new Color[] {(Color) jcomboBoxHighValue.getSelectedItem(), (Color) jcomboBoxMidValue.getSelectedItem(), (Color) jcomboBoxLowValue.getSelectedItem()};
		else
			return new Color[] {(Color) jcomboBoxHighValue.getSelectedItem(), (Color) jcomboBoxLowValue.getSelectedItem()};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		//if(src instanceof JButton) {
		//	JButton t = (JButton) src;
		//}
		for(Layer l : layers) {
			if(src == l.layerVisible) {
				getMainMap().paintImmediately(getMainMap().getBounds());
			}
		}
		if(src == textFieldHigh) {
			try {
				float high = Float.parseFloat(textFieldHigh.getText());
				getSelectedLayer().paint.max = high;
				updateCanvasScale();
				getMainMap().paintImmediately(getMainMap().getBounds());
			} catch(NumberFormatException s) {
				updateColorsAndNumbers();
			}
		}
		if(src == textFieldMid) {
			try {
				Layer selected = getSelectedLayer();
				
				float high = selected.paint.max;
				float mid = Float.parseFloat(textFieldMid.getText());
				float low = selected.paint.min;
				
				if(mid > high || mid < low) {
					float midfrac = selected.paint.getFractions()[1];
					float p = (midfrac * (selected.paint.max-selected.paint.min)) + selected.paint.min;
					textFieldMid.setText(String.format("%.3f", p));
				} else {
					float fracMid = 1 - ((mid-low)/(high-low));
					float[] fracs = new float[] {0,fracMid,1};
					selected.updateColors(fracs,getSelectedColors());
				}
				updateCanvasScale();
				getMainMap().paintImmediately(getMainMap().getBounds());
				
			} catch(NumberFormatException s) {
				updateColorsAndNumbers();
			}
		}
		if(src == textFieldLow) {
			try {
				float low = Float.parseFloat(textFieldLow.getText());
				getSelectedLayer().paint.min = low;
				updateCanvasScale();
				getMainMap().paintImmediately(getMainMap().getBounds());
			} catch(NumberFormatException s) {
				updateColorsAndNumbers();
			}
		}
		if(src == sameScale) {
			getSelectedLayer().setValueIndex(valueSlider.getValue());
			updateCanvasScale();
			updateColorsAndNumbers();
			getMainMap().paintImmediately(getMainMap().getBounds());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object src = e.getSource();
		if(src == jcomboBoxHighValue || src == jcomboBoxMidValue || src == jcomboBoxLowValue) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				getSelectedLayer().updateColors(null,getSelectedColors());
				updateCanvasScale();
				updateColorsAndNumbers();
				getMainMap().paintImmediately(getMainMap().getBounds());
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(src == valueSlider) {
			if (!valueSlider.getValueIsAdjusting()) {
				getSelectedLayer().setValueIndex(valueSlider.getValue());
				updateCanvasScale();
				updateColorsAndNumbers();
				getMainMap().paintImmediately(getMainMap().getBounds());
			}
		}
	}
}
