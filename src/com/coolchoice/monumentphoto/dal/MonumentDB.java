package com.coolchoice.monumentphoto.dal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.util.Log;

import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

public class MonumentDB {
		
	public static List<Monument> getMonuments(){
		List<Monument> monuments = null;
		try {
			monuments = DB.q(Monument.class).orderBy("CreateDate", false).query();			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
		return monuments;
	}
	
	public static List<Monument> getMonumentsWithPhotos(int status){
		List<Monument> monuments = null;
		try {
			monuments = DB.q(Monument.class).orderBy("CreateDate", true).where().eq("Status", status).query();
			for(Monument monument : monuments){
				for(GravePhoto photo : monument.Photos){
					DB.dao(GravePhoto.class).refresh(photo);				
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
		return monuments;
	}
	
	public static Monument getMonumentById(int id) {		
		Monument monument = DB.dao(Monument.class).queryForId(id);
		if(monument != null){
			for(GravePhoto photo : monument.Photos){
				DB.dao(GravePhoto.class).refresh(photo);				
			}			
			
		}
		return monument;				
	}
	
	public static Monument saveMonument(Monument monument){
		for(GravePhoto photo : monument.Photos){
			DB.dao(GravePhoto.class).createOrUpdate(photo);
		}
		DB.dao(Monument.class).createOrUpdate(monument);		
		return monument;
	}
	
	public static GravePhoto saveMonumentPhoto(GravePhoto monumentPhoto){
		DB.dao(GravePhoto.class).createOrUpdate(monumentPhoto);		
		return monumentPhoto;		
	}
	
	public static void deleteMonumentPhoto(GravePhoto monumentPhoto){
		DB.dao(GravePhoto.class).delete(monumentPhoto);		
	}	
		
	public static List<GravePhoto> getGravePhotoForUpload(){
		List<GravePhoto> gravePhotos = null;
		try {
			gravePhotos = DB.q(GravePhoto.class).orderBy("CreateDate", true).where().eq(GravePhoto.STATUS_FIELD_NAME, GravePhoto.STATUS_WAIT_SEND).query();
			for(GravePhoto gravePhoto : gravePhotos) {
				DB.dao(Grave.class).refresh(gravePhoto.Grave);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}					
		return gravePhotos;
	}
	
	public static void markGravePhotoAsSended(GravePhoto gravePhoto){
		UpdateBuilder<GravePhoto, Integer> updateBuilder = DB.dao(GravePhoto.class).updateBuilder();
		try {
			updateBuilder.updateColumnValue(Monument.STATUS_FIELD_NAME, Monument.STATUS_SEND);
			updateBuilder.where().idEq(gravePhoto.Id);
			DB.dao(GravePhoto.class).update(updateBuilder.prepare());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public List<Integer> getGravePhotoIds(Cemetery cemetery){
		String selectQuery = String.format(selectGravePhotoIdsByCemetery1, cemetery.Id);
		List<Integer> result1 = getGravePhotoIds(selectQuery);
		selectQuery = String.format(selectGravePhotoIdsByCemetery2, cemetery.Id);
		List<Integer> result2 = getGravePhotoIds(selectQuery);
		result1.addAll(result2);
		return result1;
	}
	
	public List<Integer> getGravePhotoIds(Region region){
		String selectQuery = String.format(selectGravePhotoIdsByRegion1, region.Id);
		List<Integer> result1 = getGravePhotoIds(selectQuery);
		selectQuery = String.format(selectGravePhotoIdsByRegion2, region.Id);
		List<Integer> result2 = getGravePhotoIds(selectQuery);
		result1.addAll(result2);
		return result1;
	}
	
	public List<Integer> getGravePhotoIds(Row row){
		String selectQuery = String.format(selectGravePhotoIdsByRow, row.Id);
		List<Integer> result = getGravePhotoIds(selectQuery);
		return result;
	}
	
	public List<Integer> getGravePhotoIds(Place place){
		String selectQuery = String.format(selectGravePhotoIdsByPlace, place.Id);
		List<Integer> result = getGravePhotoIds(selectQuery);
		return result;
	}
	
	public List<Integer> getGravePhotoIds(Grave grave){
		String selectQuery = String.format(selectGravePhotoIdsByGrave, grave.Id);
		List<Integer> result = getGravePhotoIds(selectQuery);
		return result;
	}
	
	//Cemetery change
	private String selectGravePhotoIdsByCemetery1 = "select distinct gp.id from gravephoto gp, grave g, place p, row row, region reg, cemetery c where gp.Grave_id = g.id and g.Place_id = p.id and p.Row_id = row.id and row.Region_id = reg.Id and reg.Cemetery_id = c.Id and c.Id = %d ";

	private String selectGravePhotoIdsByCemetery2 = "select distinct gp.id from gravephoto gp, grave g, place p, region reg, cemetery c where gp.Grave_id = g.id and g.Place_id = p.id and p.Region_id = reg.Id and reg.Cemetery_id = c.Id and c.Id = %d ";

	//Region change
	private String selectGravePhotoIdsByRegion1 = "select distinct gp.id from gravephoto gp, grave g, place p, row row, region reg where gp.Grave_id = g.id and g.Place_id = p.id and p.Row_id = row.id and row.Region_id = reg.Id and reg.id = %d ";

	private String selectGravePhotoIdsByRegion2 = "select distinct gp.id from gravephoto gp, grave g, place p, region reg where gp.Grave_id = g.id and g.Place_id = p.id and p.Region_id = reg.Id and reg.Id = %d ";

	//Row change
	private String selectGravePhotoIdsByRow = "select distinct gp.id from gravephoto gp, grave g, place p, row row where gp.Grave_id = g.id and g.Place_id = p.id and p.Row_id = row.id	and row.id = %d ";

	//Place change
	private String selectGravePhotoIdsByPlace = "select distinct gp.id from gravephoto gp, grave g, place p	where gp.Grave_id = g.id and g.Place_id = p.id and p.id = %d ";

	//Grave change
	private String selectGravePhotoIdsByGrave = "select distinct gp.id from gravephoto gp, grave g where gp.Grave_id = g.id and g.id = %d ";
	
	private List<Integer> getGravePhotoIds(String selectQuery){
		List<Integer> listResults = new ArrayList<Integer>(); 
		try {
			RuntimeExceptionDao<GravePhoto, Integer> dao = DB.db().dao(GravePhoto.class);
			GenericRawResults<String[]> rawResults = dao.queryRaw(selectQuery);
			List<String[]> results = rawResults.getResults();
			if(results != null && results.size() > 0){
				for(String[] row : results){
					Integer id = Integer.parseInt(row[0]);
					listResults.add(id);
				}
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listResults;
	}
	
	public void updateGravePhotoUriString(Cemetery cemetery, String oldPartOfPath, String newPartOfPath ){
		List<Integer> gravePhotIds = getGravePhotoIds(cemetery);
		updateGravePhotoUriString(oldPartOfPath, newPartOfPath, gravePhotIds);
	}
	
	public void updateGravePhotoUriString(Region region, String oldPartOfPath, String newPartOfPath ){
		List<Integer> gravePhotIds = getGravePhotoIds(region);
		updateGravePhotoUriString(oldPartOfPath, newPartOfPath, gravePhotIds);
	}
	
	public void updateGravePhotoUriString(Row row, String oldPartOfPath, String newPartOfPath ){
		List<Integer> gravePhotIds = getGravePhotoIds(row);
		updateGravePhotoUriString(oldPartOfPath, newPartOfPath, gravePhotIds);
	}
	
	public void updateGravePhotoUriString(Place place, String oldPartOfPath, String newPartOfPath ){
		List<Integer> gravePhotIds = getGravePhotoIds(place);
		updateGravePhotoUriString(oldPartOfPath, newPartOfPath, gravePhotIds);
	}
	
	public void updateGravePhotoUriString(Grave grave, String oldPartOfPath, String newPartOfPath ){
		List<Integer> gravePhotIds = getGravePhotoIds(grave);
		updateGravePhotoUriString(oldPartOfPath, newPartOfPath, gravePhotIds);
	}
	
	public void updateGravePhotoUriString(String oldPartName, String newPartName, List<Integer> gravePhotoIds){
		if(gravePhotoIds.size() > 0){
			String updateQuery = "update gravephoto set UriString = replace(UriString, '%s', '%s') where Id in (%s) ";
			StringBuilder sbIds = new StringBuilder();
			for(int i = 0; i < gravePhotoIds.size() - 1; i++){
				sbIds.append(gravePhotoIds.get(i));
				sbIds.append(",");
			}
			sbIds.append(gravePhotoIds.get(gravePhotoIds.size() - 1));
			updateQuery = String.format(updateQuery, oldPartName, newPartName, sbIds.toString());
			RuntimeExceptionDao<GravePhoto, Integer> dao = DB.db().dao(GravePhoto.class);
			dao.updateRaw(updateQuery);			
		}
	}
	 
	public static void deleteCemetery(int cemeteryId){
		DB.dao(Cemetery.class).deleteById(cemeteryId);
	}
	
	public static void deleteRegion(int regionId){
		DB.dao(Region.class).deleteById(regionId);		
	}
	
	public static void deleteRow(int rowId){
		DB.dao(Row.class).deleteById(rowId);
		String deletePlaceQuery = String.format("delete from place where row_id = %d;", rowId);
		DB.db().execManualSQL(deletePlaceQuery);
	}
	
	public static void deletePlace(int placeId){
		DB.dao(Place.class).deleteById(placeId);		
	}
	
	public static void deleteGrave(int graveId){
		DB.dao(Grave.class).deleteById(graveId);
	}
	
}
