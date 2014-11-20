package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.util.R;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-6
 * @see : 自定义刷新按钮
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class RefreshView extends LinearLayout
{
  private ImageTextButton mButton;
  private LayoutInflater mInflater;
  private ProgressBar mProBar;

  public RefreshView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    this.mInflater = LayoutInflater.from(paramContext);
    this.mInflater.inflate(R.layout.view_refresh, this);
    this.mButton = ((ImageTextButton)findViewById(R.id.button_view));
    this.mProBar = ((ProgressBar)findViewById(R.id.button_loading));
    this.mButton.setImage(R.drawable.button_refresh);
    setGravity(Gravity.CENTER);
  }

  public void Normal()
  {
    this.mButton.setVisibility(View.VISIBLE);
    this.mProBar.setVisibility(View.GONE);
  }

  public ImageTextButton getButton()
  {
    return this.mButton;
  }

  public void reFresh()
  {
    this.mButton.setVisibility(View.GONE);
    this.mProBar.setVisibility(View.VISIBLE);
  }

  public void reFreshFinish()
  {
    this.mButton.setVisibility(View.VISIBLE);
    this.mProBar.setVisibility(View.GONE);
  }
}