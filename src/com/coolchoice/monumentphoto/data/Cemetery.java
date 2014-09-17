package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Cemetery extends BaseDTO {
	
	public static final String ORG_ID_COLUMN = "OrgId";
	
	@ForeignCollectionField
	public Collection<GPSCemetery> GPSCemeteryList;
	
	@DatabaseField
	public Date RegionSyncDate;
	
	@DatabaseField
	public byte IsGPSChanged;
	
	@DatabaseField
	public Double Square;
	
	@DatabaseField(columnName = ORG_ID_COLUMN)
	public int OrgId;
	
	public Cemetery(){
		this.OrgId = BaseDTO.INT_NULL_VALUE;
	}
	
	@Override
	public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation);                
    }

}
