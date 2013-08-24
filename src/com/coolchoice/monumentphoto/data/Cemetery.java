package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Cemetery extends BaseDTO {
	
	@ForeignCollectionField
	public Collection<GPSCemetery> GPSCemeteryList;

}
