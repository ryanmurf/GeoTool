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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoBounds;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

public class Layer implements ActionListener {

	private class Indexer {
		int idx = 0;
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
	final JCheckBox layerVisible;
	JButton delete;
	
	public Layer(String name, List<Site> osites, List<Site> usitesMask, Color[] colors) {
		layerVisible = new JCheckBox("Visible");
		
		float[] select;
		if(colors.length == 2)
			select=twoColors;
		else
			select=threeColors;
		paint = new LinearGradientPaint2(0, 0, 0, 150, select, colors);
		this.layerName = name;
		this.sites = osites;
		this.sitesMask = usitesMask;
		
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
							s.draw(g, paint, valueIndex.idx);
						}
					} else {
						for (Site s : sites) {
							s.setMapPos(map);
							s.draw(g, paint, valueIndex.idx);
						}
					}
					g.dispose();
				}
			}
		};
		
	}
	
	public void setValueIndex(int index) {
		this.valueIndex.idx = index;
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
		for(Site s : sites) {
			values.add(s.respValues.get(valueIndex.idx).floatValue());
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
						List<Site> selected;
						if(valueIndex.mask)
							selected = sitesMask;
						else
							selected = sites;
						for (Site s : selected) {
							if (s.contains(x, y)) {
								s.border = true;
								map.getMainMap().paintImmediately(
										map.getMainMap().getBounds());
							}
						}
						// System.out.println("clicked");
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
	
	public static List<Site> getMask(List<Site> siteList, boolean interpolate, int power) {
		List<GeoPosition> geosites = new ArrayList<GeoPosition>();
		for(Site s : siteList) {
			geosites.add(s.pos[4]);
		}
		Set<GeoPosition> geoSet = new HashSet<GeoPosition>(geosites);
		GeoBounds g = new GeoBounds(geoSet);
		
		ClosestPair.Pair p = ClosestPair.divideAndConquer(siteList);
		double min = p.distance;
		min = Math.max(min, .1125);
		
		GeoPosition TL = new GeoPosition(g.getNorthWest().getLongitude() - min/2, g.getNorthWest().getLatitude() + min/2);
		GeoPosition BR = new GeoPosition(g.getSouthEast().getLongitude() + min/2, g.getSouthEast().getLatitude() - min/2);
		int cellsHeight = (int) Math.ceil(((TL.getLatitude() - BR.getLatitude())/(min)));
		int cellsWidth = (int) Math.ceil(((BR.getLongitude() - TL.getLongitude())/(min)));
		
		List<Site> raster = new ArrayList<Site>(cellsHeight*cellsWidth);
		for(int i=0; i<cellsHeight; i++) {
			for(int j=0; j<cellsWidth; j++) {
				GeoPosition center = new GeoPosition(g.getNorthWest().getLongitude()-i*min, g.getNorthWest().getLatitude()+j*min);
				Site n = new Site(center, min);
				double num=0;
				double den=0;
				for(Site s : siteList) {
					double w = 1/Math.pow(ClosestPair.distance(n, s),  (double) power);
					num += w*s.respValues.get(0);
					den += w;
				}
				n.respValues.add(num/den);
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
		LayerPanel.add(Sites);
		
		Mask = new JRadioButton("Mask");
		if (sitesMask != null) {
			if (sitesMask.size() != 0) {
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
		
		layerVisible.setSelected(true);
		layerVisible.addActionListener(map);
		LayerPanel.add(layerVisible);
		
		delete = new JButton("Delete");
		delete.addActionListener(map);
		LayerPanel.add(delete);
		
		return LayerPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		//if(src == layerVisible) {
		//	
		//}
	}
}
