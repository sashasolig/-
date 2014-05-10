package com.coolchoice.monumentphoto.util;

import java.text.DecimalFormat;
import java.util.List;

import android.location.Location;

import com.coolchoice.monumentphoto.data.GPS;

public class FindingSquare {
	
	private static DecimalFormat mSquareDecimalFormat = new DecimalFormat("##0.00");
	
	static class Point{	
		public double x;
		public double y;
		
		Point(double x, double y){
			this.x = x;
			this.y = y;
		}
	}
	
	public static Double getSquare(List<GPS> polygonGPS){	
		Point[] polygon = new Point[polygonGPS.size()];
		double minLat = polygonGPS.get(0).Latitude;
		double minLng = polygonGPS.get(0).Longitude;
		for(GPS gps : polygonGPS){
			if(gps.Latitude < minLat){
				minLat = gps.Latitude;
			}
			if(gps.Longitude < minLng){
				minLng = gps.Longitude;
			}
		}
		float[] results = new float[3];		
		int i = 0;
		for(GPS gps : polygonGPS){
			double x, y;
			results = new float[3];
			Location.distanceBetween(gps.Latitude, gps.Longitude, gps.Latitude, minLng, results);
			x = results[0];
			results = new float[3];
			Location.distanceBetween(gps.Latitude, gps.Longitude, minLat, gps.Longitude, results);
			y = results[0];
			Point point = new Point(x,y);
			polygon[i] = point;
			i++;			
		}
		double square = 0;
		for(i = 0; i < polygon.length; i++){
			Point p1 = polygon[i];
			Point p2 = polygon[((i+1) == polygon.length ) ? 0 : (i+1)];
			double result  = (p1.x-p2.x)*(p1.y + p2.y)/2;
			square = square + result;
			
		}
		square = Math.abs(square);
		square = Double.valueOf(mSquareDecimalFormat.format(square));
		return square;
	}

}
