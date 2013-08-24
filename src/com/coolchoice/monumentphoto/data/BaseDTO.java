package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;

public class BaseDTO {
	
	public static final String COLUMN_NAME = "Name";
	
	public static final int INT_NULL_VALUE = Integer.MIN_VALUE;;
	
	public BaseDTO() {
		this.ServerId = INT_NULL_VALUE;
		this.ParentServerId = INT_NULL_VALUE;
		this.IsChanged = 0;
		this.IsSync = 0;
	}

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField(index = true)
	public int ServerId;
	
	@DatabaseField(index = true)
	public int ParentServerId;
	
	@DatabaseField
	public String Name;
	
	@DatabaseField
	public byte IsChanged;
	
	@DatabaseField
	public byte IsSync;

}
