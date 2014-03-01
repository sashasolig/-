package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ResponsibleUser {
	
	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField
	public int ServerId;
	
	@DatabaseField
	public String LastName;
	
	@DatabaseField
	public String FirstName;
	
	@DatabaseField
	public String MiddleName;
	
	@DatabaseField
	public String Phones;
	
	@DatabaseField
	public String LoginPhone;
	
	@DatabaseField
	public String House;
	
	@DatabaseField
	public String Block;
	
	@DatabaseField
	public String Building;
	
	@DatabaseField
	public String Flat;
	
	@DatabaseField
	public String Country;
	
	@DatabaseField
	public String Region;
	
	@DatabaseField
	public String City;
	
	@DatabaseField
	public String Street;
	
	/*{"last_name": "", "first_name": "", "middle_name": "", "phones": "", "login_phone": null,
		"address": {"house": "12", "block": "", "building": "", "flat": "31", 
		"country": {"name": ""}, 
		"region": {"name": ""}, 
		"city": {"name": ""}, "street": {"name": ""}}}*/

}
