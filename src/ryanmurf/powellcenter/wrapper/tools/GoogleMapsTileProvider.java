package ryanmurf.powellcenter.wrapper.tools;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class GoogleMapsTileProvider {
	//private static final String VERSION = "2.75";
	private static final int minZoom = 1;
	private static final int maxZoom = 16;
	private static final int mapZoom = 17;
	private static final int tileSize = 256;
	private static final boolean xr2l = true;
	private static final boolean yt2b = true;
	private static final String baseURL = "http://mt0.google.com/vt/x=[x]&y=[y]&z=[z]";
	private static final String x = "x";
	private static final String y = "y";
	private static final String z = "zoom";

	private static final TileFactoryInfo
	GOOGLE_MAPS_TILE_INFO = new TileFactoryInfo(
	minZoom, maxZoom, mapZoom, tileSize, xr2l,
	yt2b, baseURL, x, y, z);

	public static TileFactory getDefaultTileFactory()
	{
		return (new DefaultTileFactory(GOOGLE_MAPS_TILE_INFO));
	}
}
