package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Grave extends BaseDTO {
	
	@DatabaseField
	public boolean IsMilitary;

	@DatabaseField
	public boolean IsWrongFIO;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Place Place;
		
	@ForeignCollectionField
	public Collection<GPSGrave> GPSGraveList;
	
	@ForeignCollectionField
	public Collection<Burial> BurialList;
	
	@Override
    public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation); 
        logger.info(String.format("Place=%d, IsMilitary=%b, IsWrongFIO=%b", this.Place != null ? this.Place.Id : INT_NULL_VALUE,
                this.IsMilitary, this.IsWrongFIO));        
    }

}
