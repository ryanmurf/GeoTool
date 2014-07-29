package ryanmurf.powellcenter.wrapper.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import ryanmurf.powellcenter.wrapper.tools.Site;

public class ClosestPair {
	
	public static class Pair {
		public Site point1 = null;
		public Site point2 = null;
		public double distance = 0.0;

		public Pair() {
		}

		public Pair(Site point1, Site point2) {
			this.point1 = point1;
			this.point2 = point2;
			calcDistance();
		}

		public void update(Site point1, Site point2, double distance) {
			this.point1 = point1;
			this.point2 = point2;
			this.distance = distance;
		}

		public void calcDistance() {
			this.distance = distance(point1, point2);
		}

		public String toString() {
			return point1 + "-" + point2 + " : " + distance;
		}
	}

	public static double distance(Site p1, Site p2) {
		double xdist = p2.getLongitude() - p1.getLongitude();
		double ydist = p2.getLatitude() - p1.getLatitude();
		return Math.hypot(xdist, ydist);
	}

	public static Pair bruteForce(List<? extends Site> points) {
		int numSites = points.size();
		if (numSites < 2)
			return null;
		Pair pair = new Pair(points.get(0), points.get(1));
		if (numSites > 2) {
			for (int i = 0; i < numSites - 1; i++) {
				Site point1 = points.get(i);
				for (int j = i + 1; j < numSites; j++) {
					Site point2 = points.get(j);
					double distance = distance(point1, point2);
					if (distance < pair.distance)
						pair.update(point1, point2, distance);
				}
			}
		}
		return pair;
	}

	public static void sortByX(List<? extends Site> points) {
		Collections.sort(points, new Comparator<Site>() {
			public int compare(Site point1, Site point2) {
				if (point1.getLongitude() < point2.getLongitude())
					return -1;
				if (point1.getLongitude() > point2.getLongitude())
					return 1;
				return 0;
			}
		});
	}

	public static void sortByY(List<? extends Site> points) {
		Collections.sort(points, new Comparator<Site>() {
			public int compare(Site point1, Site point2) {
				if (point1.getLatitude() < point2.getLatitude())
					return -1;
				if (point1.getLatitude() > point2.getLatitude())
					return 1;
				return 0;
			}
		});
	}

	public static Pair divideAndConquer(List<? extends Site> points) {
		List<Site> pointsSortedByX = new ArrayList<Site>(points);
		sortByX(pointsSortedByX);
		List<Site> pointsSortedByY = new ArrayList<Site>(points);
		sortByY(pointsSortedByY);
		return divideAndConquer(pointsSortedByX, pointsSortedByY);
	}

	private static Pair divideAndConquer(List<? extends Site> pointsSortedByX,
			List<? extends Site> pointsSortedByY) {
		int numSites = pointsSortedByX.size();
		if (numSites <= 3)
			return bruteForce(pointsSortedByX);

		int dividingIndex = numSites >>> 1;
		List<? extends Site> leftOfCenter = pointsSortedByX.subList(0,
				dividingIndex);
		List<? extends Site> rightOfCenter = pointsSortedByX.subList(
				dividingIndex, numSites);

		List<Site> tempList = new ArrayList<Site>(leftOfCenter);
		sortByY(tempList);
		Pair closestPair = divideAndConquer(leftOfCenter, tempList);

		tempList.clear();
		tempList.addAll(rightOfCenter);
		sortByY(tempList);
		Pair closestPairRight = divideAndConquer(rightOfCenter, tempList);

		if (closestPairRight.distance < closestPair.distance)
			closestPair = closestPairRight;

		tempList.clear();
		double shortestDistance = closestPair.distance;
		double centerX = rightOfCenter.get(0).getLongitude();
		for (Site point : pointsSortedByY)
			if (Math.abs(centerX - point.getLongitude()) < shortestDistance)
				tempList.add(point);

		for (int i = 0; i < tempList.size() - 1; i++) {
			Site point1 = tempList.get(i);
			for (int j = i + 1; j < tempList.size(); j++) {
				Site point2 = tempList.get(j);
				if ((point2.getLatitude() - point1.getLatitude()) >= shortestDistance)
					break;
				double distance = distance(point1, point2);
				if (distance < closestPair.distance) {
					closestPair.update(point1, point2, distance);
					shortestDistance = distance;
				}
			}
		}
		return closestPair;
	}

	public static void main(String[] args) {
		int numSites = (args.length == 0) ? 1000 : Integer.parseInt(args[0]);
		List<Site> points = new ArrayList<Site>();
		Random r = new Random();
		for (int i = 0; i < numSites; i++)
			points.add(new Site(new GeoPosition(r.nextDouble(), r.nextDouble())));
		System.out.println("Generated " + numSites + " random points");
		long startTime = System.currentTimeMillis();
		Pair bruteForceClosestPair = bruteForce(points);
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Brute force (" + elapsedTime + " ms): "
				+ bruteForceClosestPair);
		startTime = System.currentTimeMillis();
		Pair dqClosestPair = divideAndConquer(points);
		elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Divide and conquer (" + elapsedTime + " ms): "
				+ dqClosestPair);
		if (bruteForceClosestPair.distance != dqClosestPair.distance)
			System.out.println("MISMATCH");
	}
}
