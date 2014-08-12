package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Place extends BaseDTO {
	
	public static final String ROW_ID_COLUMN = "Row_id";
	
	public static final String REGION_ID_COLUMN = "Region_id";
    
    public static final String PLACE_LOGGER_PATTERN = "Region=%d, Row=%d, OldName=%s, IsOwnerLess=%b, Width=%f, Length=%f," +
            "SizeViolatedDate=%s, UnindentifiedDate=%s, UnownedDate=%s, MilitaryDate=%s, WrongFIODate=%s";
		
	@DatabaseField
    public Date SizeViolatedDate;

    @DatabaseField
    public Date UnindentifiedDate;
    
    @DatabaseField
    public Date UnownedDate;
	
	@DatabaseField
    public Date MilitaryDate;

    @DatabaseField
    public Date WrongFIODate;
	
	@DatabaseField
	public Double Width;
	
	@DatabaseField
	public Double Length;
	
	@DatabaseField
	public boolean IsOwnerLess;
	
	@DatabaseField
	public String OldName;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Row Row;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Region Region;
	
	@ForeignCollectionField
	public Collection<GPSPlace> GPSPlaceList;
	
	@ForeignCollectionField
    public Collection<PlacePhoto> Photos;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public ResponsibleUser ResponsibleUser;
		
	public boolean isSizeViolated(){
        return this.SizeViolatedDate != null ? true : false;
    }

    public boolean isUnindentified(){
        return this.UnindentifiedDate != null ? true : false;
    }
    
    public boolean isUnowned(){
        return this.UnownedDate != null ? true : false;
    }    
	
	public boolean isMilitary(){
	    return this.MilitaryDate != null ? true : false;
	}

    public boolean isWrongFIO(){
        return this.WrongFIODate != null ? true : false;
    }
    
    @Override
    public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation); 
        logger.info(String.format(PLACE_LOGGER_PATTERN, this.Region != null ? this.Region.Id : INT_NULL_VALUE, this.Row != null ? this.Row.Id : INT_NULL_VALUE,
                this.OldName, this.IsOwnerLess, this.Width, this.Length,
                this.SizeViolatedDate != null ? this.SizeViolatedDate.toGMTString() : NULL, this.UnindentifiedDate != null ? this.UnindentifiedDate.toGMTString() : NULL,
                this.UnownedDate != null ? this.UnownedDate.toGMTString() : NULL, this.MilitaryDate != null ? this.MilitaryDate.toGMTString() : NULL,
                this.WrongFIODate != null ? this.WrongFIODate.toGMTString() : NULL));        
    }
    
}
