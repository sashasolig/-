package com.coolchoice.monumentphoto.data;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Collection;
import java.util.Date;

@DatabaseTable
public class Monument extends BaseDTO
{
	public static final int STATUS_FORMATE = 0;	
	public static final int STATUS_WAIT_SEND = 1;
	public static final int STATUS_SEND = 2;
	
	public static final String STATUS_FIELD_NAME = "Status";
	
	@ForeignCollectionField
	public Collection<GravePhoto> Photos;
	
	@DatabaseField
	public Date CreateDate;
	
	@DatabaseField
	public String CemeteryName;
	
	@DatabaseField
	public String Region;
	
	@DatabaseField
	public String Row;
	
	@DatabaseField
	public String Place;
	
	@DatabaseField
	public String Grave;
	
	@DatabaseField
	public boolean IsOwnerLess;
	
	@DatabaseField
	public int Status;
	
	
}



