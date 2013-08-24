package com.coolchoice.monumentphoto.data;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

@DatabaseTable
public class GravePhoto extends BaseDTO
{
	public static final int STATUS_FORMATE = 0;	
	public static final int STATUS_WAIT_SEND = 1;
	public static final int STATUS_SEND = 2;
	
	public static final String STATUS_FIELD_NAME = "Status";
	
	public Monument Monument;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Grave Grave;
			
	@DatabaseField
	public Date CreateDate;
	
	@DatabaseField
	public String UriString;
	
	@DatabaseField
	public double Latitude;
	
	@DatabaseField
	public double Longitude;
	
	@DatabaseField
	public int Status;
	
}



