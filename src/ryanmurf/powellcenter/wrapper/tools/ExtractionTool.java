package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JList;

public class ExtractionTool extends JFrame implements ActionListener, ItemListener {
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
	private JPanel LayerPanel;
	private JLabel lblSoilLayer;
	private JComboBox<String> comboBoxLayers;
	private JCheckBox chckbxBoundingUse;
	private JRadioButton rdbtnEnsembles;
	private JRadioButton rdbtnScenarios;
	private ButtonGroup rdbtnGroup;
	
	private JList<String> responseList;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	
	private JList<String> headerList;
	private DefaultListModel<String> headerListModel = new DefaultListModel<String>();
	private JButton btnLoadFields;
	private JPanel panel;
	private JLabel lblNewLabel_1;
	private JComboBox<String> comboBox_format;
	private JFileChooser fc = new JFileChooser();
	
	private class GetData implements Runnable {

		@Override
		public void run() {
			setData();
		}
		
	}
	
	private class SaveData implements Runnable {
		
		@Override
		public void run() {
			String table = (String) comboBox_Table.getSelectedItem();
			String region = (String) comboBox_Region.getSelectedItem();
			String experimental = (String) comboBox_Experimental.getSelectedItem();
			String scenario = (String) comboBox_Scenario.getSelectedItem();
			List<String> response = responseList.getSelectedValuesList();
			List<String> header = headerList.getSelectedValuesList();
			int format = comboBox_format.getSelectedIndex();
			String whereClause = "";
			if(LayerPanel.isVisible() && !((String) comboBoxLayers.getSelectedItem()).contains("All")) {
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
			File file = null;
			ExtractionTool.this.fc.setSelectedFile(new File(textField_LayerName.getText()+".csv"));
			int returnVal = ExtractionTool.this.fc.showSaveDialog(ExtractionTool.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = ExtractionTool.this.fc.getSelectedFile();
				data.saveCSV(file, table, region, experimental, scenario, header, response, whereClause, rdbtnScenarios.isSelected(), format);
				//dispatchEvent(new WindowEvent(ExtractionTool.this, WindowEvent.WINDOW_CLOSING));
				ExtractionTool.this.btnLoadMap.setEnabled(true);
			} else {
				ExtractionTool.this.btnLoadMap.setEnabled(true);
			}
		}
	}
	
	public ExtractionTool(Database d) {
		super();
		this.data = d;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.getContentPane().add(getResponseSelection());
	}
	
	public void setData() {
		List<String> headerColumns = data.getTableColumnNames("header", true);
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
		
		if (data.contains(headerColumns, "Experimental_Label", false)) {
			List<String> experimentals = data.getExperimentalLabels();
			String[] sExperimentals = new String[experimentals.size()+1];
			sExperimentals[0] = "All";
			for(int i=1; i<experimentals.size()+1; i++) {
				sExperimentals[i] = experimentals.get(i-1);
			}
			comboBox_Experimental.setModel(new DefaultComboBoxModel<String>(sExperimentals));
			comboBox_Experimental.setSelectedIndex(0);
		} else {
			ExperimentalPanel.setVisible(false);
		}
		{
			loadScenarioEorS();
		}
		if (data.contains(headerColumns, "Region", false)) {
			List<Integer> regions = data.getRegions();
			String[] sRegions = new String[regions.size()+1];
			sRegions[0] = "All";
			for(int i=1; i<regions.size()+1; i++) {
				String name = "";
				switch(regions.get(i-1)) {
					case 1:
						name = "South America";
						break;
					case 2:
						name = "South Africa";
						break;
					case 3:
						name = "East Asia";
						break;
					case 4:
						name = "West Asia";
						break;
					case 5:
						name = "Europe";
						break;
					case 6:
						name = "North America";
						break;
					default:
						name = String.valueOf(regions.get(i-1));
				}
				sRegions[i] = name;
			}
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
			headerListModel.clear();
			List<String> columnNames = data.getTableColumnNames("header", true);
			
			for(int i=0; i<columnNames.size(); i++)
				headerListModel.addElement(columnNames.get(i));
			
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
			String[] layers = data.getSoilLayers((String) comboBox_Table.getSelectedItem());
			String[] list = new String[layers.length+1];
			list[0] = "All";
			for(int i=1; i<=layers.length; i++) {
				list[i] = layers[i-1];
			}
			comboBoxLayers.setModel(new DefaultComboBoxModel<String>( list ));
		} else {
			LayerPanel.setVisible(false);
		}
	}
	
