package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;

public class GPS {
	
	public GPS() {
	}

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	/**
	 * Широта.
	 */
	@DatabaseField
	public double Latitude;
	
	/**
	 * Долгота.
	 */
	@DatabaseField
	public double Longitude;
	

}
