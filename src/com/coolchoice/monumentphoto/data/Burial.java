package com.coolchoice.monumentphoto.data;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Burial extends BaseDTO {
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Grave Grave;
	
	@DatabaseField
	public Date FactDate;
	
	@DatabaseField
	public String FName;
	
	@DatabaseField
	public String MName;
	
	@DatabaseField
	public String LName;
	
	@DatabaseField
	public long DeadManId;	

}
