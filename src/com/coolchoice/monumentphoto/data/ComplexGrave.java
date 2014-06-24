package com.coolchoice.monumentphoto.data;

import java.io.File;
import java.io.IOException;

import android.net.Uri;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;

public class ComplexGrave {
	
	public Grave Grave;
	
	public Place Place;
	
	public Row Row;
	
	public Region Region;
	
	public Cemetery Cemetery;
	
	public ComplexGrave(){
		setToNullObject();
	}
	
	public String toString(){
        StringBuilder sb = new StringBuilder();
        if(this.Cemetery != null){
            sb.append(this.Cemetery.Name);
        }
        if(this.Region != null){
            sb.append(String.format(" / Уч. %s", this.Region.Name));
        }
        
        if(this.Row != null){
            sb.append(String.format(" / Ряд %s", this.Row.Name));
        }
        
        if(this.Place != null){
            sb.append(String.format(" / Место %s", this.Place.Name));
        }
        
        if(this.Grave != null){
            sb.append(String.format(" / Могила %s", this.Grave.Name != null ? this.Grave.Name : "Новая могила"));
        }
        return sb.toString();
	}
	
	public static class PlaceWithFIO{
		public String PlaceName;
		public String OldPlaceName;
		public int PlaceId;
		public String FName;
		public String MName;
		public String LName;
		
		public void toUpperFirstCharacterInFIO(){
			if(this.FName != null && this.FName.length() > 0){
				if(this.FName.length() == 1){
					this.FName = this.FName.toUpperCase();
				} else {
					this.FName = this.FName.substring(0, 1).toUpperCase() + this.FName.substring(1);
				}
			}
			if(this.LName != null && this.LName.length() > 0){
				if(this.LName.length() == 1){
					this.LName = this.LName.toUpperCase();
				} else {
					this.LName = this.LName.substring(0, 1).toUpperCase() + this.LName.substring(1);
				}
			}
			if(this.MName != null && this.MName.length() > 0){
				if(this.MName.length() == 1){
					this.MName = this.MName.toUpperCase();
				} else {
					this.MName = this.MName.substring(0, 1).toUpperCase() + this.MName.substring(1);
				}
			}
		}
	}
	
	public void setToNullObject(){
		this.Grave = null;
		this.Place = null;
		this.Row = null;
		this.Region = null;
		this.Cemetery = null;
	}
	
	public void loadByGraveId(int graveId){
		this.Grave = DB.dao(Grave.class).queryForId(graveId);
		if(this.Grave != null){
			loadByPlaceId(this.Grave.Place.Id);
		} else {
			setToNullObject();
		}
	}
	
	public void loadByPlaceId(int placeId){
		this.Place = DB.dao(Place.class).queryForId(placeId);
		if(this.Place != null){
			if(this.Place.Row != null){
				loadByRowId(this.Place.Row.Id);
			} else {
				this.Row = null;
				loadByRegionId(this.Place.Region.Id);
			}
		} else {
			setToNullObject();
		}
	}
	
	public void loadByRowId(int rowId){
		this.Row = DB.dao(Row.class).queryForId(rowId);
		if(this.Row != null){
			loadByRegionId(this.Row.Region.Id);
		} else {
			setToNullObject();
		}
	}
	
	public void loadByRegionId(int regionId){
		this.Region = DB.dao(Region.class).queryForId(regionId);
		if(this.Region != null){
			loadByCemeteryId(this.Region.Cemetery.Id);
		} else {
			setToNullObject();
		}
	}
	
	public void loadByCemeteryId(int cemeteryId){
		this.Cemetery = DB.dao(Cemetery.class).queryForId(cemeteryId);
		if(this.Cemetery != null) {
			//do nothing
		} else {
			setToNullObject();
		}
	}
	
	public Uri generateFileUri(File rootDir, String fileName) {
		File dir = new File(rootDir, this.Cemetery.Name);
		if(!dir.exists()){
			if(!dir.mkdirs()){
				return null;
			}
		}
		dir = new File(dir, this.Region.Name);
		if(!dir.exists()){
			if(!dir.mkdirs()){
				return null;
			}
		}
		if(this.Row != null){
			dir = new File(dir, this.Row.Name);
			if(!dir.exists()){
				if(!dir.mkdirs()){
					return null;
				}
			}
		}
		dir = new File(dir, this.Place.Name);
		if(!dir.exists()){
			if(!dir.mkdirs()){
				return null;
			}
		}
		if(this.Grave != null){
    		dir = new File(dir, this.Grave.Name);
    		if(!dir.exists()){
    			if(!dir.mkdirs()){
    				return null;
    			}
    		}
		}
		createFile(dir, Settings.NO_MEDIA_FILENAME);
		File newFile = null;		
		if(fileName != null){
		    newFile = new File(dir.getPath() + File.separator + fileName);
		} else {
		    String timeValue = String.valueOf(System.currentTimeMillis());
	        newFile = new File(dir.getPath() + File.separator + timeValue  + "." + Settings.JPG_EXTENSION);
		}
		
		return Uri.fromFile(newFile);
	}
	
