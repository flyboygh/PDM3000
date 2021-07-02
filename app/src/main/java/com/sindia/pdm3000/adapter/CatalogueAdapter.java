package com.sindia.pdm3000.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sindia.pdm3000.R;
import com.sindia.pdm3000.model.CatalogueData;

import java.util.ArrayList;
import java.util.List;

public class CatalogueAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener{
    private Context mContext = null;
    private CatalogueAdapter.Callback mCallback = null;
    private List<CatalogueData> mListCatalogue = new ArrayList<CatalogueData>();
    //构造函数
    public CatalogueAdapter(Context context, CatalogueAdapter.Callback callback)
    {
        mContext = context;
        mCallback = callback;
    }
    public void setList(ArrayList<CatalogueData> list)
    {
        mListCatalogue = list;
    }
    // 上层回调
    public interface Callback {
        public void selectCatalogue(int nIndex);
    }

    public void selectAllItem(){

    }


    @Override
    public int getCount() {
        return mListCatalogue.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.catalogue_cell, parent,false);

        CatalogueData catalogue = mListCatalogue.get(position);

        TextView textViewName = convertView.findViewById(R.id.textViewCatalogueName);
        String catalogueName = catalogue.lineName+catalogue.jointName+catalogue.dateInfo;
        if(catalogue.conclusion ==1)
            catalogueName += "-检测到放电信号";
        textViewName.setText(catalogueName);

        CheckBox btnCheck = convertView.findViewById(R.id.checkBox);
        btnCheck.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
        btnCheck.setTag(position);

        return convertView;
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {

    }
}
