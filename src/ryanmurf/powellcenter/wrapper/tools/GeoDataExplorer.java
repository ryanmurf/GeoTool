package ryanmurf.powellcenter.wrapper.tools;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class GeoDataExplorer {

	private JFrame frame;
	private Map map;

	private class GetData implements Runnable {

		private Map map;
		private Database data;
		
		public GetData(Map map, Database data) {
			this.map = map;
		}
		
		@Override
		public void run() {
			map.sites.addAll(data.getResponseValues("aggregation_overall_mean", "1", "Veg_fClimate", "Current", "SWinput_Composition_Grasses_fraction_const"));
			map.setMaxMin();
		}
		
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GeoDataExplorer window = new GeoDataExplorer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GeoDataExplorer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.frame = new JFrame();
		this.frame.setBounds(100, 100, 733, 563);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		map = new Map();
		
		Database data = new Database("/home/ryan/Documents/Work/Product19_SagebrushRemoval/20140723_SagebrushRemoval898_PuntaDelEste/4_Data_SWOutputAggregated/dbTables_final.sqlite3");
		//new Thread(new GetData(map, b));
		map.sites.addAll(data.getResponseValues("aggregation_overall_mean", "1", "Veg_fClimate", "Current", "SWinput_Composition_Grasses_fraction_const"));
		map.setMaxMin();
		/*final int max = 17;
        TileFactoryInfo info = new TileFactoryInfo(0,max,max,
                256, true, true,
                "http://tile.openstreetmap.org",
                "x","y","z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = max-zoom;
                return this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
            }
        };
        info.setDefaultZoomLevel(1);
        
        TileFactory tf = new DefaultTileFactory(info);
        final JXMapKit map = new JXMapKit();
        map.setTileFactory(tf);
        map.setCenterPosition(new GeoPosition(51,30,25,0,7,39)); // the center of London
        
        final List<GeoPosition> region = new ArrayList<GeoPosition>();
        region.add(new GeoPosition(38.266,12.4));
        region.add(new GeoPosition(38.283,15.65));
        region.add(new GeoPosition(36.583,15.166));
        region.add(new GeoPosition(37.616,12.25));
        
        final Polygon poly = new Polygon();
        

        Painter<JXMapViewer> polygonOverlay = new Painter<JXMapViewer>() {
			
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
				g = (Graphics2D) g.create();
		        //convert from viewport to world bitmap
		        Rectangle rect = map.getViewportBounds();
		        g.translate(-rect.x, -rect.y);
		        
		        //create a polygon
		        poly.reset();
		        for(GeoPosition gp : region) {
		            //convert geo to world bitmap pixel
		            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
		            poly.addPoint((int)pt.getX(),(int)pt.getY());
		            
		        }
		        
		        
		        //do the drawing
		        g.setColor(new Color(255,0,0,100));
		        g.fill(poly);
		        g.setColor(Color.RED);
		        g.draw(poly);
		        g.dispose();
			}
		};
		
		
		map.getMainMap().addMouseListener(new MouseListener() {
			
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
				int x = e.getX()+r.x;
				int y = e.getY()+r.y;
				if((e.getButton() == 1) && poly.contains(x,y)) {
					System.out.println("clicked");
				}
				
			}
		});
        
		CompoundPainter<Painter<JXMapViewer>> cp = new CompoundPainter<Painter<JXMapViewer>>();
		cp.setPainters(polygonOverlay);
		cp.setCacheable(false);
		
		map.getMainMap().setOverlayPainter(cp);*/
		
		this.frame.getContentPane().add(map);
	}

}
