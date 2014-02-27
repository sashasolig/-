package com.coolchoice.monumentphoto.data;
import com.j256.ormlite.field.DatabaseField;
import java.util.Date;

public class Photo extends BaseDTO
{
    public static final int STATUS_FORMATE = 0; 
    public static final int STATUS_WAIT_SEND = 1;
    public static final int STATUS_SEND = 2;
    
    public static final String STATUS_FIELD_NAME = "Status";
                    
    @DatabaseField
    public Date CreateDate;
    
    @DatabaseField
    public String UriString;
    
    @DatabaseField
    public double Latitude;
    
    @DatabaseField
    public double Longitude;
    
    @DatabaseField
    public int Status;
    
}



