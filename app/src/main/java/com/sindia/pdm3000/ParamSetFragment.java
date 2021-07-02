package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.sindia.pdm3000.http.PdHttpRequest;
import com.sindia.pdm3000.model.ParamSetData;

public class ParamSetFragment extends Fragment {
    //private ConfigViewModel configViewModel;
    private EditText mLineNameEdit;
    private EditText mJointNameEdit;
    private EditText mIntervalTime;
    private Spinner mFreqCenter;
    private Spinner mFreqWidth;

    private Button mApplyChangeButton;
    private Button mReloadDataButton;
    private Button mTestImageButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //configViewModel = ViewModelProviders.of(this).get(ConfigViewModel.class);
        View root = inflater.inflate(R.layout.fragment_param_set, container, false);

        mLineNameEdit = root.findViewById(R.id.editTextLineName);
        mJointNameEdit = root.findViewById(R.id.editTextJointName);
        mIntervalTime =  root.findViewById(R.id.editWorkInterval);
        mFreqCenter = root.findViewById(R.id.spinnerFreqCenter);
        mFreqWidth = root.findViewById(R.id.spinnerFreqWidth);
        mTestImageButton = root.findViewById(R.id.buttonTest);

        mApplyChangeButton = root.findViewById(R.id.buttonApplyChange);
        mReloadDataButton = root.findViewById(R.id.buttonReloadData);

        // 设置控件事件
        //提交数据
        mApplyChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParamSetData paramSet = new ParamSetData();
                paramSet.lineName = mLineNameEdit.getText().toString();
                paramSet.jointName = mJointNameEdit.getText().toString();
                String freqCenter = mFreqCenter.getSelectedItem().toString();
                String freqWidth = mFreqWidth.getSelectedItem().toString();
                paramSet.work_interval  = Integer.valueOf(mIntervalTime.getText().toString());
                paramSet.freq_center = GetFreqCenterValue(freqCenter);
                paramSet.freq_width = GetFreqWidthValue(freqWidth);
                PdHttpRequest.doPostParamSet(paramSet, new PdHttpRequest.Callback() {
                    @Override
                    public void onResponse(PdHttpRequest.ResponseBase resp) {
                        if (resp.errCode != 0) {
                            String msg = String.format("未知错误：%d", resp.errCode);
                            if (!resp.errMsg.isEmpty()) {
                                msg = resp.errMsg;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("错误")
                                    .setMessage(msg)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            }
        });
        //获取数据
        mReloadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PdHttpRequest.doGetParamSet(new PdHttpRequest.Callback() {
                    @Override
                    public void onResponse(PdHttpRequest.ResponseBase resp) {
                        if (resp.errCode == 0) {
                            PdHttpRequest.ParamSetResp paramSetResp = (PdHttpRequest.ParamSetResp)resp;
                            mLineNameEdit.setText(paramSetResp.paramSet.lineName);
                            mJointNameEdit.setText(paramSetResp.paramSet.jointName);
                            mIntervalTime.setText(Integer.toString(paramSetResp.paramSet.work_interval));

                            int freq_center = paramSetResp.paramSet.freq_center;
                            int freq_width = paramSetResp.paramSet.freq_width;
                            SpinnerAdapter freqCenterAdapter = mFreqCenter.getAdapter();
                            for(int i = 0; i<freqCenterAdapter.getCount(); i++)
                            {
                                if( freqCenterAdapter.getItem(i).equals(GetFreqCenterText(freq_center)))
                                {
                                    mFreqCenter.setSelection(i);
                                    break;
                                }
                            }
                            SpinnerAdapter freqWidthAdapter = mFreqWidth.getAdapter();
                            for(int i = 0; i<freqWidthAdapter.getCount(); i++)
                            {
                                if( freqWidthAdapter.getItem(i).equals(GetFreqWidthText(freq_center)))
                                {
                                    mFreqWidth.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });
        //测试图片
        mTestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),TestImageActivity.class));
            }
        });

        return root;
    }
    public int GetFreqCenterValue(String freqCenterText)
    {
        String unit = "MHz";
        int index = freqCenterText.indexOf(unit);
        String temp = freqCenterText.substring(0,index);
        int freqCenterValue = Integer.valueOf(temp);

        return freqCenterValue;
    }

    public int GetFreqWidthValue(String freqWidthText)
    {
        String unit = "kHz";
        int index = freqWidthText.indexOf(unit);
        String temp = freqWidthText.substring(0,index);
        int freqWidthValue = Integer.valueOf(temp);

        return freqWidthValue;
    }

    public String GetFreqCenterText(int freqCenterValue)
    {
        String unit = "MHz";
        String freqCenterText = Integer.toString(freqCenterValue);
        freqCenterText += unit;

        return freqCenterText;
    }

    public String GetFreqWidthText(int freqWidthValue)
    {
        String unit = "kHz";
        String freqWidthText = Integer.toString(freqWidthValue);
        freqWidthText += unit;

        return freqWidthText;
    }
}
