package com.sindia.pdm3000;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SubNavigation extends RelativeLayout implements View.OnClickListener {
    private ImageView iv_navi_back;                // 返回按钮
    private TextView tv_navi_title;                // 中间的标题


    public SubNavigation(Context context)
    {
        this(context,null);
    }

    public SubNavigation(Context context, AttributeSet attrs)
    {
        super(context,attrs);

        View view  = LayoutInflater.from(context).inflate(R.layout.navigation_sub,this,true);

        iv_navi_back = (ImageView) findViewById(R.id.iv_navi_back);
        iv_navi_back.setOnClickListener(this);

        tv_navi_title = (TextView) findViewById(R.id.tv_navi_title);
    }

    /***
     * 获取标题
     * @return tv_navi_title
     */
    public TextView getTv_navi_title()
    {
        return tv_navi_title;
    }

    /***
     * 设置标题
     * @param title
     */
    public void setTitle(String title)
    {
        tv_navi_title.setText(title);
    }


    private SubNavigation.ClickCallback callback;                 // 声明回调函数

    /***
     * 设置按钮点击回调的接口
     * @param callback
     */
    public void setClickCallback(SubNavigation.ClickCallback callback)
    {
        this.callback = callback;
    }

    /***
     * 导航栏点击回调接口 -- Block中的回调方法
     * 如若需要标题可点击,可再添加
     */
    public static interface ClickCallback
    {
        void onBackClick();
        void onRightClick();
    }

    /***
     * 点击事件
     */
    @Override
    public void onClick(View v)
    {
        int id = v.getId();

        if (id == R.id.iv_navi_back)
        {
            callback.onBackClick();
            return;
        }
        if (id == R.id.iv_navi_right)
        {
            callback.onRightClick();
            return;
        }
    }
}