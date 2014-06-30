package com.coolchoice.monumentphoto.data;

import org.apache.log4j.Logger;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlacePhoto extends Photo {

    @DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public Place Place;
    
    @Override
    public void toLog(Logger logger, LogOperation operation){
        super.toLog(logger, operation);       
        logger.info(String.format("Place=%d, CreateDate=%s, UriString=%s, ThumbnailUriString=%s, FileName=%s, ServerFileName=%s, " +
        		"Latitude=%f, Longitude=%f, Status=%d", this.Place != null ? this.Place.Id : INT_NULL_VALUE,
                this.CreateDate != null ? this.CreateDate.toGMTString() : NULL, this.UriString, this.ThumbnailUriString, 
                this.FileName, this.ServerFileName, this.Latitude, this.Longitude, this.Status));        
    }
   

}
