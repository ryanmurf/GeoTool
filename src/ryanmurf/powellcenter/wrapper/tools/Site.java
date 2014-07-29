package ryanmurf.powellcenter.wrapper.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoBounds;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public class Site extends Rectangle {
	private static final long serialVersionUID = 1L;
	/**
	 * Site id is the id from weather database
	 */
	int Site_id;
	/**
	 * P_id is the id from the storage database
	 */
	int P_id;
	/**
	 * The region the site is in
	 */
	int region;
	/**
	 * The variable we are looking at
	 */
	String response;
	/**
	 * The value(s) we are looking at
	 */
	List<java.lang.Double> respValues = new ArrayList<java.lang.Double>();
	/**
	 * The Geo position of 5 corners UL, UR, BR, BL, CENTER
	 */
	GeoPosition[] pos = new GeoPosition[5];
	/**
	 * Border
	 */
	boolean border = false;
	boolean draw = true;
	
	public Site(GeoPosition center) {
		pos[4] = new GeoPosition(center.getLatitude(), center.getLongitude());
	}
	
	public Site(GeoPosition center, double gridSize) {
		pos[0] = new GeoPosition(center.getLatitude()+gridSize/2, center.getLongitude()-gridSize/2);
		pos[1] = new GeoPosition(center.getLatitude()+gridSize/2, center.getLongitude()+gridSize/2);
		pos[2] = new GeoPosition(center.getLatitude()-gridSize/2, center.getLongitude()-gridSize/2);
		pos[3] = new GeoPosition(center.getLatitude()-gridSize/2, center.getLongitude()+gridSize/2);
		
		pos[4] = new GeoPosition(center.getLatitude(), center.getLongitude());
	}

	public Site(GeoPosition UL, GeoPosition UR, GeoPosition BL, GeoPosition BR) {
		pos[0] = UL;
		pos[1] = UR;
		pos[2] = BL;
		pos[3] = BR;
		
		pos[4] = new GeoPosition((UR.getLatitude()-BL.getLatitude())/2, (UR.getLongitude()-BL.getLongitude())/2);
	}
	
	public double arcSecondsToXYsize(double arcSecondsGridSize) {
		return (arcSecondsGridSize/2)/3600;
	}
	
	public double getLatitude() {
		return pos[4].getLatitude();
	}
	
	public double getLongitude() {
		return pos[4].getLongitude();
	}
	
	public void setSize(double gridSize) {
		pos[0] = new GeoPosition(pos[4].getLatitude()+gridSize/2, pos[4].getLongitude()-gridSize/2);
		pos[1] = new GeoPosition(pos[4].getLatitude()+gridSize/2, pos[4].getLongitude()+gridSize/2);
		pos[2] = new GeoPosition(pos[4].getLatitude()-gridSize/2, pos[4].getLongitude()-gridSize/2);
		pos[3] = new GeoPosition(pos[4].getLatitude()-gridSize/2, pos[4].getLongitude()+gridSize/2);
	}

	public void setMapPos(JXMapViewer map) {
		Point2D ptUL = map.getTileFactory().geoToPixel(pos[0], map.getZoom());
		Point2D ptBR = map.getTileFactory().geoToPixel(pos[1], map.getZoom());
		int sizeX = (int) (ptBR.getX() - ptUL.getX());
		int sizeY = (int) (ptUL.getY() - ptBR.getY());
		this.setLocation((int) ptUL.getX(), (int) ptUL.getY());
		this.setSize(sizeX, sizeY);
	}

	public void draw(Graphics2D g, LinearGradientPaint2 paint, int valueIndex) {
		if(draw) {
			float respValue = respValues.get(valueIndex).floatValue();
			g.setColor(paint.getColorAt(paint.getFraction(respValue)));
			g.fill(this);
			if(border)
				g.setColor(Color.black);
			g.draw(this);
		} else {
			if(border) {
				g.setColor(Color.black);
				g.draw(this);
			}
		}
	}
	
	public static List<Site> getMask(List<Site> siteList, boolean interpolate) {
		List<GeoPosition> geosites = new ArrayList<GeoPosition>();
		for(Site s : siteList) {
			geosites.add(s.pos[4]);
		}
		Set<GeoPosition> geoSet = new HashSet<GeoPosition>(geosites);
		GeoBounds g = new GeoBounds(geoSet);
		
		ClosestPair.Pair p = ClosestPair.divideAndConquer(siteList);
		double min = p.distance;
		min = Math.max(min, .3125);
		
		GeoPosition TL = new GeoPosition(g.getNorthWest().getLongitude() - min/2, g.getNorthWest().getLatitude() + min/2);
		GeoPosition BR = new GeoPosition(g.getSouthEast().getLongitude() + min/2, g.getSouthEast().getLatitude() - min/2);
		int cellsHeight = (int) Math.ceil(((TL.getLatitude() - BR.getLatitude())/(min)));
		int cellsWidth = (int) Math.ceil(((BR.getLongitude() - TL.getLongitude())/(min)));
		
		List<Site> raster = new ArrayList<Site>(cellsHeight*cellsWidth);
		for(int i=0; i<cellsHeight; i++) {
			for(int j=0; j<cellsWidth; j++) {
				GeoPosition center = new GeoPosition(g.getNorthWest().getLongitude()-i*min, g.getNorthWest().getLatitude()+j*min);
				Site n = new Site(center, min);
				double num=0;
				double den=0;
				for(Site s : siteList) {
					double w = 1/Math.pow(ClosestPair.distance(n, s), 16);
					num += w*s.respValues.get(0);
					den += w;
				}
				n.respValues.add(num/den);
				raster.add(n);
			}
		}
		
		//for(Site s : siteList) {
			//s.setSize(min);
			//if(s.P_id == p.point1.P_id) {
			//	p.point1.setSize(.1);
			//}
		//}
		return raster;
	}
}
