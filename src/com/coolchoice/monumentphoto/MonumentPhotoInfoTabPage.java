package com.coolchoice.monumentphoto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.*;

public class MonumentPhotoInfoTabPage extends Fragment implements MonumentInfoActivity.TabPageListener {	
	
	private Button btnMakePhoto, btnDeletePhoto, btnSendPhoto;
	
	private GridView gridPhotos;
	
	private static List<PhotoGridItem> gridPhotoItems = new ArrayList<PhotoGridItem>();
		
	private static Uri mUri;
	
	private Monument mMonument;
	
	private PhotoGridAdapter mPhotoGridAdapter;
	
	private final int REQUEST_CODE_PHOTO_INTENT = 101;
	
	private static int screenWidth, screenHeight;
	private static int screenWidthDp, screenHeightDp;
	
	private static int widthPhoto, widthPhotoDp, gridPhotoWidthDp;
	
	private static final int countPhotoInRow = 2;
		
	@Override
	public void onChangeTab(int fromPageNumber, int toPageNumber) {
		if(toPageNumber == MonumentInfoActivity.PHOTO_INFO_TABPAGE_NUMBER){
			updatePhotoGridItems();
			updateStatusInPhotoGrid();
			((PhotoGridAdapter) gridPhotos.getAdapter()).notifyDataSetChanged();			
		}		
	}
			
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.monument_photo_info_tabpage, null);
        this.btnDeletePhoto = (Button) result.findViewById(R.id.btnDeletePhoto);
        this.btnMakePhoto = (Button) result.findViewById(R.id.btnMakePhoto);
        this.btnSendPhoto = (Button) result.findViewById(R.id.btnSend);
        this.gridPhotos = (GridView) result.findViewById(R.id.gridPhotos);
                
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        screenHeightDp = (int)(screenHeight/metrics.density);
        screenWidthDp = (int)(screenWidth/metrics.density);
        int rangeWidthDp = 5 * 2 +  (countPhotoInRow + 1) * 5;
        if(screenWidth < screenHeight){
        	//min is screenWidth
        	gridPhotoWidthDp = (screenWidthDp - rangeWidthDp)/countPhotoInRow;
        } else {
        	//min is screenHeight
        	gridPhotoWidthDp = (screenHeightDp - rangeWidthDp)/countPhotoInRow;
        }
        
        widthPhotoDp = gridPhotoWidthDp;
        widthPhoto =(int) (widthPhotoDp * metrics.density);
        this.gridPhotos.setColumnWidth(gridPhotoWidthDp);
        

        
        this.btnMakePhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean enableGPS =  true;
				if(enableGPS){
					if(Settings.getCurrentLocation() == null){
						DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    	    @Override
				    	    public void onClick(DialogInterface dialog, int which) {
				    	        switch (which){
				    	        case DialogInterface.BUTTON_POSITIVE:			    	        	
				    	            break;
				    	        }
				    	    }
						};	
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					   	builder.setMessage(getActivity().getString(R.string.notDetectGPS)).setPositiveButton(getActivity().getString(R.string.yes),dialogClickListener).show();
						return;
					}
					mUri = generateFileUri();
					if (mUri == null) {
						Toast.makeText(MonumentPhotoInfoTabPage.this.getActivity(), "SD card not available",
								Toast.LENGTH_LONG).show();
						return;
					}
	
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
					startActivityForResult(intent, REQUEST_CODE_PHOTO_INTENT);
				}
				
			}
		});
        
        this.btnDeletePhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<PhotoGridItem> deletedItems = new ArrayList<PhotoGridItem>();
				for(PhotoGridItem item : gridPhotoItems){
					if(item.isChecked()){
						File file = new File(item.getPath());
						file.delete();
						MonumentDB.deleteMonumentPhoto(item.getMonumentPhoto());
						deletedItems.add(item);
					}
				}
				gridPhotoItems.removeAll(deletedItems);
				((BaseAdapter)gridPhotos.getAdapter()).notifyDataSetChanged();
				btnDeletePhoto.setEnabled(mPhotoGridAdapter.isChoosePhoto());
				btnSendPhoto.setEnabled(mPhotoGridAdapter.isChoosePhoto());
			}
		});
        
        this.btnSendPhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HashSet<Integer> hashSet = new HashSet<Integer>();
				ArrayList<PhotoGridItem> sendedItems = new ArrayList<PhotoGridItem>();
				for(PhotoGridItem item : gridPhotoItems){
					if(item.isChecked()){
						hashSet.add(item.getMonumentPhoto().Id);
						item.setChecked(false);
					}
				}
				
				boolean isMonumentStatusWait = true;
				Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));						
				for(GravePhoto photo: monument.Photos){
					if(photo.Status != Monument.STATUS_SEND){
						if(hashSet.contains(photo.Id)){
							photo.Status = Monument.STATUS_WAIT_SEND;
							DB.dao(GravePhoto.class).createOrUpdate(photo);
						} else {
							isMonumentStatusWait = false;
						}
					}
				}
				if(monument.Status != Monument.STATUS_SEND && isMonumentStatusWait){
					monument.Status = Monument.STATUS_WAIT_SEND;
					DB.dao(Monument.class).createOrUpdate(monument);
				}
				updatePhotoGridItems();
				updateStatusInPhotoGrid();
			}
			
		});
        
          
        
        updatePhotoGridItems();
        this.mPhotoGridAdapter = new PhotoGridAdapter();
        this.gridPhotos.setAdapter(this.mPhotoGridAdapter);
        this.gridPhotos.setOnItemClickListener(new OnItemClickListener() {            
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id ) {
				PhotoGridItem item = gridPhotoItems.get(position);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(item.getUri(), "image/*");
				startActivity(intent);				
			}
        });
        this.gridPhotos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				gridPhotoItems.get(position).setChecked(!gridPhotoItems.get(position).isChecked());
				((BaseAdapter)gridPhotos.getAdapter()).notifyDataSetChanged();
				btnDeletePhoto.setEnabled(mPhotoGridAdapter.isChoosePhoto());
				btnSendPhoto.setEnabled(mPhotoGridAdapter.isChoosePhoto());
				return true;
			}
		});   
        
        this.btnDeletePhoto.setEnabled(this.mPhotoGridAdapter.isChoosePhoto());
        this.btnSendPhoto.setEnabled(this.mPhotoGridAdapter.isChoosePhoto());
        ((MonumentInfoActivity)getActivity()).setOnTabPageListener(this);
        return result;		
	}
	
	private void updatePhotoGridItems(){
		this.mMonument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
        boolean isAddImage = true;
        if(gridPhotoItems.size() > 0){
        	if(this.mMonument.Id != gridPhotoItems.get(0).monumentPhoto.Monument.Id){
        		gridPhotoItems.clear();        		
        	}else{
        		isAddImage = false;
        	}
        }
        if(isAddImage){
        	if(this.mMonument != null){
		        for(GravePhoto monumentPhoto : this.mMonument.Photos){
		        	PhotoGridItem item = new PhotoGridItem();
		        	Uri uri = Uri.parse(monumentPhoto.UriString);
		        	item.setPath(uri.getPath());
		        	item.setChecked(false);
		        	item.setUri(uri);
		        	item.setBmp(null);
		        	item.setMonumentPhoto(monumentPhoto);
		        	gridPhotoItems.add(item);
		        }
        	}
        }    
	}
	
	public void updateStatusInPhotoGrid(){
		this.mMonument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
		HashMap<Integer,GravePhoto> hashMapStatus = new HashMap<Integer,GravePhoto>();
		for(GravePhoto photo : this.mMonument.Photos){
			hashMapStatus.put(photo.Id, photo);
		}
		for(PhotoGridItem item : gridPhotoItems){
			if(item.getMonumentPhoto() != null){
				item.setMonumentPhoto(hashMapStatus.get(item.getMonumentPhoto().Id));
			}
		}
		mPhotoGridAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_PHOTO_INTENT:
				this.mMonument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
				GravePhoto monumentPhoto = new GravePhoto();
				monumentPhoto.Monument = this.mMonument;
				monumentPhoto.CreateDate = new Date();
				monumentPhoto.UriString = mUri.toString();
				if(Settings.getCurrentLocation() != null){
					Location location = Settings.getCurrentLocation();
					monumentPhoto.Latitude = location.getLatitude();
					monumentPhoto.Longitude = location.getLongitude();
				}
				if(Settings.IsAutoSendPhotoToServer(getActivity())){
					monumentPhoto.Status = Monument.STATUS_WAIT_SEND;
				} else {
					monumentPhoto.Status = Monument.STATUS_FORMATE;
				}
				DB.dao(GravePhoto.class).create(monumentPhoto);
				PhotoGridItem item = new PhotoGridItem();
	        	item.setPath(mUri.getPath());
	        	item.setChecked(false);
	        	item.setUri(mUri);
	        	item.setBmp(null);
	        	item.setMonumentPhoto(monumentPhoto);
	        	gridPhotoItems.add(item);
	        	((BaseAdapter)gridPhotos.getAdapter()).notifyDataSetChanged();
	        	saveExifInfo(mUri.getPath(), this.mMonument, monumentPhoto);
				break;
			}
		} 
		
	}
	
	private Uri generateFileUri() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return null;
		File path = new File(Environment.getExternalStorageDirectory(),	Settings.getStorageDirPhoto());
		if (!path.exists()) {
			if (!path.mkdirs()) {
				return null;
			}
		}
		String timeStamp = String.valueOf(System.currentTimeMillis());
		File newFile = new File(path.getPath() + File.separator + timeStamp	+ ".jpg");
		return Uri.fromFile(newFile);
	}
	
	public boolean saveExifInfo(String filePath, Monument monument, GravePhoto monumentPhoto){
		try {
			TiffOutputSet outputSet = null;
			IImageMetadata metadata = Sanselan.getMetadata(new File(filePath));
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				TiffImageMetadata exif = jpegMetadata.getExif();
				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}
			if (null != outputSet) {
				TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
				int ownerLessFlag = 0;
				if(monument.IsOwnerLess){
					ownerLessFlag = 1;
				}
				String userCommentValue = String.format("%f~%f~%s~%s~%s~%s~%s~%d", monumentPhoto.Longitude, monumentPhoto.Latitude, monument.CemeteryName,
						monument.Region, monument.Row, monument.Place, monument.Grave, ownerLessFlag);
				byte[] userCommentValueBytes = userCommentValue.getBytes("Cp1251");
				TiffOutputField f = new TiffOutputField(ExifTagConstants.EXIF_TAG_USER_COMMENT,
						ExifTagConstants.EXIF_TAG_USER_COMMENT.FIELD_TYPE_ASCII,
						userCommentValueBytes.length, userCommentValueBytes);
				exifDirectory.removeField(TiffConstants.EXIF_TAG_USER_COMMENT);
				exifDirectory.add(f);
				outputSet.setGPSInDegrees(monumentPhoto.Longitude, monumentPhoto.Latitude);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ExifRewriter exifRewriter = new ExifRewriter();
				exifRewriter.updateExifMetadataLossless(new File(filePath), baos, outputSet);
				FileOutputStream output = new FileOutputStream(filePath);
				output.write(baos.toByteArray());
				output.close();				
			}
		} catch (Exception ex) {
			return false;
		}
		return true;

	}
	
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onDestroy(){		
		super.onDestroy();
	}
	
	public class PhotoGridAdapter extends BaseAdapter {
					
        public PhotoGridAdapter() {
        }
        

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	LayoutInflater inflater = (LayoutInflater) MonumentPhotoInfoTabPage.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.photo_grid_item, parent, false);
            }
            ImageView ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
            ImageView ivPhotoChoose = (ImageView) convertView.findViewById(R.id.ivChoosePhoto);
            ImageView ivIsSend = (ImageView) convertView.findViewById(R.id.ivStatus);
            TextView tvGPS = (TextView) convertView.findViewById(R.id.tvGPS);
            PhotoGridItem item = gridPhotoItems.get(position);
            if(item.getBmp() == null) {
	            File imgFile = new  File(item.getPath());
	            int widthScaledPhotoPx = MonumentPhotoInfoTabPage.widthPhoto;
	            int heightScaledPhotoPx;
	            int rotateAngle = 0;
	            if(imgFile.exists()){	
	            	try {
						ExifInterface ex = new ExifInterface(imgFile.getAbsolutePath());
						int orientation = ex.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
						switch (orientation) {
						case ExifInterface.ORIENTATION_NORMAL:
							rotateAngle = 0;
							break;
						case ExifInterface.ORIENTATION_ROTATE_90:
							rotateAngle = 90;		
							break;
						case ExifInterface.ORIENTATION_ROTATE_180:
							rotateAngle = 180;
							break;
						case ExifInterface.ORIENTATION_ROTATE_270:
							rotateAngle = 270;
							break;
						default:
							rotateAngle = 0;
							break;
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	                heightScaledPhotoPx = myBitmap.getHeight() * widthScaledPhotoPx / myBitmap.getWidth();
	                Bitmap scaledBmp = Bitmap.createScaledBitmap(myBitmap, widthScaledPhotoPx, heightScaledPhotoPx, true);
	                
	                Matrix matrix = new Matrix();
	                matrix.setRotate(rotateAngle);
	                Bitmap rotateScaledBmp = Bitmap.createBitmap(scaledBmp, 0, 0, scaledBmp.getWidth(), scaledBmp.getHeight(), matrix, true);
	                                
	                
	                ivPhoto.setImageBitmap(rotateScaledBmp);
	                gridPhotoItems.get(position).setBmp(rotateScaledBmp);	                
	            }            
            } else {
            	ivPhoto.setImageBitmap(item.getBmp());
            }
            
            if(item.isChecked()) {
            	ivPhotoChoose.setVisibility(View.VISIBLE);
            } else {
            	ivPhotoChoose.setVisibility(View.GONE);
            }
            if(item.getMonumentPhoto() != null){
            	ivIsSend.getDrawable().setLevel(item.getMonumentPhoto().Status);
            }            
            if(item.getMonumentPhoto() != null){
            	double lat = item.getMonumentPhoto().Latitude;
            	double lng = item.getMonumentPhoto().Longitude;
            	String gpsString = String.format("GPS:%s, %s", Location.convert(lat, Location.FORMAT_SECONDS),Location.convert(lat, Location.FORMAT_SECONDS) );
            	tvGPS.setText(gpsString);
            } else {
            	tvGPS.setText("GPS");
            }

            return convertView;
        }  
        
        public boolean isChoosePhoto(){
        	for(PhotoGridItem item : gridPhotoItems){
        		if(item.isChecked()){
        			return true;
        		}
        	}
        	return false;
        }
        


        public final int getCount() {
            return gridPhotoItems.size();
        }

        public final Object getItem(int position) {
            return gridPhotoItems.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }
    
    class PhotoGridItem{
    	  	
		private String path;
		private Uri uri;
    	private Bitmap bmp;
    	private boolean checked;
    	private GravePhoto monumentPhoto;
    	
    	public GravePhoto getMonumentPhoto() {
			return monumentPhoto;
		}

		public void setMonumentPhoto(GravePhoto monumentPhoto) {
			this.monumentPhoto = monumentPhoto;
		}

		public boolean isChecked() {
			return checked;
		}
    	
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public Uri getUri() {
			return uri;
		}

		public void setUri(Uri uri) {
			this.uri = uri;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		
		public Bitmap getBmp() {
			return bmp;
		}
		
		public void setBmp(Bitmap bmp) {
			this.bmp = bmp;
		}	
	}
	
}
