package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Grave extends BaseDTO {
	
	public static final String IsMilitaryColumnName = "IsMilitary";
	
	public static final String IsWrongFIOColumnName = "IsWrongFIO";
	
	@DatabaseField
	public boolean IsMilitary;

	@DatabaseField
	public boolean IsWrongFIO;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Place Place;
	
	@ForeignCollectionField
	public Collection<GravePhoto> Photos;
	
	@ForeignCollectionField
	public Collection<GPSGrave> GPSGraveList;
	
	@ForeignCollectionField
	public Collection<Burial> BurialList;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (IsMilitary ? 1231 : 1237);
		result = prime * result + (IsWrongFIO ? 1231 : 1237);
		result = prime * result + ((Place == null) ? 0 : Place.hashCode());
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
		Grave other = (Grave) obj;
		if (IsMilitary != other.IsMilitary)
			return false;
		if (IsWrongFIO != other.IsWrongFIO)
			return false;
		if (Place == null) {
			if (other.Place != null)
				return false;
		} else if (!Place.equals(other.Place))
			return false;
		return true;
	}

}
