package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.painter.Painter;

public class Layer {

	final String layerName;
	final List<Site> sites;
	final List<Site> sitesMask;
	final LinearGradientPaint2 paint;
	final Integer valueIndex = new Integer(0);
	
	Painter<JXMapViewer> LayerOverlay;
	MouseListener mouseListener;
	MouseMotionListener mouseMotion;
	boolean selected = false;
	
	public Layer(String name, List<Site> osites, List<Site> usitesMask) {
		paint = new LinearGradientPaint2(0, 0, 0, 150, new float[] {0, 1.0f/3.0f, 2.0f/3.0f}, new Color[] {Color.red, Color.blue, Color.green});
		this.layerName = name;
		this.sites = osites;
		this.sitesMask = usitesMask;
		
		setMaxMin();
		
		LayerOverlay = new Painter<JXMapViewer>() {
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
				g = (Graphics2D) g.create();
		        //convert from viewport to world bitmap
		        Rectangle rect = map.getViewportBounds();
		        g.translate(-rect.x, -rect.y);
		        
				for (Site s : sites) {
					s.setMapPos(map);
					s.draw(g, paint, valueIndex.intValue());
				}
				
				g.dispose();
			}
		};
		
	}
	public void setSelected() {
		this.selected = true;
	}
	public void setMaxMin() {
		List<Float> values = new ArrayList<Float>();
		for(Site s : sites) {
			values.add(s.respValues.get(valueIndex).floatValue());
		}
		paint.max = Collections.max(values).floatValue();
		paint.min = Collections.min(values).floatValue();
	}
	
	public MouseListener getMouseListeners(final Map map) {
		if (mouseListener == null) {
			mouseListener = new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseClicked(MouseEvent e) {
					Rectangle r = map.getMainMap().getViewportBounds();
					int x = e.getX() + r.x;
					int y = e.getY() + r.y;
					if ((e.getButton() == 1)) {
						for (Site s : sites) {
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
	
	public MouseMotionListener getMouseMotionListener(final Map map) {
		if (mouseMotion == null) {
			mouseMotion = new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					// TODO Auto-generated method stub
					Rectangle r = map.getMainMap().getViewportBounds();
					int x = e.getX() + r.x;
					int y = e.getY() + r.y;
					for (Site s : sites) {
						if (s.contains(x, y)) {
							s.border = true;
							map.getMainMap().paintImmediately(
									map.getMainMap().getBounds());
						}
					}
				}

				@Override
				public void mouseDragged(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}
			};
		}
		return mouseMotion;
	}
}
