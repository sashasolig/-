package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;

public class GPS {
	
	public static final String ORDINAL_COLUMN = "OrdinalNumber";
	
	public GPS() {
	}

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField
	public int ServerId;
	
	@DatabaseField(canBeNull = false)
	public double Latitude;
	
	@DatabaseField(canBeNull = false)
	public double Longitude;
	
	@DatabaseField(canBeNull = false)
	public int OrdinalNumber;
	

}
