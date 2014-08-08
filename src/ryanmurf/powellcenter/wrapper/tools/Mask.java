package ryanmurf.powellcenter.wrapper.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class Mask {
	int nRegion;
	String RegionName;
	int ncols;
	int nrows;
	double XLLCORNER;
	double YLLCORNER;
	double cellSize;
	int noDataValue;
	
	int[][] mask;
	
	List<Site> raster = new ArrayList<Site>();
	
	public Mask(int region) {
		nRegion = region;
	}
	
	public void read(Path maskPath) throws IOException {
		List<String> lines = java.nio.file.Files.readAllLines(maskPath, StandardCharsets.UTF_8);
		for(int i=0; i<lines.size(); i++) {
			String line = lines.get(i).trim();
			String[] values = line.split("[ \t]+");
			switch (i) {
			case 0:
				ncols = Integer.parseInt(values[1]);
				break;
			case 1:
				nrows = Integer.parseInt(values[1]);
				mask = new int[nrows][ncols];
				break;
			case 2:
				XLLCORNER = Double.parseDouble(values[1]);
				break;
			case 3:
				YLLCORNER = Double.parseDouble(values[1]);
				break;
			case 4:
				cellSize = Double.parseDouble(values[1]);
				break;
			case 5:
				noDataValue = Integer.parseInt(values[1]);
				break;
			default:
				int row = i-6;
				for(int j=0; j<ncols; j++) {
					mask[row][j] = (int)Double.parseDouble(values[j]);
				}
				break;
			}
		}
		generateSites();
	}
	
	public void generateSites() {
		for(int i=0; i<mask.length; i++) {
			for(int j=0; j<mask[i].length; j++) {
				if(mask[i][j] != noDataValue) {
					GeoPosition UL = new GeoPosition((nrows-i)*cellSize + YLLCORNER, j*cellSize + XLLCORNER);
					GeoPosition UR = new GeoPosition((nrows-i)*cellSize + YLLCORNER, j*cellSize + XLLCORNER + cellSize);
					GeoPosition BR = new GeoPosition((nrows-i)*cellSize + YLLCORNER - cellSize, j*cellSize + XLLCORNER + cellSize);
					GeoPosition BL = new GeoPosition((nrows-i)*cellSize + YLLCORNER - cellSize, j*cellSize + XLLCORNER);
					raster.add(new Site(UL,UR,BL,BR));
				}
			}
		}
	}
}
