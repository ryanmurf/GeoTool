package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoBounds;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class Layer implements ActionListener {

	private class Indexer {
		int idx = 0;
		int alpha = 95;
		boolean mask = false;
	}
	
	final String layerName;
	final List<Site> sites;
	final List<Site> sitesMask;
	final LinearGradientPaint2 paint;
	final Indexer valueIndex = new Indexer();
	
	Painter<JXMapViewer> LayerOverlay;
	MouseListener mouseListener;
	MouseMotionListener mouseMotion;
	boolean selected = false;
	
	private float[] twoColors = new float[] {0,1};
	private float[] threeColors = new float[] {0,0.5f,1f};
	
	JPanel LayerPanel;
	JLabel LayerName;
	JRadioButton Sites;
	JRadioButton Mask;
	ButtonGroup bGroup;
	JComboBox<Integer> alpha;
	final JCheckBox layerVisible;
	JButton delete;
	
	final Map map;
	
	public Layer(String name, List<Site> osites, Color[] colors, Map map, Mask mask, boolean genMask, boolean interpolate, int power, double gridSize) {
		this.map = map;
		layerVisible = new JCheckBox("Visible");
		
		float[] select;
		if(colors.length == 2)
			select=twoColors;
		else
			select=threeColors;
		paint = new LinearGradientPaint2(0, 0, 0, 150, select, colors);
		this.layerName = name;
		this.sites = osites;
		if(genMask) {
			this.sitesMask = getMask(interpolate, power, gridSize);
			this.valueIndex.mask = true;
		} else if(mask != null) {
			setMask(mask, interpolate, power);
			this.sitesMask = mask.raster;
			this.valueIndex.mask = true;
		} else {
			this.sitesMask = null;
		}
		
		setMaxMin();
		
		LayerOverlay = new Painter<JXMapViewer>() {
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
				if (layerVisible.isSelected()) {
					g = (Graphics2D) g.create();
					// convert from viewport to world bitmap
					Rectangle rect = map.getViewportBounds();
					g.translate(-rect.x, -rect.y);

					if (valueIndex.mask) {
						for (Site s : sitesMask) {
							s.setMapPos(map);
							s.draw(g, paint, valueIndex.alpha, valueIndex.idx);
						}
					} else {
						for (Site s : sites) {
							s.setMapPos(map);
							s.draw(g, paint, valueIndex.alpha, valueIndex.idx);
						}
					}
					g.dispose();
				}
			}
		};
		
	}
	
	public void setValueIndex(int index) {
		this.valueIndex.idx = index;
		if(map.sameScale.isSelected())
			setMaxMinOverall();
		else
			setMaxMin();
	}
	
	public int getNumberOfValues() {
		return sites.get(0).respValues.size();
	}
	
	public void setSelected() {
		this.selected = true;
	}
	
	public void setMaxMin() {
		List<Float> values = new ArrayList<Float>();
		if(valueIndex.mask) {
			for (Site s : sitesMask) {
				values.add(s.respValues.get(valueIndex.idx).floatValue());
			}
		} else {
			for (Site s : sites) {
				values.add(s.respValues.get(valueIndex.idx).floatValue());
			}
		}
		paint.max = Collections.max(values).floatValue();
		paint.min = Collections.min(values).floatValue();
	}
	
	public void setMaxMinOverall() {
		List<Float> values = new ArrayList<Float>();
		if(valueIndex.mask) {
			for (Site s : sitesMask) {
				for(Double d : s.respValues)
					values.add(d.floatValue());
			}
		} else {
			for (Site s : sites) {
				for(Double d : s.respValues)
					values.add(d.floatValue());
			}
		}
		paint.max = Collections.max(values).floatValue();
		paint.min = Collections.min(values).floatValue();
	}
	
	public MouseListener getMouseListeners(final Map map) {
		if (mouseListener == null) {
			mouseListener = new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseClicked(MouseEvent e) {
					Rectangle r = map.getMainMap().getViewportBounds();
					int x = e.getX() + r.x;
					int y = e.getY() + r.y;
					if ((e.getButton() == 1)) {
						if (!valueIndex.mask) {
							for (Site s : sites) {
								if (s.contains(x, y)) {
									s.border = true;
									map.getMainMap().paintImmediately(
											map.getMainMap().getBounds());
								}
							}
						}
					}
				}
			};
		}
		return mouseListener;
	}
	
	public void updateColors(float[] fracs, Color[] colors) {
		float[] select;
		if(colors.length == 2)
			select=twoColors;
		else
			select=threeColors;
		if(fracs == null)
			fracs = select;
		paint.setLinearGradient(0, 0, 0, 150, fracs, colors);
	}
	
	public MouseMotionListener getMouseMotionListener(final Map map, final JLabel valueLabel) {
		if (mouseMotion == null) {
			mouseMotion = new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					Rectangle r = map.getMainMap().getViewportBounds();
					int x = e.getX() + r.x;
					int y = e.getY() + r.y;
					List<Site> selected;
					if(valueIndex.mask)
						selected = sitesMask;
					else
						selected = sites;
					for (Site s : selected) {
						if (s.contains(x, y)) {
							s.border = true;
							valueLabel.setText(String.format("%.3f", s.respValues.get(valueIndex.idx)));
							map.getMainMap().paintImmediately(
									map.getMainMap().getBounds());
						} else {
							if(s.border == true) {
								s.border = false;
								map.getMainMap().paintImmediately(
										map.getMainMap().getBounds());
							}
						}
					}
				}

				@Override
				public void mouseDragged(MouseEvent arg0) {

				}
			};
		}
		return mouseMotion;
	}
	
	public void setMask(Mask mask, boolean interpolate, int power) {
		List<Site> raster = mask.raster;
		for (Site n : raster) {
			if (interpolate) {
				for (int k = 0; k < sites.get(0).respValues.size(); k++) {
					double num = 0;
					double den = 0;
					for (Site s : sites) {
						double w = 1 / Math.pow(ClosestPair.distance(n, s), (double) power);
						num += w * s.respValues.get(k);
						den += w;
					}
					n.respValues.add(num / den);
				}
			} else {
				// just copy over values if they contain a site
				List<List<Double>> tempValues = new ArrayList<List<Double>>();

				for (Site s : sites) {
					if (n.contains(s.pos[4])) {
						tempValues.add(new ArrayList<Double>(
								sites.get(0).respValues.size()));
						for (int k = 0; k < sites.get(0).respValues.size(); k++) {
							tempValues.get(tempValues.size() - 1).add(
									s.respValues.get(k));
						}
					}
				}

				List<Double> fValues = new ArrayList<Double>();
				// Average all the sites that go to one cell together
				for (int k = 0; k < sites.get(0).respValues.size(); k++) {
					double temp = 0;
					for (List<Double> l : tempValues) {
						temp += l.get(k).doubleValue();
					}
					fValues.add(temp / tempValues.size());
				}
				n.respValues = fValues;
			}
		}
	}
	
	public List<Site> getMask(boolean interpolate, int power, double minSize) {
		List<GeoPosition> geosites = new ArrayList<GeoPosition>();
		for(Site s : sites) {
			geosites.add(s.pos[4]);
		}
		Set<GeoPosition> geoSet = new HashSet<GeoPosition>(geosites);
		GeoBounds g = new GeoBounds(geoSet);
		
		ClosestPair.Pair p = ClosestPair.divideAndConquer(sites);
		double min = p.distance;
		min = Math.max(min, minSize);
		
		GeoPosition TL = new GeoPosition(g.getNorthWest().getLongitude() - min/2, g.getNorthWest().getLatitude() + min/2);
		GeoPosition BR = new GeoPosition(g.getSouthEast().getLongitude() + min/2, g.getSouthEast().getLatitude() - min/2);
		int cellsHeight = (int) Math.ceil(((TL.getLatitude() - BR.getLatitude())/(min)));
		int cellsWidth = (int) Math.ceil(((BR.getLongitude() - TL.getLongitude())/(min)));
		
		List<Site> raster = new ArrayList<Site>(cellsHeight*cellsWidth);
		for(int i=0; i<cellsHeight; i++) {
			for(int j=0; j<cellsWidth; j++) {
				GeoPosition center = new GeoPosition(g.getNorthWest().getLongitude()-i*min, g.getNorthWest().getLatitude()+j*min);
				Site n = new Site(center, min);
				if (interpolate) {
					for (int k = 0; k < sites.get(0).respValues.size(); k++) {//if we have more then one response value we generate interp for each
						double num = 0;
						double den = 0;
						for (Site s : sites) {
							double w = 1 / Math.pow(ClosestPair.distance(n, s),
									(double) power);
							num += w * s.respValues.get(k);
							den += w;
						}
						n.respValues.add(num / den);
					}
				} else {
					//just copy over values if they contain a site
					List<List<Double>> tempValues = new ArrayList<List<Double>>();

					for (Site s : sites) {
						if(n.contains(s.pos[4])) {
							tempValues.add(new ArrayList<Double>(sites.get(0).respValues.size()));
							for (int k = 0; k < sites.get(0).respValues.size(); k++) {
								tempValues.get(tempValues.size()-1).add(s.respValues.get(k));
							}
						}
					}
					if (tempValues.size() != 0) {
						List<Double> fValues = new ArrayList<Double>();
						// Average all the sites that go to one cell together
						for (int k = 0; k < sites.get(0).respValues.size(); k++) {
							double temp = 0;
							for (List<Double> l : tempValues) {
								temp += l.get(k).doubleValue();
							}
							fValues.add(temp / tempValues.size());
						}
						n.respValues = fValues;
					} else {
						n.respValues.add(0.0);
					}
				}
				raster.add(n);
			}
		}
		return raster;
	}
	
	public JPanel generateLayerPanel(Map map) {
		LayerPanel = new JPanel();
		
		LayerName = new JLabel(this.layerName+" : ");
		LayerPanel.add(LayerName);
		
		Sites = new JRadioButton("Sites");
		Sites.addActionListener(this);
		LayerPanel.add(Sites);
		
		Mask = new JRadioButton("Mask");
		Mask.addActionListener(this);
		if (sitesMask != null) {
			if (sitesMask.size() != 0) {
				valueIndex.mask = true;
				Mask.setSelected(true);
				LayerPanel.add(Mask);
			} else {
				Sites.setSelected(true);
			}
		} else {
			Sites.setSelected(true);
		}
		
		bGroup = new ButtonGroup();
		bGroup.add(Sites);
		if (sitesMask != null)
			if(sitesMask.size() != 0)
				bGroup.add(Mask);
		
		JLabel alphaLabel = new JLabel("Alpha");
		LayerPanel.add(alphaLabel);
		
		alpha = new JComboBox<>(new Integer[] {0, 15, 25, 50, 60, 70, 75, 100, 156, 206, 212, 224,231,248,252, 255});
		alpha.setSelectedIndex(11);
		alpha.addActionListener(this);
		LayerPanel.add(alpha);
		
		layerVisible.setSelected(true);
		layerVisible.addActionListener(map);
		LayerPanel.add(layerVisible);
		
		delete = new JButton("Delete");
		delete.addActionListener(this);
		LayerPanel.add(delete);
		
		return LayerPanel;
	}
	
	public static double Median(List<Double> values) {
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return (double) values.get((values.size() + 1) / 2 - 1);
		else {
			double lower = (double) values.get(values.size() / 2 - 1);
			double upper = (double) values.get(values.size() / 2);

			return (lower + upper) / 2.0;
		}
	}
	
	public static double[] Quartiles(List<Double> values) throws Exception
	{
	    if (values.size() < 3)
	    throw new Exception("This method is not designed to handle lists with fewer than 3 elements.");
	 
	    double median = Median(values);
	 
	    List<Double> lowerHalf = GetValuesLessThan(values, median, true);
	    List<Double> upperHalf = GetValuesGreaterThan(values, median, true);
	 
	    return new double[] {Median(lowerHalf), median, Median(upperHalf)};
	}
	 
	public static List<Double> GetValuesGreaterThan(List<Double> values, double limit, boolean orEqualTo)
	{
		List<Double> modValues = new ArrayList<Double>();
	 
	    for (double value : values)
	        if (value > limit || (value == limit && orEqualTo))
	            modValues.add(value);
	 
	    return modValues;
	}
	 
	public static List<Double> GetValuesLessThan(List<Double> values, double limit, boolean orEqualTo)
	{
		List<Double> modValues = new ArrayList<Double>();
	 
	    for (double value : values)
	        if (value < limit || (value == limit && orEqualTo))
	            modValues.add(value);
	 
	    return modValues;
	}
	
	public static double InterQuartileRange(List<Double> values) throws Exception {
		double[] quartiles = Quartiles(values);
		return quartiles[2] - quartiles[0];
    }
	
	private IntervalXYDataset getDataset() {
		HistogramDataset histogramDataset = new HistogramDataset();
		
		List<Double> values = new ArrayList<Double>();
		if(valueIndex.mask) {
			for (Site s : sitesMask) {
				values.add(s.respValues.get(valueIndex.idx).doubleValue());
			}
		} else {
			for (Site s : sites) {
				values.add(s.respValues.get(valueIndex.idx).doubleValue());
			}
		}
		double max = paint.max;
		double min = paint.min;
		
		int bins = 0;
		try {
			//Freedman-Diaconis rule
			double IQR = InterQuartileRange(values);
			double binSize = (2 * IQR * Math.pow(values.size(), -1/3));
			bins = (int) Math.ceil( (max - min) / binSize );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String s = (String) JOptionPane.showInputDialog("Please, Enter Number of Bins. Freedman-Diaconis Rule recommends "+String.valueOf(bins));
		int ubins = 1;
		try {
			ubins = Integer.parseInt(s);
			bins = ubins;
		} catch(NumberFormatException e) {
			System.out.println(e.toString());
		}
		
		double[] histValues = new double[values.size()];
		for(int i=0; i<values.size(); i++)
			histValues[i] = values.get(i).doubleValue();
		
		
		histogramDataset.addSeries("On Screen Values", histValues, bins);
		
		return histogramDataset;
	}
	
	private JFreeChart createChart(IntervalXYDataset intervalxydataset) {
		String templayerName = layerName;
		if(map.valueSlider.isVisible())
			templayerName = layerName.replace("*", String.valueOf(map.valueSlider.getValue()+1));
		
		JFreeChart jfreechart = ChartFactory.createHistogram(templayerName,
				null, null, intervalxydataset, PlotOrientation.VERTICAL, true,
				true, false);
		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		xyplot.setForegroundAlpha(0.85F);
		XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
		xybarrenderer.setDrawBarOutline(true);
		return jfreechart;
	}
	
	public void showChart() {
		JFrame chartFrame = new JFrame();
		chartFrame.getContentPane().add(new ChartPanel(createChart(getDataset())));
		chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chartFrame.pack();
		chartFrame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == alpha) {
			valueIndex.alpha = ((Integer) alpha.getSelectedItem()).intValue();
			map.getMainMap().paintImmediately(map.getMainMap().getBounds());
		}
		if(src == delete) {
			map.deleteLayer(this);
			map.getMainMap().paintImmediately(map.getMainMap().getBounds());
		}
		if(src == Sites) {
			this.valueIndex.mask = this.Mask.isSelected();
			if(map.sameScale.isSelected())
				setMaxMinOverall();
			else
				setMaxMin();
			map.updateColorsAndNumbers();
			map.updateCanvasScale();
			map.getMainMap().paintImmediately(map.getMainMap().getBounds());
		}
		if(src == Mask) {
			this.valueIndex.mask = this.Mask.isSelected();
			if(map.sameScale.isSelected())
				setMaxMinOverall();
			else
				setMaxMin();
			map.updateColorsAndNumbers();
			map.updateCanvasScale();
			map.getMainMap().paintImmediately(map.getMainMap().getBounds());
		}
	}
}
