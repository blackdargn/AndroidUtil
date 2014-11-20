/*******************************************************
 * @作者: zhaohua
 * @日期: 2012-6-11
 * @描述: 分页控件
 * @声明: copyrights reserved by Petfone 2007-2011
*******************************************************/
package com.android.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.util.R;

/**
 * @author zhaohua
 *
 */
public class PageView extends LinearLayout implements OnClickListener
{
	private Button priv_page_btn;
	private Button next_page_btn;
	private TextView cur_page_tx;
	
	private int totalPage;
	private int curPage;
	private OnPageClickListener listener;
	
	public PageView(Context context)
	{
		super(context);
		initViews();
	}

	public PageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initViews();
	}

	
	/**
	 * @param curPage [0-n)
	 * @param totalPage n
	 * @throws Exception 参数不合法时
	 */
	public void initPage(int curPage, int totalPage) throws Exception
	{
		this.curPage = curPage;
		this.totalPage = totalPage;
		
		if(curPage < 0 || totalPage < 0 || curPage >= totalPage)
		{
			throw new Exception(" page init invalid!");
		}
		
		updateView();
	}
	
	public void setOnPageClickListener(OnPageClickListener listener)
	{
		this.listener = listener;
	}
	
	private void initViews()
	{
		View row = inflate(getContext(), R.layout.view_page, null);
		addView(row,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		priv_page_btn = (Button)findViewById(R.id.priv_page_btn);
		next_page_btn = (Button)findViewById(R.id.next_page_btn);
		cur_page_tx = (TextView)findViewById(R.id.cur_page_tx);
		
		priv_page_btn.setOnClickListener(this);
		next_page_btn.setOnClickListener(this);
	}
	
	private void updateView()
	{
		priv_page_btn.setVisibility(curPage == 0 ? View.GONE : View.VISIBLE);
		
		next_page_btn.setVisibility((curPage == totalPage-1)? View.GONE : View.VISIBLE);
		
		cur_page_tx.setText( (curPage+1) +"/" + totalPage);
	}

	@Override
	public void onClick(View v)
	{
        if (priv_page_btn == v) {
            --curPage;
            if (curPage < 0) {
                curPage = 0;
            }
        } else if (v == next_page_btn) {
            ++curPage;
            if (curPage >= totalPage) {
                curPage = (totalPage - 1);
            }
        }
		
		updateView();
		if(listener != null)
		{
			listener.onPageClickListener(curPage);
		}
	}
	
	public static interface OnPageClickListener
	{
		/**
		 * 翻页按钮动作事件监听
		 * @param page 动作的页码
		 */
		public void onPageClickListener(int page);
	}
}
