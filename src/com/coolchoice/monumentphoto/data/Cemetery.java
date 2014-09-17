package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Cemetery extends BaseDTO {
	
	@ForeignCollectionField
	public Collection<GPSCemetery> GPSCemeteryList;
	
	@DatabaseField
	public Date RegionSyncDate;
	
	@DatabaseField
	public byte IsGPSChanged;
	
	@DatabaseField
	public Double Square;
	
	@DatabaseField
	public int OrgId;
	
	public Cemetery(){
		this.OrgId = BaseDTO.INT_NULL_VALUE;
	}
	
	@Override
	public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation);                
    }

}
