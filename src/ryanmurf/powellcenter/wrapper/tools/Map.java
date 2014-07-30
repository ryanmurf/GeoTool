package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

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
	//final LinearGradientPaint2 paint;
	final Integer valueIndex = new Integer(0);
	
	private final List<Layer> layers = new ArrayList<Layer>();
	private CompoundPainter<Painter<JXMapViewer>> cp;
	private List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

	private Canvas canvas;
	
	private JComboBox<Color> 
	
	public Map() {
		super();
		//paint = new LinearGradientPaint2(0, 0, 50, 50, new float[] {0, 1.0f/3.0f, 2.0f/3.0f}, new Color[] {Color.red, Color.blue, Color.green});
		
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
		
		cp = new CompoundPainter<Painter<JXMapViewer>>();
		cp.setCacheable(false);
		
		Painter<JXMapViewer> layerNameOverlay = new Painter<JXMapViewer>() {
			
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
				// TODO Auto-generated method stub
				for(Layer l : layers) {
					if(l.selected) {
						g.setPaint(new Color(0, 0, 0, 150));
						int width = g.getFontMetrics().stringWidth(l.layerName);
						g.fillRoundRect(10, 10, width+40, 30, 10, 10);
						g.setPaint(Color.WHITE);
						g.drawString(l.layerName, 10 + 10, 10 + 20);
					}
				}
			}
		};
		
		JPanel scale = new JPanel();
		GridBagConstraints gbc_canvas = new GridBagConstraints();
		gbc_canvas.anchor = GridBagConstraints.EAST;
		gbc_canvas.gridx = 1;
		gbc_canvas.gridy = 0;
		
		JPanel left = new JPanel();
		left.setLayout(null);
		left.setSize(50, 150);
		
		this.canvas = new Canvas();
		canvas.setSize(15, 150);
		scale.add(canvas);
		
		this.getMainMap().add(scale, gbc_canvas);
		
		addLayerPainter(layerNameOverlay);
	}
	
	public void addLayer(Layer layer) {
		this.getMainMap().addMouseListener(layer.getMouseListeners(this));
		this.getMainMap().addMouseMotionListener(layer.getMouseMotionListener(this));
		this.addLayerPainter(layer.LayerOverlay);
		this.layers.add(layer);
		this.layers.get(layers.size() - 1).setSelected();
		updateCanvasScale();
	}
	
	private void addLayerPainter(Painter<JXMapViewer> painter) {
		this.painters.add(painter);
		Painter<JXMapViewer>[] paints = painters.toArray(new Painter[painters.size()]);
		this.cp.setPainters(paints);
		getMainMap().setOverlayPainter(cp);
	}
	
	private void removeLayerPainter(Painter<JXMapViewer> painter) {
		this.painters.remove(painter);
		Painter<JXMapViewer>[] paints = painters.toArray(new Painter[painters.size()]);
		this.cp.setPainters(paints);
		getMainMap().setOverlayPainter(cp);
	}
	private void updateCanvasScale() {
		Graphics g = canvas.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		for(Layer l : layers) {
			if(l.selected) {
				g2.setPaint(l.paint);
				g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			}
		}
	}
}
