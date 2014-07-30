package ryanmurf.powellcenter.wrapper.tools;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Canvas;

public class GeoDataExplorer implements ActionListener, MenuListener {

	private JFileChooser fc = new JFileChooser();
	private JFrame frame;
	private Map map;
	private JMenuBar menuBar;
	private JMenuItem mntmOpen;
	private JMenu mnFile;
	private JSeparator separator;
	private JMenuItem mntmNewMap;
	private JSeparator separator_1;
	private JMenuItem mntmExit;
	
	private String dbFilePath;
	private Database data;
	private ResponseSelection rs;
	
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
		
		this.mntmOpen = new JMenuItem("Open");
		this.mntmOpen.addActionListener(this);
		this.mnFile.add(this.mntmOpen);
		
		this.separator = new JSeparator();
		this.mnFile.add(this.separator);
		
		this.mntmNewMap = new JMenuItem("New Map");
		this.mntmNewMap.setEnabled(false);
		this.mntmNewMap.addActionListener(this);
		this.mnFile.add(this.mntmNewMap);
		
		this.separator_1 = new JSeparator();
		this.mnFile.add(this.separator_1);
		
		this.mntmExit = new JMenuItem("Exit");
		this.mnFile.add(this.mntmExit);
		
	}

	@Override
	public void menuCanceled(MenuEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void menuSelected(MenuEvent e) {
		// TODO Auto-generated method stub
		Object src = (Object) e.getSource();
		if(src==null) {
			
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object src = (Object) e.getSource();
		if(src == mntmOpen) {
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(null);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	this.dbFilePath = fc.getSelectedFile().toPath().toString();
	        	this.data = new Database(this.dbFilePath);
	        	this.mntmNewMap.setEnabled(true);
	        } else {
	        	JOptionPane.showMessageDialog(null, "Could not open file.","Alert", JOptionPane.ERROR_MESSAGE);
	        }
		}
		if(src == mntmNewMap) {
			rs = new ResponseSelection(data, map);
			rs.pack();
		    rs.setVisible(true);
		}
	}

}
