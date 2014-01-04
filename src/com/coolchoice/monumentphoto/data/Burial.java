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
	
	public void toLowerCaseFIO(){
		if(this.FName != null){
			this.FName = this.FName.toLowerCase();
		}
		if(this.LName != null){
			this.LName = this.LName.toLowerCase();
		}
		if(this.MName != null){
			this.MName = this.MName.toLowerCase();
		}
	}
	
	public void toUpperFirstCharacterInFIO(){
		if(this.FName != null && this.FName.length() > 0){
			if(this.FName.length() == 1){
				this.FName = this.FName.toUpperCase();
			} else {
				this.FName = this.FName.substring(0, 1).toUpperCase() + this.FName.substring(1);
			}
		}
		if(this.LName != null && this.LName.length() > 0){
			if(this.LName.length() == 1){
				this.LName = this.LName.toUpperCase();
			} else {
				this.LName = this.LName.substring(0, 1).toUpperCase() + this.LName.substring(1);
			}
		}
		if(this.MName != null && this.MName.length() > 0){
			if(this.MName.length() == 1){
				this.MName = this.MName.toUpperCase();
			} else {
				this.MName = this.MName.substring(0, 1).toUpperCase() + this.MName.substring(1);
			}
		}
	}

}
