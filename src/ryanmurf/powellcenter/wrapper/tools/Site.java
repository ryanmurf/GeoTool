package ryanmurf.powellcenter.wrapper.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.JXMapViewer;
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
		Point2D ptBR = map.getTileFactory().geoToPixel(pos[3], map.getZoom());
		int sizeX = (int) Math.round(ptBR.getX() - ptUL.getX());
		int sizeY = (int) Math.round(ptBR.getY() - ptUL.getY());
		this.setLocation((int) Math.round(ptUL.getX()), (int) Math.round(ptUL.getY()));
		this.setSize(sizeX, sizeY);
	}

	public void draw(Graphics2D g, LinearGradientPaint2 paint, int valueIndex) {
		if(draw) {
			float respValue = respValues.get(valueIndex).floatValue();
			Color o = paint.getColorAt(paint.getFraction(respValue));
			Color c = new Color(o.getRed(), o.getGreen(), o.getBlue(), 210);
			
			g.setColor(c);
			g.fill(this);
			if(border) {
				g.setColor(Color.black);
				g.draw(this);
			} else {
				g.setStroke(new BasicStroke(1f));
				c = new Color(o.getRed(), o.getGreen(), o.getBlue(), 255);
				g.setColor(c);
				g.draw(this);
			}
		} else {
			if(border) {
				g.setColor(Color.black);
				g.draw(this);
			}
		}
	}
}
