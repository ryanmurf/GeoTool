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
import java.util.HashSet;
import java.util.List;

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

	private boolean scenarioData = false;
	private boolean ensembleData = false;

	public Database(String path) {

		MainDatabase = Paths.get(path);
		if (MainDatabase.getFileName().toString().toLowerCase()
				.contains("current"))
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
			statement.executeQuery("ATTACH DATABASE '"
					+ MainDatabase.toString() + "' AS 'MAINDB';");

			if (ensembleData) {
				for (Path p : ensemblesPaths) {
					statement.executeQuery("ATTACH DATABASE '" + p.toString()
							+ "' AS '" + getEnsembleDatabaseName(p) + "';");
				}
			}

		} catch (SQLException e) {

		}
	}

	String getEnsembleDatabaseName(Path ensemble) {
		String name = ensemble.getFileName().toString().split(".")[0];
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
					if (table.toLowerCase().contains("mean")) {
						tables.add(table.replace("_mean", ""));
					}
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
		for (String name : names) {
			name.replaceAll("_m\\d++_", "_m*_");
			name.replaceAll("_L\\d++_", "_L*_");
			name.replaceAll("doy\\d++", "doy*");
		}
		List<String> reducedNames = new ArrayList<String>(new HashSet<String>(
				names));
		return reducedNames;
	}

	List<String> getEnsembleTables(String ensembleDB) {
		List<String> names = new ArrayList<String>();
		if (ensembleData) {
			try {
				Statement statement = dbTables.createStatement();
				statement.setQueryTimeout(45);
				ResultSet rs = statement.executeQuery("SELECT name FROM "
						+ ensembleDB
						+ ".sqlite_master WHERE type='table' ORDER BY name;");
				while (rs.next()) {
					String tablename = rs.getString("name");
					names.add(tablename);
				}
			} catch (SQLException e) {
				System.out.println("Database : Problem with getEnsembleTables");
			}
		}
		return names;
	}

	List<String> getEnsembleFamiliesAndRanks(String ensembleDB) {
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
	}

	List<Site> getResponseValues(String table, String region,
			String experimental, String scenario, String response) {
		boolean current = true;
		if (scenario.compareTo("Current") != 0) {
			current = false;
			if (!scenarioData || !ensembleData) {
				// problem
			}
		}
		String database = "";
		String Table = "";
		if (scenarioData) {
			database = "MAINDB";
			Table = table;
		} else {
			if (ensembleData) {
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
				regex = response.split("*")[0] + "\\d++";
			else
				regex = response.split("*")[0] + "\\d++" + response.split("*")[1];
			
			for(String name : getTableColumnNames("aggregation_overall_mean")) {
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
		
		String sql = "SELECT "+headerColumn+", "+response+" FROM "+dbtable+" INNER JOIN MAINDB.header ON "+dbtable+".P_id=MAINDB.header.P_id WHERE MAINDB.header.Experimental_Label='"+experimental+"' AND MAINDB.header.Scenario='"+scenario+"'";
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
				Site s = new Site(new GeoPosition(rs.getDouble("Y_WGS84"), rs.getDouble("X_WGS84")), 30);
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
			System.out.println("Could not get response\n"+sql);
		}
		return Site.getMask(sites, true);
		//return sites;
	}
}