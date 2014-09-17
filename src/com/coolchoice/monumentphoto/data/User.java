package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class User extends BaseDTO {
		
	public static final String USER_NAME_COLUMN = "UserName";
	
	public static final String IS_ACTIVE_COLUMN = "IsActive";
	
	@DatabaseField(columnName=USER_NAME_COLUMN)
	public String UserName;
	
	@DatabaseField
	public String FName;
	
	@DatabaseField
	public String LName;
	
	@DatabaseField
	public String MName;
	
	@DatabaseField
	public int OrgId;
	
	@DatabaseField(columnName=IS_ACTIVE_COLUMN)
	public int IsActive;
	
	@Override
	public String toString(){
		return String.format("%s %s %s", LName != null ? LName : "", FName != null ? FName : "", MName != null ? MName : "");
	}
	
}
