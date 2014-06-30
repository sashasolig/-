package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Row extends BaseDTO {
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Region Region;
	
	@ForeignCollectionField
	public Collection<GPSRow> GPSRowList;
	
	@Override
    public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation); 
        logger.info(String.format("Region:%d", this.Region != null ? this.Region.Id : INT_NULL_VALUE));
    }

}
