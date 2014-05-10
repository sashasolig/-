package com.coolchoice.monumentphoto.map;

import com.coolchoice.monumentphoto.data.GPS;

import android.graphics.drawable.Drawable;
import ru.yandex.yandexmapkit.overlay.drag.DragAndDropItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;

public class CustomDragAndDropItem extends DragAndDropItem {
	
	private GPS gps;

	public GPS getGps() {
		return gps;
	}

	public void setGps(GPS gps) {
		this.gps = gps;
	}

	public CustomDragAndDropItem(GeoPoint point, Drawable drawable, GPS gps) {
		super(point, drawable);
		this.gps = gps;		
	}
	
	public void syncGPSWithGeoPoint(){
		if(this.gps != null){
			this.gps.Latitude = getGeoPoint().getLat();
			this.gps.Longitude = getGeoPoint().getLon();
		}
	}

}
