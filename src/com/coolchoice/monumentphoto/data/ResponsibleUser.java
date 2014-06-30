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
	
	public String getFIO(){
        return String.format("%s %s %s", this.LastName != null ? this.LastName : "",
                this.FirstName != null ? this.FirstName : "",
                this.MiddleName != null ? this.MiddleName : "");
    }

}