	private void createFile(File rootDir, String fileName){
	    File file = new File(rootDir, Settings.NO_MEDIA_FILENAME);
	    if(file != null && !file.exists()){
	        try {
	            file.createNewFile();
	        } catch (IOException e) {
	            //do nothing
	        }
	    }
	}
	
	
	
	public File getPhotoFolder(){
		File rootDir = Settings.getRootDirPhoto();
		File dir = null;
		if(this.Cemetery != null){
			dir = new File(rootDir, this.Cemetery.Name);
			if(!dir.exists()){
				return null;
			}
		}
		
		if(this.Region != null){
			dir = new File(dir, this.Region.Name);
			if(!dir.exists()){
				return null;
			}
		}
		
		if(this.Row != null){
			dir = new File(dir, this.Row.Name);
			if(!dir.exists()){
				return null;
			}
		}
		
		if(this.Place != null){
			dir = new File(dir, this.Place.Name);
			if(!dir.exists()){
				return null;
			}
		}
		
		if(this.Grave != null){
			dir = new File(dir, this.Grave.Name);
			if(!dir.exists()){
				return null;
			}
		}
		return dir;
	}
	
	public Uri generateFileUri(String fileName) {
        File rootDir = Settings.getRootDirPhoto();
        return this.generateFileUri(rootDir, fileName);               
    }	
	
	public static boolean renameCemetery(Cemetery cemetery, String oldCemeteryName){
	    ComplexGrave complexGrave = new ComplexGrave();
        complexGrave.loadByCemeteryId(cemetery.Id);
        boolean result = renameMonumentEntity(complexGrave, oldCemeteryName);
        return result;
	}
	
	public static boolean renameRegion(Region region, String oldRegionName){
	    ComplexGrave complexGrave = new ComplexGrave();
        complexGrave.loadByRegionId(region.Id);
        boolean result = renameMonumentEntity(complexGrave, oldRegionName);
        return result;
	}
	
	public static boolean renameRow(Row row, String oldRowName){
	    ComplexGrave complexGrave = new ComplexGrave();
        complexGrave.loadByRowId(row.Id);
        boolean result = renameMonumentEntity(complexGrave, oldRowName);
        return result;
	}
	
	public static boolean renamePlace(Place place, String oldPlaceName){
	    ComplexGrave complexGrave = new ComplexGrave();
        complexGrave.loadByPlaceId(place.Id);
        boolean result = renameMonumentEntity(complexGrave, oldPlaceName);
        return result;
	}

	public static boolean renameGrave(Grave grave, String oldGraveName){		
		ComplexGrave complexGrave = new ComplexGrave();
		complexGrave.loadByGraveId(grave.Id);
		boolean result = renameMonumentEntity(complexGrave, oldGraveName);
		return result;
	}
	
	private static boolean renameMonumentEntity(ComplexGrave complexGrave, String oldMonumentEntityName){
	    MonumentDB monumentDB = new MonumentDB();
	    File photoDir = Settings.getRootDirPhoto();        
        StringBuilder sbNewPath = new StringBuilder(photoDir.getPath());
        if(complexGrave.Cemetery != null){
            sbNewPath.append(File.separator + complexGrave.Cemetery.Name);
        }
        if(complexGrave.Region != null){
            sbNewPath.append(File.separator + complexGrave.Region.Name);
        }
        if(complexGrave.Row != null){
            sbNewPath.append(File.separator + complexGrave.Row.Name);
        }
        if(complexGrave.Place != null){
            sbNewPath.append(File.separator + complexGrave.Place.Name);
        }
        if(complexGrave.Grave != null){
            sbNewPath.append(File.separator + complexGrave.Grave.Name);
        }
        String oldPath = sbNewPath.substring(0, sbNewPath.lastIndexOf(File.separator)) + File.separator + oldMonumentEntityName;                      
        File source = new File(oldPath);
        if(!source.exists()){
            return true; 
        }
        File dest = new File(sbNewPath.toString());
        Uri oldPartOfPathURI = Uri.fromFile(source);
        Uri newPartOfPathURI = Uri.fromFile(dest);      
        if(!dest.exists()){
            source.renameTo(dest);
        }       
        monumentDB.updateGravePhotoUriString(oldPartOfPathURI.toString(), newPartOfPathURI.toString());
        return true;
	}

	

}
