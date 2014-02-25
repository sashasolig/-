package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlacePhoto extends Photo {

    @DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
    public Place Place;
}
