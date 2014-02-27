package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Place extends BaseDTO {
		
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
    
}
