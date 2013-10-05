package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;

public class GPS {
	
	public GPS() {
	}

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField
	public double Latitude;
	
	@DatabaseField
	public double Longitude;
	

}
