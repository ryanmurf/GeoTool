package ryanmurf.powellcenter.wrapper.tools;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

import java.awt.Font;
import java.awt.Component;
import java.io.IOException;

import javax.swing.JFormattedTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileNameExtensionFilter;

import ryanmurf.powellcenter.wrapper.tools.GeoDataExplorer.MaskInfo;

public class ResponseSelection extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	
	private JPanel TablePanel;
	private JLabel lblTable;
	private JComboBox<String> comboBox_Table;
	private JPanel ExperimentalPanel;
	private JLabel lblExperimental;
	private JComboBox<String> comboBox_Experimental;
	private JPanel ScenarioPanel;
	private JLabel lblScenario;
	private JComboBox<String> comboBox_Scenario;
	private JPanel RegionPanel;
	private JLabel lblRegion;
	private JComboBox<String> comboBox_Region;
	private JPanel RespPanel;
	private JLabel lblResponseFields;
	private JComboBox<String> comboBox_Response;
	private JCheckBox chckbxReduce;
	private JPanel MaskPanel;
	private JLabel lblMask;
	private JButton btnLoadMask;
	private JCheckBox chckbxGenerateMask;
	private JPanel InterpPanel;
	private JLabel lblInterpolate;
	private JCheckBox chckbxShepardsMethod;
	private JComboBox<String> comboBox_Power;
	private JPanel panel;
	private JPanel BoundingPanel;
	private JPanel BoundingLabelPanel;
	private JLabel lblLocationBoundingBox;
	private JPanel BoundingLatPanel;
	private JLabel lblLatitude;
	private JFormattedTextField formattedTextField_LatMax;
	private JLabel lblNewLabel;
	private JFormattedTextField formattedTextField_LatMin;
	private JPanel BoundingLongPanel;
	private JLabel lblLongitude;
	private JFormattedTextField formattedTextField_LongMax;
	private JLabel lblMin;
	private JFormattedTextField formattedTextField_LongMin;
	private JPanel ButtonPanel;
	private JButton btnLoadMap;
	private JButton btnCancel;
	private JPanel BoundingMainPanel;
	private JPanel RespMainPanel;
	private JPanel LayerNamePanel;
	private JTextField textField_LayerName;
	private JLabel lblLayerName;
	
	private Database data;
	private Map map;
	private JPanel LayerPanel;
	private JLabel lblSoilLayer;
	private JComboBox<String> comboBoxLayers;
	private JCheckBox chckbxBoundingUse;
	private JLabel lblGridSize;
	private JFormattedTextField formattedTextFieldGridSize;
	private JRadioButton rdbtnEnsembles;
	private JRadioButton rdbtnScenarios;
	private ButtonGroup rdbtnGroup;
	
	Mask mask;
	
	private MaskInfo maskInfo;
	
	private class GetData implements Runnable {

		@Override
		public void run() {
			setData();
		}
		
	}
	
	private class GetLayer implements Runnable {

		@Override
		public void run() {
			String table = (String) comboBox_Table.getSelectedItem();
			String region = (String) comboBox_Region.getSelectedItem();
			String experimental = (String) comboBox_Experimental.getSelectedItem();
			String scenario = (String) comboBox_Scenario.getSelectedItem();
			String response = (String) comboBox_Response.getSelectedItem();
			String whereClause = "";
			if(LayerPanel.isVisible()) {
				whereClause += "Soil_Layer = "+(String) comboBoxLayers.getSelectedItem();
			}
			if(chckbxBoundingUse.isSelected()) {
				if(whereClause.length() != 0)
					whereClause += " AND ";
				else {
					String LongMin = String.valueOf(((Number) formattedTextField_LongMin.getValue()).doubleValue());
					String LongMax = String.valueOf(((Number) formattedTextField_LongMax.getValue()).doubleValue());
					
					String LatMin = String.valueOf(((Number) formattedTextField_LatMin.getValue()).doubleValue());
					String LatMax = String.valueOf(((Number) formattedTextField_LatMax.getValue()).doubleValue());
					whereClause += "X_WGS84 BETWEEN "+LongMin+" AND "+LongMax+" AND Y_WGS84 BETWEEN "+LatMin+" AND "+LatMax;
				}
			}
			
			boolean genMask = chckbxGenerateMask.isSelected();
			boolean Interpolate = chckbxShepardsMethod.isSelected();
			int power = Integer.valueOf((String) comboBox_Power.getSelectedItem());
			
			List<Site> sites = data.getResponseValues(table, region, experimental, scenario, response, whereClause, rdbtnScenarios.isSelected());
			Layer l = new Layer(textField_LayerName.getText(), sites, map.getSelectedColors(), map, mask, genMask, Interpolate, power);
			map.addLayer(l);
			
			dispatchEvent(new WindowEvent(ResponseSelection.this, WindowEvent.WINDOW_CLOSING));
		}
		
	}
	
	public ResponseSelection(Database d, Map map, MaskInfo maskInfo) {
		super();
		this.data = d;
		this.map = map;
		this.maskInfo = maskInfo;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.getContentPane().add(getResponseSelection());
	}
	
	public void setData() {
		List<String> headerColumns = data.getTableColumnNames("header");
		List<String> tables = data.getTables();
		String[] sTables = tables.toArray(new String[tables.size()]);
		comboBox_Table.setEnabled(true);
		comboBox_Table.setModel(new DefaultComboBoxModel<String>(sTables));
		comboBox_Table.setSelectedIndex(0);
		
		if(data.ensembleData && data.scenarioData) {
			rdbtnEnsembles.setVisible(true);
			rdbtnEnsembles.setSelected(true);
			rdbtnScenarios.setVisible(true);
			rdbtnScenarios.setSelected(false);
		} else if(data.ensembleData && !data.scenarioData) {
			rdbtnEnsembles.setVisible(true);
			rdbtnEnsembles.setSelected(true);
			rdbtnScenarios.setVisible(false);
			rdbtnScenarios.setSelected(false);
		} else if(!data.ensembleData && data.scenarioData) {
			rdbtnEnsembles.setVisible(false);
			rdbtnEnsembles.setSelected(false);
			rdbtnScenarios.setVisible(true);
			rdbtnScenarios.setSelected(true);
		} else if(!data.ensembleData && !data.scenarioData) {
			//only current values are set
			rdbtnGroup.remove(rdbtnEnsembles);
			rdbtnGroup.remove(rdbtnScenarios);
			rdbtnEnsembles.setVisible(false);
			rdbtnEnsembles.setSelected(false);
			rdbtnScenarios.setVisible(false);
			rdbtnScenarios.setSelected(false);
		}
		rdbtnScenarios.addActionListener(this);
		rdbtnEnsembles.addActionListener(this);
		
		if (data.contains(headerColumns, "Experimental_Label")) {
			List<String> experimentals = data.getExperimentalLabels();
			String[] sExperimentals = experimentals.toArray(new String[experimentals.size()]);
			comboBox_Experimental.setModel(new DefaultComboBoxModel<String>(sExperimentals));
			comboBox_Experimental.setSelectedIndex(0);
		} else {
			ExperimentalPanel.setVisible(false);
		}
		{
			loadScenarioEorS();
		}
		if (data.contains(headerColumns, "Region")) {
			List<Integer> regions = data.getRegions();
			String[] sRegions = new String[regions.size()+1];
			sRegions[0] = "All";
			for(int i=1; i<regions.size()+1; i++)
				sRegions[i] = String.valueOf(regions.get(i-1));
			comboBox_Region.setModel(new DefaultComboBoxModel<String>(sRegions));
			comboBox_Region.setSelectedIndex(0);
		} else {
			RegionPanel.setVisible(false);
		}
		setLayersPanel();
		{
			formattedTextField_LatMax.setValue(data.getSiteMaxLatitude());
			formattedTextField_LatMin.setValue(data.getSiteMinLatitude());
			formattedTextField_LongMax.setValue(data.getSiteMaxLongitude());
			formattedTextField_LongMin.setValue(data.getSiteMinLongitude());
		}
		{
			setResponseNames();
		}
	}
	
	private void loadScenarioEorS() {
		if(rdbtnScenarios.isSelected()) {
			List<String> scenarios = data.getScenarioLabels();
			String[] sScenarios = scenarios.toArray(new String[scenarios.size()]);
			comboBox_Scenario.setModel(new DefaultComboBoxModel<String>(sScenarios));
			comboBox_Scenario.setSelectedIndex(0);
		} else if(rdbtnEnsembles.isSelected()) {
			List<String> ensemble = data.getEnsembleFamiliesAndRanks();
			String[] sEnsemble = ensemble.toArray(new String[ensemble.size()+1]);
			for(int i=0; i<ensemble.size()+1; i++) {
				if(i==0)
					sEnsemble[i] = "Current";
				else
					sEnsemble[i] = ensemble.get(i-1);
			}
			comboBox_Scenario.setModel(new DefaultComboBoxModel<String>(sEnsemble));
			comboBox_Scenario.setSelectedIndex(0);
		} else {
			//only Current data
			comboBox_Scenario.setModel(new DefaultComboBoxModel<String>(new String[] {"Current"}));
			comboBox_Scenario.setSelectedIndex(0);
		}
	}
	
	public void setLayersPanel() {
		if(data.getIsTableLayers((String) comboBox_Table.getSelectedItem())) {
			LayerPanel.setVisible(true);
			comboBoxLayers.setModel(new DefaultComboBoxModel<String>(data.getSoilLayers((String) comboBox_Table.getSelectedItem())));
		} else {
			LayerPanel.setVisible(false);
		}
	}
	
	public void setResponseNames() {
		if(chckbxReduce.isSelected()) {
			List<String> columnNames = data.getReducedNames((String) comboBox_Table.getSelectedItem());
			String[] sColumnNames = columnNames.toArray(new String[columnNames.size()]);
			comboBox_Response.setModel(new DefaultComboBoxModel<String>(sColumnNames));
			comboBox_Response.setSelectedIndex(1);
		} else {
			List<String> columnNames = data.getTableColumnNames((String) comboBox_Table.getSelectedItem());
			String[] sColumnNames = columnNames.toArray(new String[columnNames.size()]);
			comboBox_Response.setModel(new DefaultComboBoxModel<String>(sColumnNames));
			comboBox_Response.setSelectedIndex(1);
		}
	}
	/**
	 * @wbp.parser.entryPoint
	 */
	public JPanel getResponseSelection() {
		JPanel ResponsePanel = new JPanel();
		ResponsePanel.setLayout(new BoxLayout(ResponsePanel, BoxLayout.Y_AXIS));
		
		this.LayerNamePanel = new JPanel();
		FlowLayout flowLayout_2_1 = (FlowLayout) this.LayerNamePanel.getLayout();
		flowLayout_2_1.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.LayerNamePanel);
		
		this.lblLayerName = new JLabel("Layer Name:");
		this.LayerNamePanel.add(this.lblLayerName);
		
		this.textField_LayerName = new JTextField();
		this.LayerNamePanel.add(this.textField_LayerName);
		this.textField_LayerName.setColumns(20);
		
		this.lblGridSize = new JLabel("Site Size:");
		this.LayerNamePanel.add(this.lblGridSize);
		
		this.formattedTextFieldGridSize = new JFormattedTextField();
		this.formattedTextFieldGridSize.setColumns(6);
		this.formattedTextFieldGridSize.setText("0.3125");
		this.LayerNamePanel.add(this.formattedTextFieldGridSize);
		
		this.rdbtnEnsembles = new JRadioButton("Ensembles");
		this.rdbtnEnsembles.setSelected(true);
		this.LayerNamePanel.add(this.rdbtnEnsembles);
		
		this.rdbtnScenarios = new JRadioButton("Scenarios");
		this.LayerNamePanel.add(this.rdbtnScenarios);
		
		rdbtnGroup = new ButtonGroup();
		rdbtnGroup.add(rdbtnEnsembles);
		rdbtnGroup.add(rdbtnScenarios);
		
		this.TablePanel = new JPanel();
		ResponsePanel.add(this.TablePanel);
		this.TablePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		this.lblTable = new JLabel("Table:");
		this.TablePanel.add(this.lblTable);
		
		this.comboBox_Table = new JComboBox<String>();
		this.comboBox_Table.addItemListener(this);
		this.TablePanel.add(this.comboBox_Table);
		
		{
			this.ExperimentalPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) this.ExperimentalPanel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			ResponsePanel.add(this.ExperimentalPanel);
		
			this.lblExperimental = new JLabel("Experimental:");
			this.ExperimentalPanel.add(this.lblExperimental);
		
			this.comboBox_Experimental = new JComboBox<String>();
			this.comboBox_Experimental.setFont(new Font("Dialog", Font.PLAIN, 12));
			this.ExperimentalPanel.add(this.comboBox_Experimental);
		}
		{
			this.ScenarioPanel = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) this.ScenarioPanel
					.getLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);
			ResponsePanel.add(this.ScenarioPanel);

			this.lblScenario = new JLabel("Scenario:");
			this.ScenarioPanel.add(this.lblScenario);

			this.comboBox_Scenario = new JComboBox<String>();
			this.comboBox_Scenario.setFont(new Font("Dialog", Font.PLAIN, 12));
			this.ScenarioPanel.add(this.comboBox_Scenario);
		}
		{
			this.RegionPanel = new JPanel();
			FlowLayout flowLayout_2 = (FlowLayout) this.RegionPanel.getLayout();
			flowLayout_2.setAlignment(FlowLayout.LEFT);
			ResponsePanel.add(this.RegionPanel);

			this.lblRegion = new JLabel("Region:");
			this.RegionPanel.add(this.lblRegion);

			this.comboBox_Region = new JComboBox<String>();
			this.comboBox_Region.setFont(new Font("Dialog", Font.PLAIN, 12));
			this.RegionPanel.add(this.comboBox_Region);
		}
		
		this.LayerPanel = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) this.LayerPanel.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.LayerPanel);
		
		this.lblSoilLayer = new JLabel("Soil Layer:");
		this.LayerPanel.add(this.lblSoilLayer);
		
		this.comboBoxLayers = new JComboBox<String>();
		this.LayerPanel.add(this.comboBoxLayers);
		
		this.BoundingMainPanel = new JPanel();
		FlowLayout fl_BoundingMainPanel = (FlowLayout) this.BoundingMainPanel.getLayout();
		fl_BoundingMainPanel.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.BoundingMainPanel);
		{
			this.BoundingPanel = new JPanel();
			this.BoundingMainPanel.add(this.BoundingPanel);

			this.BoundingLabelPanel = new JPanel();

			this.lblLocationBoundingBox = new JLabel("Location Bounding Box:");
			this.BoundingLabelPanel.add(this.lblLocationBoundingBox);

			this.BoundingLatPanel = new JPanel();
			FlowLayout flowLayout_2 = (FlowLayout) this.BoundingLatPanel
					.getLayout();
			flowLayout_2.setAlignment(FlowLayout.LEFT);

			this.lblLatitude = new JLabel("Latitude    Max:");
			this.BoundingLatPanel.add(this.lblLatitude);

			this.formattedTextField_LatMax = new JFormattedTextField(
					NumberFormat.getNumberInstance());
			this.formattedTextField_LatMax.setValue(new Double(0));
			this.formattedTextField_LatMax.setColumns(8);
			this.BoundingLatPanel.add(this.formattedTextField_LatMax);

			this.lblNewLabel = new JLabel("Min:");
			this.BoundingLatPanel.add(this.lblNewLabel);

			this.formattedTextField_LatMin = new JFormattedTextField(
					NumberFormat.getNumberInstance());
			this.formattedTextField_LatMin.setValue(new Double(0));
			this.formattedTextField_LatMin.setColumns(8);
			this.BoundingLatPanel.add(this.formattedTextField_LatMin);

			this.BoundingLongPanel = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) this.BoundingLongPanel
					.getLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);

			this.lblLongitude = new JLabel("Longitude Max:");
			this.BoundingLongPanel.add(this.lblLongitude);

			this.formattedTextField_LongMax = new JFormattedTextField(
					NumberFormat.getNumberInstance());
			this.formattedTextField_LongMax.setValue(new Double(0));
			this.formattedTextField_LongMax.setColumns(8);
			this.BoundingLongPanel.add(this.formattedTextField_LongMax);

			this.lblMin = new JLabel("Min:");
			this.BoundingLongPanel.add(this.lblMin);

			this.formattedTextField_LongMin = new JFormattedTextField(
					NumberFormat.getNumberInstance());
			this.formattedTextField_LongMin.setValue(new Double(0));
			this.formattedTextField_LongMin.setColumns(8);
			this.BoundingLongPanel.add(this.formattedTextField_LongMin);
			GroupLayout gl_BoundingPanel = new GroupLayout(this.BoundingPanel);
			gl_BoundingPanel.setHorizontalGroup(
				gl_BoundingPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_BoundingPanel.createSequentialGroup()
						.addGap(5)
						.addGroup(gl_BoundingPanel.createParallelGroup(Alignment.LEADING)
							.addComponent(this.BoundingLabelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(this.BoundingLatPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(this.BoundingLongPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
			);
			gl_BoundingPanel.setVerticalGroup(
				gl_BoundingPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_BoundingPanel.createSequentialGroup()
						.addGap(5)
						.addComponent(this.BoundingLabelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(5)
						.addComponent(this.BoundingLatPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(5)
						.addComponent(this.BoundingLongPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
			);
			
			this.chckbxBoundingUse = new JCheckBox("Use");
			this.BoundingLabelPanel.add(this.chckbxBoundingUse);
			this.BoundingPanel.setLayout(gl_BoundingPanel);
		}
		
		this.RespMainPanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) this.RespMainPanel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.RespMainPanel);
		{
			this.RespPanel = new JPanel();
			this.RespMainPanel.add(this.RespPanel);
			this.RespPanel.setLayout(new BoxLayout(this.RespPanel, BoxLayout.Y_AXIS));
			
			this.panel = new JPanel();
			this.panel.setMaximumSize(new Dimension(500, 30));
			this.RespPanel.add(this.panel);
			
						this.lblResponseFields = new JLabel("Response Field(s):");
						this.panel.add(this.lblResponseFields);
						this.lblResponseFields.setAlignmentX(Component.CENTER_ALIGNMENT);
						
						this.chckbxReduce = new JCheckBox("Reduce");
						this.panel.add(this.chckbxReduce);
						this.chckbxReduce.setAlignmentX(Component.CENTER_ALIGNMENT);
						this.chckbxReduce.setSelected(true);
						this.chckbxReduce.addActionListener(this);

			this.comboBox_Response = new JComboBox<String>();
			comboBox_Response.setMaximumSize(new Dimension(500, 75));
			comboBox_Response.addItemListener(this);
			this.comboBox_Response.setFont(new Font("Dialog", Font.PLAIN, 10));
			this.RespPanel.add(this.comboBox_Response);
		}
		
		this.MaskPanel = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) this.MaskPanel.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.MaskPanel);
		
		this.lblMask = new JLabel("Mask:");
		this.MaskPanel.add(this.lblMask);
		
		this.btnLoadMask = new JButton("Load Mask");
		this.btnLoadMask.addActionListener(this);
		this.MaskPanel.add(this.btnLoadMask);
		
		this.chckbxGenerateMask = new JCheckBox("Generate Mask");
		this.MaskPanel.add(this.chckbxGenerateMask);
		
		this.InterpPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) this.InterpPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.InterpPanel);
		
		this.lblInterpolate = new JLabel("Interpolate:");
		this.InterpPanel.add(this.lblInterpolate);
		
		this.chckbxShepardsMethod = new JCheckBox("Inverse Distance Weighting");
		this.InterpPanel.add(this.chckbxShepardsMethod);
		
		this.comboBox_Power = new JComboBox<String>();
		this.comboBox_Power.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}));
		this.InterpPanel.add(this.comboBox_Power);
		
		this.ButtonPanel = new JPanel();
		ResponsePanel.add(this.ButtonPanel);
		
		this.btnLoadMap = new JButton("Load Map");
		this.btnLoadMap.addActionListener(this);
		this.ButtonPanel.add(this.btnLoadMap);
		
		this.btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		this.ButtonPanel.add(this.btnCancel);
		
		Thread k = new Thread(new GetData());
		k.run();
		
		return ResponsePanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == this.chckbxReduce) {
			setResponseNames();
		}
		if(src == rdbtnEnsembles || src == rdbtnScenarios) {
			loadScenarioEorS();
		}
		if(src == this.btnLoadMask) {
			int nregion = 0;
			String region = (String) this.comboBox_Region.getSelectedItem();
			if (region != null) {
				if (region.compareTo("All") == 0)
					nregion = 0;
				else
					nregion = Integer.valueOf(region);
			}
			mask = new Mask(nregion);
			
			JFileChooser fc;
			
			if(maskInfo.maskPath == null)
				fc = new JFileChooser();
			else
				fc = new JFileChooser(maskInfo.maskPath.toFile());
			
			fc.setAcceptAllFileFilterUsed(false);
	    	fc.setMultiSelectionEnabled(false);
	    	fc.setFileFilter(new FileNameExtensionFilter("*", "asc"));
	    	
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(null);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		maskInfo.maskPath = fc.getSelectedFile().toPath().getParent();
					mask.read(fc.getSelectedFile().toPath());
					this.btnLoadMask.setEnabled(false);
					this.chckbxGenerateMask.setSelected(false);
					this.chckbxGenerateMask.setEnabled(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	        } else {
	        	JOptionPane.showMessageDialog(null, "Could not open file.","Alert", JOptionPane.ERROR_MESSAGE);
	        }
		}
		if(src == this.btnLoadMap) {
			this.btnLoadMap.setEnabled(false);
			
			Thread gettingData = new Thread(new GetLayer());
			gettingData.run();
		}
		if(src == this.btnCancel) {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object src = e.getSource();
		if(src == comboBox_Table) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				setResponseNames();
				setLayersPanel();
				this.pack();
			}
		}
		if(src == comboBox_Response) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				this.textField_LayerName.setText((String) comboBox_Response.getSelectedItem());
			}
		}
	}
}
