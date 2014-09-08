package ryanmurf.powellcenter.wrapper.tools;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JCheckBoxMenuItem;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class GeoDataExplorer implements ActionListener, MenuListener {

	private JFileChooser fc = new JFileChooser();
	private JFrame frame;
	private Map map;
	private JMenuBar menuBar;
	private JMenuItem mntmOpen;
	private JMenu mnFile;
	private JMenuItem mntmNewMap;
	private JSeparator separator_1;
	private JMenuItem mntmExit;
	
	private String dbFilePath;
	private Database data;
	private ResponseSelection rs;
	private JMenu mnView;
	private JCheckBoxMenuItem chckbxmntmColors;
	private JCheckBoxMenuItem chckbxmntmColorValues;
	private JSeparator separator_2;
	private JMenu mnMapProvider;
	private JMenu mnGoogle;
	private JMenuItem mntmGhybrid;
	private JMenuItem mntmGSatellite;
	private JMenuItem mntmGstreet1;
	private JMenuItem mntmGstreet2;
	private JMenuItem mntmGTerrain;
	private JMenuItem mntmOpenStreetMap;
	private JMenuItem mntmMapQuest;
	private JMenu mnNokia;
	private JMenuItem mntmNormal;
	private JMenuItem mntmNormalGrey;
	private JMenuItem mntmNormalTransit;
	private JMenuItem mntmSatellite;
	private JMenuItem mntmTerrain;
	private JMenu mnEdit;
	private JMenuItem mntmLayers;
	
	public class MaskInfo {
		Path maskPath;
	}
	MaskInfo maskInfo = new MaskInfo();
	private JSeparator separator_3;
	private JMenuItem mntmHistogram;
	private JMenu mnTools;
	private JMenuItem mntmDataExtraction;
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
		
		fc.setAcceptAllFileFilterUsed(false);
    	fc.setMultiSelectionEnabled(false);
    	fc.setFileFilter(new FileNameExtensionFilter("dbTables*", "sqlite3"));
		//new Thread(new GetData(map, b));
		//map.sites.addAll(data.getResponseValues("aggregation_overall_mean", "1", "Veg_fClimate", "Current", "SWinput_Composition_Grasses_fraction_const",""));
		//map.setMaxMin();
		
		map = new Map();
		this.frame.getContentPane().add(map);
		
		this.menuBar = new JMenuBar();
		this.frame.setJMenuBar(this.menuBar);
		
		this.mnFile = new JMenu("File");
		this.menuBar.add(this.mnFile);
		
		this.mntmOpen = new JMenuItem("Open Database");
		this.mntmOpen.addActionListener(this);
		this.mnFile.add(this.mntmOpen);
		
		this.separator_1 = new JSeparator();
		this.mnFile.add(this.separator_1);
		
		this.mntmExit = new JMenuItem("Exit");
		this.mnFile.add(this.mntmExit);
		
		this.mnEdit = new JMenu("Edit");
		this.menuBar.add(this.mnEdit);
		
		this.mntmLayers = new JMenuItem("Layers");
		this.mntmLayers.addActionListener(this);
		this.mnEdit.add(this.mntmLayers);
		
		this.mntmNewMap = new JMenuItem("New Layer");
		this.mnEdit.add(this.mntmNewMap);
		this.mntmNewMap.setEnabled(false);
		this.mntmNewMap.addActionListener(this);
		
		this.mnTools = new JMenu("Tools");
		this.menuBar.add(this.mnTools);
		
		this.mntmDataExtraction = new JMenuItem("Data Extraction");
		this.mntmDataExtraction.setEnabled(false);
		this.mntmDataExtraction.addActionListener(this);
		this.mnTools.add(this.mntmDataExtraction);
		
		this.mnView = new JMenu("View");
		this.menuBar.add(this.mnView);
		
		this.chckbxmntmColors = new JCheckBoxMenuItem("Show Colors");
		this.chckbxmntmColors.setSelected(true);
		this.chckbxmntmColors.addActionListener(this);
		this.mnView.add(this.chckbxmntmColors);
		
		this.chckbxmntmColorValues = new JCheckBoxMenuItem("Show Color Values");
		this.chckbxmntmColorValues.setSelected(true);
		this.chckbxmntmColorValues.addActionListener(this);
		this.mnView.add(this.chckbxmntmColorValues);
		
		this.separator_2 = new JSeparator();
		this.mnView.add(this.separator_2);
		
		this.mnMapProvider = new JMenu("Map Provider");
		this.mnView.add(this.mnMapProvider);
		
		this.mnGoogle = new JMenu("Google");
		this.mnMapProvider.add(this.mnGoogle);
		
		this.mntmGhybrid = new JMenuItem("Hybrid");
		this.mntmGhybrid.addActionListener(this);
		this.mnGoogle.add(this.mntmGhybrid);
		
		this.mntmGSatellite = new JMenuItem("Satellite");
		this.mntmGSatellite.addActionListener(this);
		this.mnGoogle.add(this.mntmGSatellite);
		
		this.mntmGstreet1 = new JMenuItem("Street 1");
		this.mntmGstreet1.addActionListener(this);
		this.mnGoogle.add(this.mntmGstreet1);
		
		this.mntmGstreet2 = new JMenuItem("Street 2");
		this.mntmGstreet2.addActionListener(this);
		this.mnGoogle.add(this.mntmGstreet2);
		
		this.mntmGTerrain = new JMenuItem("Terrain");
		this.mntmGTerrain.addActionListener(this);
		this.mnGoogle.add(this.mntmGTerrain);
		
		this.mntmOpenStreetMap = new JMenuItem("Open Street Map");
		this.mntmOpenStreetMap.addActionListener(this);
		this.mnMapProvider.add(this.mntmOpenStreetMap);
		
		this.mntmMapQuest = new JMenuItem("Map Quest");
		this.mntmMapQuest.addActionListener(this);
		this.mnMapProvider.add(this.mntmMapQuest);
		
		this.mnNokia = new JMenu("Nokia OVI Maps");
		this.mnMapProvider.add(this.mnNokia);
		
		this.mntmNormal = new JMenuItem("Normal");
		this.mntmNormal.addActionListener(this);
		this.mnNokia.add(this.mntmNormal);
		
		this.mntmNormalGrey = new JMenuItem("Normal (Grey)");
		this.mntmNormalGrey.addActionListener(this);
		this.mnNokia.add(this.mntmNormalGrey);
		
		this.mntmNormalTransit = new JMenuItem("Normal (Transit)");
		this.mntmNormalTransit.addActionListener(this);
		this.mnNokia.add(this.mntmNormalTransit);
		
		this.mntmSatellite = new JMenuItem("Satellite");
		this.mntmSatellite.addActionListener(this);
		this.mnNokia.add(this.mntmSatellite);
		
		this.mntmTerrain = new JMenuItem("Terrain");
		this.mntmTerrain.addActionListener(this);
		this.mnNokia.add(this.mntmTerrain);
		
		this.separator_3 = new JSeparator();
		this.mnView.add(this.separator_3);
		
		this.mntmHistogram = new JMenuItem("Histogram");
		this.mntmHistogram.addActionListener(this);
		this.mnView.add(this.mntmHistogram);
		
	}

	@Override
	public void menuCanceled(MenuEvent e) {
		
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		
	}

	@Override
	public void menuSelected(MenuEvent e) {
		Object src = (Object) e.getSource();
		if(src==null) {
			
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = (Object) e.getSource();
		if(src == mntmOpen) {
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(null);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	this.dbFilePath = fc.getSelectedFile().toPath().toString();
	        	this.data = new Database(this.dbFilePath);
	        	this.mntmNewMap.setEnabled(true);
	        	this.mntmDataExtraction.setEnabled(true);
	        } else {
	        	JOptionPane.showMessageDialog(null, "Could not open file.","Alert", JOptionPane.ERROR_MESSAGE);
	        }
		}
		if(src == mntmNewMap) {
			rs = new ResponseSelection(data, map, maskInfo);
			rs.pack();
		    rs.setVisible(true); 
		}
		if(src == mntmDataExtraction) {
			ExtractionTool tl = new ExtractionTool(data);
			tl.pack();
			tl.setVisible(true);
		}
		if(src == chckbxmntmColors) {
			map.setColorsHidden(chckbxmntmColors.isSelected());
		}
		if(src == chckbxmntmColorValues) {
			map.setColorValuesHidden(chckbxmntmColorValues.isSelected());
		}
		if(src == mntmLayers) {
			Layers t = new Layers(map.layers, map);
			t.pack();
		    t.setVisible(true);
		}
		if(src == mntmHistogram) {
			this.map.getSelectedLayer().showChart();
		}
		if(src == mntmGhybrid) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://mt1.google.com/vt/lyrs=y",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"&x="+x+"&y="+y+"&z="+zoom;
	            }
	        };
	        info.setDefaultZoomLevel(14);
	        
	        TileFactory tf = new DefaultTileFactory(info);
	        map.setTileFactory(tf);
	        map.setCenterPosition(new GeoPosition(41.3167,-105.5833));
	        map.setZoomSliderVisible(true);
	        map.setZoom(14);
		}
		if(src == mntmGSatellite) {
			TileFactoryInfo info = new TileFactoryInfo(0,13,13,
	                256, true, true,
	                "http://khm1.google.com/kh/v=59",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"&x="+x+"&y="+y+"&z="+zoom;
	            }
	        };
	        info.setDefaultZoomLevel(13);
	        
	        TileFactory tf = new DefaultTileFactory(info);
	        map.setTileFactory(tf);
	        map.setCenterPosition(new GeoPosition(41.3167,-105.5833));
	        map.setZoomSliderVisible(true);
	        map.setZoom(13);
		}
		if(src == mntmGstreet1) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://mt0.google.com/vt/",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"x="+x+"&y="+y+"&z="+zoom;
	            }
	        };
	        info.setDefaultZoomLevel(15);
	        
	        TileFactory tf = new DefaultTileFactory(info);
	        map.setTileFactory(tf);
	        map.setCenterPosition(new GeoPosition(41.3167,-105.5833));
	        map.setZoomSliderVisible(true);
	        map.setZoom(15);
		}
		if (src == mntmGstreet2) {
			TileFactoryInfo info = new TileFactoryInfo(0, 17, 17, 256, true,
					true, "http://mt1.google.com/vt/lyrs=m", "x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = 17 - zoom;
					return this.baseURL + "&x=" + x + "&y=" + y + "&z=" + zoom;
				}
			};
			info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if (src == mntmGTerrain) {
			TileFactoryInfo info = new TileFactoryInfo(0, 17, 17, 256, true,
					true, "http://mt1.google.com/vt/lyrs=p&", "x", "y", "z") {
				public String getTileUrl(int x, int y, int zoom) {
					zoom = 17 - zoom;
					return this.baseURL + "x=" + x + "&y=" + y + "&z=" + zoom;
				}
			};
			info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if (src == mntmOpenStreetMap) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://tile.openstreetmap.org",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if (src == mntmMapQuest) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://otile1.mqcdn.com/tiles/1.0.0/osm/",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if(src == mntmNormal) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+"/256/png8";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if(src == mntmNormalGrey) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.grey",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+"/256/png8";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if(src == mntmNormalTransit) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.transit",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+"/256/png8";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if(src == mntmSatellite) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/satellite.day",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+"/256/png8";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
		if(src == mntmTerrain) {
			TileFactoryInfo info = new TileFactoryInfo(0,17,17,
	                256, true, true,
	                "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/terrain.day",
	                "x","y","z") {
	            public String getTileUrl(int x, int y, int zoom) {
	                zoom = 17-zoom;
	                return this.baseURL +"/"+zoom+"/"+x+"/"+y+"/256/png8";
	            }
	        };
	        info.setDefaultZoomLevel(15);

			TileFactory tf = new DefaultTileFactory(info);
			map.setTileFactory(tf);
			map.setCenterPosition(new GeoPosition(41.3167, -105.5833));
			map.setZoomSliderVisible(true);
			map.setZoom(15);
		}
	}
}
