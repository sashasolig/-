package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Place extends BaseDTO {
	
	public static final String IsOwnerLessColumnName = "IsOwnerLess";
	
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
	
	public Place createClone(){
		Place newPlace = new Place();
		newPlace.GPSPlaceList = this.GPSPlaceList;
		newPlace.Id = this.Id;
		newPlace.IsChanged = this.IsChanged;
		newPlace.IsOwnerLess = this.IsOwnerLess;
		newPlace.Length = this.Length;
		newPlace.Name = this.Name;
		newPlace.OldName = this.OldName;
		newPlace.ParentServerId = this.ParentServerId;
		newPlace.Region = this.Region;
		newPlace.Row = this.Row;
		newPlace.ServerId = this.ServerId;
		newPlace.Width = this.Width;
		return newPlace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (IsOwnerLess ? 1231 : 1237);
		result = prime * result + ((Length == null) ? 0 : Length.hashCode());
		result = prime * result + ((OldName == null) ? 0 : OldName.hashCode());
		result = prime * result + ((Region == null) ? 0 : Region.hashCode());
		result = prime * result + ((Row == null) ? 0 : Row.hashCode());
		result = prime * result + ((Width == null) ? 0 : Width.hashCode());
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
		Place other = (Place) obj;
		if (IsOwnerLess != other.IsOwnerLess)
			return false;
		if (Length == null) {
			if (other.Length != null)
				return false;
		} else if (!Length.equals(other.Length))
			return false;
		if (OldName == null) {
			if (other.OldName != null)
				return false;
		} else if (!OldName.equals(other.OldName))
			return false;
		if (Region == null) {
			if (other.Region != null)
				return false;
		} else if (!Region.equals(other.Region))
			return false;
		if (Row == null) {
			if (other.Row != null)
				return false;
		} else if (!Row.equals(other.Row))
			return false;
		if (Width == null) {
			if (other.Width != null)
				return false;
		} else if (!Width.equals(other.Width))
			return false;
		return true;
	}	

}
