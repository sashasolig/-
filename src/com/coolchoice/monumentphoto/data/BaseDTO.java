package com.coolchoice.monumentphoto.data;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class BaseDTO {
	
	public static final String COLUMN_NAME = "Name";
	
	public static final String COLUMN_ID = "Id";
	
	public static final String COLUMN_SERVER_ID = "ServerId";
	
	public static final String COLUMN_PARENT_SERVER_ID = "ParentServerId";
	
	public static final int INT_NULL_VALUE = Integer.MIN_VALUE;;
	
	public BaseDTO() {
		this.ServerId = INT_NULL_VALUE;
		this.ParentServerId = INT_NULL_VALUE;
		this.IsChanged = 0;		
	}

	@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
	public int Id;
	
	@DatabaseField(index = true)
	public int ServerId;
	
	@DatabaseField(index = true)
	public int ParentServerId;
	
	@DatabaseField
	public String Name;
	
	@DatabaseField
	public byte IsChanged;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Name == null) ? 0 : Name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseDTO other = (BaseDTO) obj;
		if (Name == null) {
			if (other.Name != null)
				return false;
		} else if (!Name.equals(other.Name))
			return false;
		return true;
	}
	
}
