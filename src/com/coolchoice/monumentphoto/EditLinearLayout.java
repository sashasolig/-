package com.coolchoice.monumentphoto;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class EditLinearLayout extends LinearLayout{
	
	/*public EditLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}*/

	public EditLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public EditLinearLayout(Context context) {
		super(context);		
	}

	public interface StateListener{
		void onChangeState(boolean state);
	}
	
	private StateListener onStateListener;	
	
	public void setOnStateListener(StateListener onStateListener){
		this.onStateListener = onStateListener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
	    final int actualHeight = getHeight();

	    if (actualHeight > proposedheight){
	        // Keyboard is shown
	    	if(onStateListener != null){
	    		onStateListener.onChangeState(true);
	    	}
	    } else {
	        // Keyboard is hidden
	    	if(onStateListener != null){
	    		onStateListener.onChangeState(false);
	    	}
	    }

	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
}
