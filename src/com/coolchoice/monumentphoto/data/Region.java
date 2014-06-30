package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.coolchoice.monumentphoto.data.ILogable.LogOperation;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Region extends BaseDTO {
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Cemetery Cemetery;
	
	@ForeignCollectionField
	public Collection<GPSRegion> GPSRegionList;
	
	@DatabaseField
	public Date PlaceSyncDate;
	
	@DatabaseField
	public Date GraveSyncDate;
	
	@DatabaseField
	public Date BurialSyncDate;
	
	@DatabaseField
	public byte IsGPSChanged;
	
	@DatabaseField
	public Double Square;
	
	@Override
    public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation); 
        logger.info(String.format("Cemetery:%d", this.Cemetery != null ? this.Cemetery.Id : INT_NULL_VALUE));
    }

}
