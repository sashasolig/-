package com.coolchoice.monumentphoto.data;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Burial extends BaseDTO {
    
    public static String STATUS_COLUMN_NAME = "Status";
    
    public static String PLANDATE_COLUMN_NAME = "PlanDate";
    
    public enum ContainerTypeEnum {
        CONTAINER_COFFIN("Гроб"),
        CONTAINER_URN("Урна"),
        CONTAINER_ASH("Прах"),
        CONTAINER_BIO("Биоотходы");
        String value;
        ContainerTypeEnum(String s) {
            value = s;
        }
        
        public static ContainerTypeEnum getEnum(String value) {
            if(value == null){
                return null;
            }
            return ContainerTypeEnum.valueOf(value.toUpperCase());
        }
        
        @Override
        public String toString(){
            return this.value;
        }
    }
    
    public enum StatusEnum {        
        BACKED("Отозвано"),
        DECLINED("Отклонено"),
        DRAFT("Черновик"),
        READY("На согласовании"),
        INSPECTING("На обследовании"),
        APPROVED("Согласовано"),
        CLOSED("Закрыто"),
        EXHUMATED("Эксгумировано");         
        String value;
        StatusEnum(String s) {
            value = s;
        }
        
        public static StatusEnum getEnum(String value) {
            if(value == null){
                return null;
            }
            return StatusEnum.valueOf(value.toUpperCase());
        }
        
        @Override
        public String toString(){
            return this.value;
        }        
        
    }
	
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
	
	@DatabaseField(dataType = DataType.ENUM_INTEGER)
    public ContainerTypeEnum ContainerType;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public Cemetery Cemetery;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public Region Region;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public Place Place;
	
	@DatabaseField
    public String Row;
	
	@DatabaseField
    public Date PlanDate;
	
	@DatabaseField(dataType = DataType.ENUM_INTEGER)
    public StatusEnum Status;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public ResponsibleUser ResponsibleUser;
	
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
	
	public String getFIO(){
	    return String.format("%s %s %s", this.LName != null ? this.LName : "",
	            this.FName != null ? this.FName : "",
	            this.MName != null ? this.MName : "");
	}
	
}