	public void setResponseNames() {
		listModel.clear();
		List<String> columnNames = data.getTableColumnNames((String) comboBox_Table.getSelectedItem(), false);
		
		for(int i=0; i<columnNames.size(); i++)
			listModel.addElement(columnNames.get(i));
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
		
		this.lblLayerName = new JLabel("File Name:");
		this.LayerNamePanel.add(this.lblLayerName);
		
		this.textField_LayerName = new JTextField();
		this.LayerNamePanel.add(this.textField_LayerName);
		this.textField_LayerName.setColumns(20);
		
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
		this.comboBoxLayers.setFont(new Font("Dialog", Font.PLAIN, 12));
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
		
		this.panel = new JPanel();
		ResponsePanel.add(this.panel);
		
		this.btnLoadFields = new JButton("Load Fields");
		this.panel.add(this.btnLoadFields);
		this.btnLoadFields.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.btnLoadFields.addActionListener(this);
		
		this.lblNewLabel_1 = new JLabel("Floating Point Number Format: ");
		this.panel.add(this.lblNewLabel_1);
		
		this.comboBox_format = new JComboBox<String>();
		this.panel.add(this.comboBox_format);
		this.comboBox_format.setModel(new DefaultComboBoxModel<String>(new String[] {"No Format", "1 Decimal", "2 Decimals", "3 Decimals", "4 Decimals", "5 Decimals"}));
		comboBox_format.setSelectedIndex(0);
		
		this.RespMainPanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) this.RespMainPanel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		ResponsePanel.add(this.RespMainPanel);
		{
			JPanel headerPanel = new JPanel();
			headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
			this.RespMainPanel.add(headerPanel);
			
			JLabel headerLabel = new JLabel("Header Field(s):");
			headerLabel.setAlignmentX(CENTER_ALIGNMENT);
			headerPanel.add(headerLabel);
			
			this.headerList = new JList<String>();
			this.headerList.setModel(headerListModel);
			JScrollPane hspane = new JScrollPane(headerList);
			headerPanel.add(hspane);
			
			this.RespPanel = new JPanel();
			this.RespMainPanel.add(this.RespPanel);
			this.RespPanel.setLayout(new BoxLayout(this.RespPanel, BoxLayout.Y_AXIS));
			
			this.lblResponseFields = new JLabel("Response Field(s):");
			this.RespPanel.add(this.lblResponseFields);
			this.lblResponseFields.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.responseList = new JList<String>();
			this.responseList.setModel(listModel);
			JScrollPane spane = new JScrollPane(responseList);
			this.RespPanel.add(spane);
			
		}
		
		this.ButtonPanel = new JPanel();
		ResponsePanel.add(this.ButtonPanel);
		
		this.btnLoadMap = new JButton("Save CSV File");
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

		if(src == rdbtnEnsembles || src == rdbtnScenarios) {
			loadScenarioEorS();
		}
		if(src == this.btnLoadMap) {
			this.btnLoadMap.setEnabled(false);
			
			Thread gettingData = new Thread(new SaveData());
			gettingData.run();
		}
		if(src == this.btnCancel) {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		if(src == this.btnLoadFields) {
			JTextArea textArea = new JTextArea();
			textArea.setColumns(50);
			textArea.setRows(20);
			JScrollPane pta = new JScrollPane(textArea);
			int response;
			response = JOptionPane.showConfirmDialog(this, pta, "Enter Data", JOptionPane.OK_CANCEL_OPTION);
			if(response == JOptionPane.OK_OPTION) {
				String text = textArea.getText();
				String lines[] = text.split("\\r?\\n");
				ArrayList<Integer> indicesR = new ArrayList<>();
				ArrayList<Integer> indicesH = new ArrayList<>();
				for(String field : lines) {
					field = field.trim();
					if(listModel.contains(field)) {
						indicesR.add(listModel.indexOf(field));
					}
					if(headerListModel.contains(field)) {
						indicesH.add(headerListModel.indexOf(field));
					}
				}
				int both[] = concat(responseList.getSelectedIndices(), convertIntegers(indicesR));
				responseList.setSelectedIndices(both);
				both = concat(headerList.getSelectedIndices(), convertIntegers(indicesH));
				headerList.setSelectedIndices(both);
			}
		}
	}

	int[] concat(int[] A, int[] B) {
		int aLen = A.length;
		int bLen = B.length;
		int[] C = new int[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
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
