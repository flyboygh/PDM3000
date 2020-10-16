package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class ConfigFragment extends Fragment {
    //private ConfigViewModel configViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //configViewModel = ViewModelProviders.of(this).get(ConfigViewModel.class);
        View root = inflater.inflate(R.layout.fragment_config, container, false);
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
