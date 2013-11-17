package com.coolchoice.monumentphoto.map;

import com.coolchoice.monumentphoto.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.overlay.balloon.OnBalloonListener;
import ru.yandex.yandexmapkit.utils.GeoPoint;

public class PointBalloonItem extends BalloonItem implements OnBalloonListener {

	private Context mContext;
    private TextView mTextViewTitle;
    private ImageButton mImageBtnRemove;
    private OnRemoveOverlayItem mOnRemoveOverlayItem;
    
    interface OnRemoveOverlayItem{
    	void onRemoveOverlayItem(PointBalloonItem balloonItem);
    }

    public PointBalloonItem(Context context, GeoPoint geoPoint) {
        super(context, geoPoint);
        mContext = context;
    }
      
    @Override
    public void inflateView(Context context){
        LayoutInflater inflater = LayoutInflater.from( context );
        model = (ViewGroup)inflater.inflate(R.layout.map_gps_balloon, null);
        this.mTextViewTitle = (TextView) model.findViewById(R.id.balloon_text_title);
        this.mImageBtnRemove = (ImageButton) model.findViewById(R.id.balloon_remove);        
        String gpsString = String.format("%s, %s", Location.convert(getGeoPoint().getLat(), Location.FORMAT_SECONDS), Location.convert(getGeoPoint().getLon(), Location.FORMAT_SECONDS) );
        this.mTextViewTitle.setText(gpsString);
        this.mImageBtnRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mOnRemoveOverlayItem != null){
					mOnRemoveOverlayItem.onRemoveOverlayItem(PointBalloonItem.this);
				}
				PointBalloonItem.this.setVisible(false);
				
			}
		});
    }    
    
    public void setOnRemoveOverlayItem(OnRemoveOverlayItem onRemoveOverlayItem){
        this.mOnRemoveOverlayItem = onRemoveOverlayItem;   
    }

    @Override
    public void onBalloonViewClick(BalloonItem item, View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle((String)view.getTag());
        dialog.show();
    }

    @Override
    public void onBalloonShow(BalloonItem balloonItem) {
    }

    @Override
    public void onBalloonHide(BalloonItem balloonItem) {

    }

    @Override
    public void onBalloonAnimationStart(BalloonItem balloonItem) {

    }

    @Override
    public void onBalloonAnimationEnd(BalloonItem balloonItem) {
    }
}