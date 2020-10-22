package com.sindia.pdm3000;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// 数据查看主视图
public class DataViewFragment extends Fragment {
    private DataFilterFragment mFilterFragment;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_view, container, false);
        //mFilterFragment = root.frag root.findViewById(R.id.fragmentDataFilter);
        return root;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        /*if (hidden) {  //不在最前端界面显示
            //mVideoView.pause();
            mFilterFragment.onHideFragment();
        } else {  //重新显示到最前端
            //mVideoView.start();
            mFilterFragment.onShowFragment();
        }*/
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }
}
