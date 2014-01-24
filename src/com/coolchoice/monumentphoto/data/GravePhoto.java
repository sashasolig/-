package com.coolchoice.monumentphoto.data;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

@DatabaseTable
public class GravePhoto extends BaseDTO
{
	public static final int STATUS_FORMATE = 0;	
	public static final int STATUS_WAIT_SEND = 1;
	public static final int STATUS_SEND = 2;
	
	public static final String STATUS_FIELD_NAME = "Status";
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Grave Grave;
			
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(Latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(Longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((UriString == null) ? 0 : UriString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GravePhoto other = (GravePhoto) obj;
		if (Double.doubleToLongBits(Latitude) != Double
				.doubleToLongBits(other.Latitude))
			return false;
		if (Double.doubleToLongBits(Longitude) != Double
				.doubleToLongBits(other.Longitude))
			return false;
		if (UriString == null) {
			if (other.UriString != null)
				return false;
		} else if (!UriString.equals(other.UriString))
			return false;
		return true;
	}
	
}



