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

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

public class Map extends JXMapKit {
	private static final long serialVersionUID = 1L;
	
	final List<Site> sites = new ArrayList<Site>();
	final LinearGradientPaint2 paint;
	final Integer valueIndex = new Integer(0);

	public Map() {
		super();
		paint = new LinearGradientPaint2(0, 0, 50, 50, new float[] {0, 1.0f/3.0f, 2.0f/3.0f}, new Color[] {Color.red, Color.blue, Color.green});
		
		//this.setDefaultProvider(DefaultProviders.OpenStreetMaps);
		//TileFactoryInfo info = new TileFactoryInfo(0,17,17,
        //        256, true, true,
        //        "http://tile.openstreetmap.org",
        //        "x","y","z") {
        //    public String getTileUrl(int x, int y, int zoom) {
        //        zoom = 17-zoom;
        //        return this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
        //    }
        //};
        //info.setDefaultZoomLevel(1);
        
       //TileFactory tf = new DefaultTileFactory(info);
        //setTileFactory(tf);
		
		
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
		
		setGridPainter();
		setListeners();
	}
	
	public void setMaxMin() {
		List<Float> values = new ArrayList<Float>();
		for(Site s : sites) {
			values.add(s.respValues.get(valueIndex).floatValue());
		}
		paint.max = Collections.max(values).floatValue();
		paint.min = Collections.min(values).floatValue();
	}
	
	
	public void setGridPainter() {
		Painter<JXMapViewer> polygonOverlay = new Painter<JXMapViewer>() {
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
		
		CompoundPainter<Painter<JXMapViewer>> cp = new CompoundPainter<Painter<JXMapViewer>>();
		cp.setPainters(polygonOverlay);
		cp.setCacheable(false);
		
		getMainMap().setOverlayPainter(cp);
	}
	
	public void setListeners() {
		getMainMap().addMouseListener(new MouseListener() {
			
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
				Rectangle r = getMainMap().getViewportBounds();
				int x = e.getX()+r.x;
				int y = e.getY()+r.y;
				if((e.getButton() == 1)) {
					for(Site s : sites) {
						if(s.contains(x,y)) {
							s.border = true;
							getMainMap().paintImmediately(getMainMap().getBounds());
						}
					}
					//System.out.println("clicked");
				}
			}
		});
		getMainMap().addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				Rectangle r = getMainMap().getViewportBounds();
				int x = e.getX()+r.x;
				int y = e.getY()+r.y;
				for(Site s : sites) {
					if(s.contains(x,y)) {
						s.border = true;
						getMainMap().paintImmediately(getMainMap().getBounds());
					}
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
}
