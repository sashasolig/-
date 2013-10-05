package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class DeletedObject {
	
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField(index = true)
	public int ServerId;
	
	@DatabaseField(index = true)
	public int ClientId;
	
	@DatabaseField(index = true)
	public int TypeId;
			
	@DatabaseField
	public String Name;

}
