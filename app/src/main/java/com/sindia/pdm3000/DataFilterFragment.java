package com.sindia.pdm3000;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sindia.pdm3000.util.PDFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataFilterFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner mLineNameSpinner;
    private Spinner mLinePosSpinner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_filter, container, false);

        mLineNameSpinner = root.findViewById(R.id.spinnerLineName);
        mLinePosSpinner = root.findViewById(R.id.spinnerLinePos);

        Context context = root.getContext();
        List<String> list = PDFileUtil.getLineNames(context);

        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.item_line_name, R.id.textView4, list);
        mLineNameSpinner.setAdapter(adapter);
        mLineNameSpinner.setOnItemSelectedListener(this);

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.equals(mLineNameSpinner)) {
            String lineName = (String) mLineNameSpinner.getSelectedItem();
            PDFileUtil.getLinePosNames(getActivity(), lineName);
            //Log.e("", "");
        } else {
            //Log.e("", "");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.e("", "");
    }
}
