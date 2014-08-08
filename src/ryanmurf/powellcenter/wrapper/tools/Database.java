package ryanmurf.powellcenter.wrapper.tools;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class Database {
	private final static String[] headerTables = new String[] { "runs",
			"sqlite_sequence", "header", "run_labels", "scenario_labels",
			"sites", "experimental_labels", "treatments", "simulation_years",
			"weatherfolders" };

	private Path MainDatabase;
	private List<Path> ensemblesPaths;
	private Connection dbTables;
	//private WeatherData weatherData;

	boolean scenarioData = false;
	boolean ensembleData = false;

	public Database(String path) {

		MainDatabase = Paths.get(path);
		if (MainDatabase.getFileName().toString().toLowerCase().contains("current"))
			scenarioData = false;
		else
			scenarioData = true;

		try {
			listEnsembleFiles(MainDatabase.getParent());
		} catch (IOException e) {
			ensembleData = false;
		}

		if (ensemblesPaths.isEmpty())
			ensembleData = false;
		else
			ensembleData = true;
		
		connect();

	}

	void listEnsembleFiles(Path dir) throws IOException {
		ensemblesPaths = new ArrayList<Path>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
				"dbEnsemble_*.{sqlite,sqlite3}")) {
			for (Path entry : stream) {
				ensemblesPaths.add(entry);
			}
		} catch (DirectoryIteratorException ex) {
			// I/O error encounted during the iteration, the cause is an
			// IOException
			throw ex.getCause();
		}
	}

	void connect() {
		try {
			dbTables = DriverManager.getConnection("jdbc:sqlite::memory:");

			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			statement.executeQuery("ATTACH DATABASE '" + MainDatabase.toString() + "' AS 'MAINDB';");
		} catch (SQLException e) {
			//System.out.println("Database : Connect Problem MAINDB " + e.toString());
		}
		if (ensembleData) {
			for (Path p : ensemblesPaths) {
				String ensembleDBname = getEnsembleDatabaseName(p).toUpperCase();
				try {
					Statement statement = dbTables.createStatement();
					statement.setQueryTimeout(45);
					statement.executeQuery("ATTACH DATABASE '" + p.toString()
						+ "' AS '" + ensembleDBname + "';");
				} catch(SQLException e) {
					//System.out.println("Database : Connect Problem Ensemble " + e.toString());
				}
			}
		}
		//double check scenario data S
		if(scenarioData) {
			List<String> scenarios = getScenarioLabels();
			if(scenarios.size() == 1)
				scenarioData = false;
		}
		
	}

	String getEnsembleDatabaseName(Path ensemble) {
		String name = ensemble.getFileName().toString().split("\\.")[0];
		// dbEnsemble_aggregation_doy_AET
		name = name.replace("doy_", "");
		name = name.replace("dbEnsemble_aggregation_", "");
		return name.toUpperCase();
	}

	boolean isHeaderTable(String table) {
		table = table.toLowerCase();
		for (int i = 0; i < headerTables.length; i++) {
			if (headerTables[i].trim().toLowerCase().contains(table)) {
				return true;
			}
		}
		return false;
	}
	
	boolean contains(List<String> list, String value) {
		value = value.toLowerCase();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).trim().toLowerCase().contains(value)) {
				return true;
			}
		}
		return false;
	}
	
	public double getSiteMaxLatitude() {
		double v = 0;
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("SELECT MAX(Y_WGS84) FROM MAINDB.sites;");
			while (rs.next()) {
				v = rs.getDouble(1);
			}
		} catch (SQLException e) {
			System.out.println("Database : getSiteMaxLatitude");
		}
		return v;
	}
	
	public double getSiteMinLatitude() {
		double v = 0;
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("SELECT MIN(Y_WGS84) FROM MAINDB.sites;");
			while (rs.next()) {
				v = rs.getDouble(1);
			}
		} catch (SQLException e) {
			System.out.println("Database : getSiteMinLatitude");
		}
		return v;
	}
	
	public double getSiteMaxLongitude() {
		double v = 0;
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("SELECT MAX(X_WGS84) FROM MAINDB.sites;");
			while (rs.next()) {
				v = rs.getDouble(1);
			}
		} catch (SQLException e) {
			System.out.println("Database : getSiteMaxLongitude");
		}
		return v;
	}
	
	public boolean getIsTableLayers(String table) {
		List<String> columnNames = getTableColumnNames(table);
		return contains(columnNames, "Soil_Layer");
	}
	
	public int getMaxSoilLayer(String table) {
		int v = 0;
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("SELECT MAX(Soil_Layer) FROM MAINDB."+table+";");
			while (rs.next()) {
				v = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println("Database : getMaxSoilLayer");
		}
		return v;
	}
	
	public String[] getSoilLayers(String table) {
		if(getIsTableLayers(table)) {
			int max = getMaxSoilLayer(table);
			String[] vals = new String[max];
			for(int i=0; i<max; i++)
				vals[i] = String.valueOf(i+1);
			return vals;
		}
		return null;
	}
	
	public double getSiteMinLongitude() {
		double v = 0;
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("SELECT MIN(X_WGS84) FROM MAINDB.sites;");
			while (rs.next()) {
				v = rs.getDouble(1);
			}
		} catch (SQLException e) {
			System.out.println("Database : getSiteMinLongitude");
		}
		return v;
	}

	List<Integer> getRegions() {
		List<Integer> regions = new ArrayList<Integer>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement
					.executeQuery("SELECT DISTINCT Region FROM MAINDB.sites ORDER BY Region;");
			while (rs.next()) {
				int region = rs.getInt("Region");
				regions.add(region);
			}
		} catch (SQLException e) {
			System.out.println("Database : getRegions");
		}
		return regions;
	}

	List<String> getExperimentalLabels() {
		List<String> ExperimentalLabels = new ArrayList<String>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement
					.executeQuery("SELECT id,label FROM MAINDB.experimental_labels ORDER BY id;");
			while (rs.next()) {
				String label = rs.getString("label");
				ExperimentalLabels.add(label);
			}
		} catch (SQLException e) {
			System.out.println("Database : getExperimentalLabels");
		}
		return ExperimentalLabels;
	}

	List<String> getScenarioLabels() {
		List<String> ScenarioLabels = new ArrayList<String>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement
					.executeQuery("SELECT id,label FROM MAINDB.scenario_labels ORDER BY id;");
			while (rs.next()) {
				String label = rs.getString("label");
				ScenarioLabels.add(label);
			}
		} catch (SQLException e) {
			System.out.println("Database : getScenarioLabels");
		}
		return ScenarioLabels;
	}

	List<String> getWeatherFolders() {
		List<String> weatherFolders = new ArrayList<String>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement
					.executeQuery("SELECT id,folder FROM MAINDB.weatherfolders ORDER BY id;");
			while (rs.next()) {
				String label = rs.getString("folder");
				weatherFolders.add(label);
			}
		} catch (SQLException e) {
			System.out.println("Database : getTables");
		}
		return weatherFolders;
	}

	List<String> getTables() {
		List<String> tables = new ArrayList<String>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement
					.executeQuery("SELECT name FROM MAINDB.sqlite_master WHERE type='table' ORDER BY name;");
			while (rs.next()) {
				String table = rs.getString("name");
				if (!isHeaderTable(table)) {
					//if (table.toLowerCase().contains("mean")) {
					//	tables.add(table.toLowerCase().replace("_mean", ""));
					//}
					tables.add(table);
				}
			}
		} catch (SQLException e) {
			System.out.println("Database : getTables");
		}
		return tables;
	}

	List<String> getTableColumnNames(String table) {
		List<String> names = new ArrayList<String>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(45);
			ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table
					+ ");");
			while (rs.next()) {
				String columnName = rs.getString("name");
				names.add(columnName);
			}
		} catch (SQLException e) {
			System.out.println("Database : getTableColumnNames");
		}
		return names;
	}

	List<String> getReducedNames(String table) {
		List<String> names = getTableColumnNames(table);
		for(int i=0; i<names.size(); i++) {
			names.set(i, names.get(i).replaceAll("_m\\d++_", "_m*_"));
			names.set(i, names.get(i).replaceAll("_L\\d++_", "_L*_"));
			names.set(i, names.get(i).replaceAll("doy\\d++", "doy*"));
		}
		List<String> reducedNames = new ArrayList<String>(new HashSet<String>(
				names));
		Collections.sort(reducedNames);
		return reducedNames;
	}

	List<String> getEnsembleTables(String ensembleDB) {
		List<String> names = new ArrayList<String>();
		if (ensembleData) {
			try {
				Statement statement = dbTables.createStatement();
				statement.setQueryTimeout(45);
				ResultSet rs = statement.executeQuery("SELECT name FROM "
						+ ensembleDB.toUpperCase()
						+ ".sqlite_master WHERE type='table' ORDER BY name;");
				while (rs.next()) {
					String tablename = rs.getString("name");
					names.add(tablename);
				}
			} catch (SQLException e) {
				System.out.println("Database : Problem with getEnsembleTables "+ e.toString());
			}
		}
		return names;
	}

	List<String> getEnsembleFamiliesAndRanks() {
		if (ensembleData) {
			String ensembleDB = getEnsembleDatabaseName(this.ensemblesPaths.get(0));
			List<String> names = getEnsembleTables(ensembleDB);
			List<String> Ensembles = new ArrayList<String>();
			if (names.size() > 0) {
				for (String name : names) {
					if (name.toLowerCase().contains("_means")) {
						Ensembles.add(name.replace("_means", ""));
					}
				}
			}
			return Ensembles;
		} else
			return null;
		
	}

	List<Site> getResponseValues(String table, String region,
			String experimental, String scenario, String response, String whereClause, boolean bScenario) {
		
		if(region == null)
			region = "";
		
		boolean current = true;
		if (scenario.compareTo("Current") != 0) {
			current = false;
			if (!scenarioData || !ensembleData) {
				// problem
			}
		}
		String database = "";
		String Table = "";
		if (scenarioData && bScenario) {
			database = "MAINDB";
			Table = table;
		} else {
			if (ensembleData && !bScenario) {
				if (current) {
					database = "MAINDB";
					Table = table;
				} else {
					database = table.toUpperCase();
					Table = scenario+"_means";
					scenario = "Current";
				}
			} else {
				database = "MAINDB";
				Table = table;
			}
		}
		
		String dbtable = database+"."+Table;
		int responses = 1;
		
		List<String> responseMatches = new ArrayList<String>();
		if(response.contains("_m*_") || response.contains("_L*_") || response.contains("doy*")) {
			
			String regex;
			if(response.contains("doy*"))
				regex = response.split("\\*")[0] + "\\d++";
			else
				regex = response.split("\\*")[0] + "\\d++" + response.split("\\*")[1];
			
			for(String name : getTableColumnNames(Table)) {
				if(name.matches(regex)) {
					responseMatches.add(name);
				}
			}
			
			response = "";
			responses = responseMatches.size();
			for(String resp : responseMatches) {
				response += dbtable+"."+resp+" AS "+resp+",";
			}
			response = response.substring(0, response.length()-1);
			
		} else {
			response = dbtable+"."+response+" AS "+response;
		}
		List<String> headerColumns = getTableColumnNames("header");
		
		String headerColumn = "";
		if(contains(headerColumns,"P_id"))
			headerColumn += "MAINDB.header.P_id AS P_id, ";
		if(contains(headerColumns,"site_id"))
			headerColumn += "MAINDB.header.site_id AS site_id, ";
		if(contains(headerColumns,"Region"))
			headerColumn += "MAINDB.header.Region AS Region, ";
		if(contains(headerColumns,"X_WGS84"))
			headerColumn += "MAINDB.header.X_WGS84 AS X_WGS84, ";
		if(contains(headerColumns,"Y_WGS84"))
			headerColumn += "MAINDB.header.Y_WGS84 AS Y_WGS84";
		
		String sqlExperimental = "";
		if(experimental != null) {
			sqlExperimental = "MAINDB.header.Experimental_Label='"+experimental+"' AND ";
		}
		String sql = "SELECT "+headerColumn+", "+response+" FROM "+dbtable+" INNER JOIN MAINDB.header ON "+dbtable+".P_id=MAINDB.header.P_id WHERE "+sqlExperimental+"MAINDB.header.Scenario='"+scenario+"'";
		if(whereClause.length() != 0)
			sql += " AND "+whereClause;
		if(region.compareTo("All") != 0 && contains(headerColumns,"Region")) {
			sql += " AND MAINDB.header.Region="+region;
		}
		if(contains(headerColumns,"Region"))
			sql += " ORDER BY MAINDB.header.Region;";
		
		List<Site> sites = new ArrayList<Site>();
		try {
			Statement statement = dbTables.createStatement();
			statement.setQueryTimeout(240);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				Site s = new Site(new GeoPosition(rs.getDouble("Y_WGS84"), rs.getDouble("X_WGS84")), .2125);
				if(contains(headerColumns,"P_id"))
					s.P_id = rs.getInt("P_id");
				if(contains(headerColumns,"site_id"))
					s.Site_id = rs.getInt("site_id");
				if(contains(headerColumns,"Region"))
					s.region = rs.getInt("Region");
				if(responses > 1) {
					for(String sp : responseMatches) {
						s.respValues.add(rs.getDouble(sp));
					}
				} else {
					s.respValues.add(rs.getDouble(response.split(" AS ")[1]));
				}
				sites.add(s);
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Could not get response\n"+sql+"\n"+e.toString(), "alert", JOptionPane.ERROR_MESSAGE);
			System.out.println("Could not get response\n"+sql);
		}
		if(sites.size() == 0)
			JOptionPane.showMessageDialog(null, "Zero Sites Selected", "alert", JOptionPane.ERROR_MESSAGE);
		//return Site.getMask(sites, true);
		return sites;
	}
}
