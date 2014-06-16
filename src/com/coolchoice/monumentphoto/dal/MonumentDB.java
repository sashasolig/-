package com.coolchoice.monumentphoto.dal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.DeletedObject;
import com.coolchoice.monumentphoto.data.DeletedObjectType;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Photo;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.PlacePhoto;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class MonumentDB {
	
	public static void deleteGravePhoto(GravePhoto gravePhoto){
		DB.dao(GravePhoto.class).refresh(gravePhoto);
		if(gravePhoto.ServerId > 0){
			DeletedObject deletedObject = new DeletedObject();
			deletedObject.ServerId = gravePhoto.ServerId;
			deletedObject.ClientId = gravePhoto.Id;
			deletedObject.TypeId = DeletedObjectType.GRAVEPHOTO;
			DB.dao(DeletedObject.class).create(deletedObject);
		}
		DB.dao(GravePhoto.class).delete(gravePhoto);
	}
	
	public static void deletePlacePhoto(PlacePhoto placePhoto){
        DB.dao(PlacePhoto.class).refresh(placePhoto);
        if(placePhoto.ServerId > 0){
            DeletedObject deletedObject = new DeletedObject();
            deletedObject.ServerId = placePhoto.ServerId;
            deletedObject.ClientId = placePhoto.Id;
            deletedObject.TypeId = DeletedObjectType.PLACEPHOTO;
            DB.dao(DeletedObject.class).create(deletedObject);
        }
        DB.dao(PlacePhoto.class).delete(placePhoto);
    }
	
	public static ArrayList<Photo> getPhotos(Place place){
	    ArrayList<Photo> photoList = new ArrayList<Photo>();
        for(PlacePhoto photo : place.Photos){                   
            photoList.add(photo);                    
        }
        List<Grave> graves = DB.dao(Grave.class).queryForEq("Place_id", place.Id);
        for(Grave g : graves){
            DB.dao(Grave.class).refresh(g);
            for(GravePhoto photo : g.Photos){
                photoList.add(photo);
            }
        }
        Collections.sort(photoList, new Comparator<Photo>() {
            @Override
            public int compare(Photo one, Photo two) {
                return two.CreateDate.compareTo(one.CreateDate);
            }
        });  
        return photoList;
    }
	
	public List<ComplexGrave.PlaceWithFIO> getPlaceWithFIO(int cemeteryId, String filterLastName){
		try {
			String utf8String = new String(filterLastName.getBytes(), "UTF-8");
			filterLastName = utf8String;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String selectQuery = String.format(selectPlaceByName1, cemeteryId, filterLastName);
		List<ComplexGrave.PlaceWithFIO> result1 = getPlaceWithFIOByQuery(selectQuery);
		selectQuery = String.format(selectPlaceByName2, cemeteryId, filterLastName);
		List<ComplexGrave.PlaceWithFIO> result2 = getPlaceWithFIOByQuery(selectQuery);
		result1.addAll(result2);
		return result1;
	}
	
	private List<ComplexGrave.PlaceWithFIO> getPlaceWithFIOByQuery(String selectQuery){
		List<ComplexGrave.PlaceWithFIO> listResults = new ArrayList<ComplexGrave.PlaceWithFIO>(); 
		try {
			RuntimeExceptionDao<Burial, Integer> dao = DB.db().dao(Burial.class);
			GenericRawResults<String[]> rawResults = dao.queryRaw(selectQuery);
			List<String[]> results = rawResults.getResults();
			if(results != null && results.size() > 0){
				for(String[] row : results){					
					ComplexGrave.PlaceWithFIO obj = new ComplexGrave.PlaceWithFIO();					
					obj.FName = row[0];
					obj.MName = row[1];
					obj.LName = row[2];
					obj.PlaceName = row[3];
					obj.OldPlaceName = row[4];
					obj.PlaceId = Integer.parseInt(row[5]);
					listResults.add(obj);
				}
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listResults;
	}
		
	//search Place by name
	private String selectPlaceByName1 = "select distinct b.FName, b.MName, b.LName, p.Name, p.OldName, p.Id from burial b, grave g, place p, row row, region reg, cemetery c where b.Grave_id = g.id and g.Place_id = p.id and p.Row_id = row.id and row.Region_id = reg.Id and reg.Cemetery_id = c.Id and c.Id = %d and b.LName like '%s%%' ORDER BY b.LName LIMIT 20 OFFSET 0";
	
	private String selectPlaceByName2 = "select distinct b.FName, b.MName, b.LName, p.Name, p.OldName, p.Id from burial b, grave g, place p, region reg, cemetery c where b.Grave_id = g.id and g.Place_id = p.id and p.Region_id = reg.Id and reg.Cemetery_id = c.Id and c.Id = %d and b.LName like '%s%%' ORDER BY b.LName LIMIT 20 OFFSET 0 ";
	
	public void updateGravePhotoUriString(String oldPartName, String newPartName){
		String updateQuery = "update gravephoto set UriString = replace(UriString, '%s', '%s'), ThumbnailUriString = replace(ThumbnailUriString, '%s', '%s');";
		updateQuery = String.format(updateQuery, oldPartName, newPartName, oldPartName, newPartName);
		RuntimeExceptionDao<GravePhoto, Integer> gravePhotoDAO = DB.db().dao(GravePhoto.class);
		gravePhotoDAO.updateRaw(updateQuery);
		
		updateQuery = "update placephoto set UriString = replace(UriString, '%s', '%s'), ThumbnailUriString = replace(ThumbnailUriString, '%s', '%s');";
        updateQuery = String.format(updateQuery, oldPartName, newPartName, oldPartName, newPartName);
        RuntimeExceptionDao<PlacePhoto, Integer> placePhotoDAO = DB.db().dao(PlacePhoto.class);
        placePhotoDAO.updateRaw(updateQuery);
        
		
	}
	 
	public static void deleteCemetery(int cemeteryId){
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByCemeteryId(cemeteryId);
		File photoFolder = complexGrave.getPhotoFolder();
		if(photoFolder != null){
			deleteFolder(photoFolder);
		}
		DB.dao(Cemetery.class).deleteById(cemeteryId);
		deleteNonLinkedRegion();
		deleteNonLinkedRow();
		deleteNonLinkedPlace();
		deleteNonLinkedGrave();
		deleteNonLinkedGravePhoto();
	}
	
	public static void deleteRegion(int regionId){
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByRegionId(regionId);
		File photoFolder = complexGrave.getPhotoFolder();
		if(photoFolder != null){
			deleteFolder(photoFolder);
		}
		
		DB.dao(Region.class).deleteById(regionId);
		deleteNonLinkedRow();
		deleteNonLinkedPlace();
		deleteNonLinkedGrave();
		deleteNonLinkedGravePhoto();
	}
	
	public static void deleteRow(int rowId){
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByRowId(rowId);
		File photoFolder = complexGrave.getPhotoFolder();
		if(photoFolder != null){
			deleteFolder(photoFolder);
		}
		
		DB.dao(Row.class).deleteById(rowId);
		deleteNonLinkedPlace();
		deleteNonLinkedGrave();
		deleteNonLinkedGravePhoto();
	}
	
	public static void deletePlace(int placeId){
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByPlaceId(placeId);
		File photoFolder = complexGrave.getPhotoFolder();
		if(photoFolder != null){
			deleteFolder(photoFolder);
		}
		
		DB.dao(Place.class).deleteById(placeId);
		deleteNonLinkedGrave();
		deleteNonLinkedGravePhoto();
	}
	
	public static void deleteGrave(int graveId){
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByGraveId(graveId);
		File photoFolder = complexGrave.getPhotoFolder();
		if(photoFolder != null){
			deleteFolder(photoFolder);
		}
		
		DB.dao(Grave.class).deleteById(graveId);
		deleteNonLinkedGravePhoto();
	}
	
	private static void deleteNonLinkedRegion(){
		DB.db().execManualSQL("delete from region where not exists(select * from cemetery where cemetery.Id = region.Cemetery_id);");
	}
	
	private static void deleteNonLinkedRow(){
		DB.db().execManualSQL("delete from row where not exists(select * from region where region.Id = row.Region_id);");
	}
	
	private static void deleteNonLinkedPlace(){
		DB.db().execManualSQL("delete from place where not exists(select * from row where row.Id = place.Row_id) and not exists(select * from region where region.Id = place.Region_id);");
	}
	
	private static void deleteNonLinkedGrave(){
		DB.db().execManualSQL("delete from grave where not exists(select * from place where place.Id = grave.Place_id);");
	}
	
	private static void deleteNonLinkedGravePhoto(){
		DB.db().execManualSQL("delete from gravephoto where not exists(select * from grave where grave.Id = gravephoto.Grave_id);");
	}
	
	private static void deleteFolder(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory()){
	        for (File child : fileOrDirectory.listFiles()){
	            deleteFolder(child);
	        }
	    }
	    fileOrDirectory.delete();
	}
}
