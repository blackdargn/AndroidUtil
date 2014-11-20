/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-12-26
 * @描述: 长按与松开 自定义 按钮
 * @声明: copyrights reserved by personal 2007-2013
*******************************************************/
package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class LongPressButton extends Button
{
	private OnPressLongListener mPressLongListener;
	private int mLongPressTimeMin = 200;
	private boolean mIsPressed = false;
	private boolean mIsFire = false;
	private boolean isLong = true;
	
	public LongPressButton(Context context)
	{
		super(context);
	}

	public LongPressButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();	
		switch(action)
		{
			case MotionEvent.ACTION_MOVE:
			{
				return true;
			}
			case MotionEvent.ACTION_DOWN:
			{
				if(mIsPressed) 
				{
					break;
				}				
				// 已按住
				mIsPressed = true;
				postDelayed(mLongPressRunnable, isLong ? mLongPressTimeMin : 10);
				
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			{				
				removeCallbacks(mLongPressRunnable);
				
				if(mIsFire)
				{
					if(mPressLongListener != null)
					{
						mPressLongListener.onPressUp(this);
					}
				}
				
				mIsPressed = false;
				mIsFire = false;
				break;
			}
		}
		
		return super.onTouchEvent(event);
	}	
	
	public void setOnPressLongListener(OnPressLongListener listener,boolean isLong)
	{
		this.mPressLongListener = listener;
		this.isLong = isLong;
	}
	
	public void setOnPressLongListener(OnPressLongListener listener)
    {
	    setOnPressLongListener(listener,true);
    }

	private Runnable mLongPressRunnable = new Runnable()
	{	
		@Override
		public void run()
		{
			if(mPressLongListener != null)
			{
				mPressLongListener.onPressDown(LongPressButton.this);
			}
			mIsFire = true;
		}
	};
	
	public static interface OnPressLongListener
	{
		/** 长按下时      触发的事件 */
		public void onPressDown(View v);
		/** 长按松开时 触发的事件*/
		public void onPressUp(View v);
	}
}
