package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sindia.pdm3000.http.PdHttpRequest;
import com.sindia.pdm3000.model.ParamSetData;

public class ParamSetFragment extends Fragment {
    //private ConfigViewModel configViewModel;
    private EditText mLineNameEdit;
    private EditText mJointNameEdit;
    private Button mApplyChangeButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //configViewModel = ViewModelProviders.of(this).get(ConfigViewModel.class);
        View root = inflater.inflate(R.layout.fragment_param_set, container, false);

        mLineNameEdit = root.findViewById(R.id.editTextLineName);
        mJointNameEdit = root.findViewById(R.id.editTextJointName);
        mApplyChangeButton = root.findViewById(R.id.buttonApplyChange);

        // 测试数据
        mLineNameEdit.setText("默认线路");
        mJointNameEdit.setText("默认接头");

        // 设置控件事件
        mApplyChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParamSetData paramSet = new ParamSetData();
                paramSet.lineName = mLineNameEdit.getText().toString();
                paramSet.jointName = mJointNameEdit.getText().toString();
                PdHttpRequest.doPostParamSet(paramSet, new PdHttpRequest.Callback() {
                    @Override
                    public void onResponse(PdHttpRequest.ResponseBase resp) {

                    }
                });
            }
        });
        /*final TextView textView = root.findViewById(R.id.text_home);
        configViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });*/
        return root;
    }
}
