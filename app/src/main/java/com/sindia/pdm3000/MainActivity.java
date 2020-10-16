package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {// implements BottomNavigationView.OnNavigationItemSelectedListener { //AdapterView.OnItemClickListener,
    // 顶部导航栏相关
    private MainNavigation mMainNavigation;      // 调用自定义Navigation的Java类

    // 中间容器及片段
    private SystemFragment mSystemFragment;
    private ConfigFragment mConfigFragment;
    private Fragment[] mAllFragments;
    private int mLastFragment;//用于记录上个选择的Fragment

    // 底部导航控件
    BottomNavigationView mBottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        // 隐藏标题栏
        requestWindowFeature(FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
*/
        setContentView(R.layout.activity_main);

        //禁止旋转（在xml写了）
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 创建顶部主导航
        mMainNavigation = (MainNavigation) super.findViewById(R.id.naviMain);
        mMainNavigation.setTitle(getString(R.string.app_title));
        mMainNavigation.setClickCallback(mMainNavigationCallBack); // Java中叫回调，iOS中叫Block

        // 底部导航
        mBottomNavView = findViewById(R.id.bottom_nav_view);
        mBottomNavView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // 创建所有flagment
        mSystemFragment = new SystemFragment();
        mConfigFragment = new ConfigFragment();
        mAllFragments = new Fragment[]{mSystemFragment, mConfigFragment};
        mLastFragment = 0;

        // 显示最左侧的页签视图
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.add(R.id.fragment_parent_view, mSystemFragment);
        //transaction.add(R.id.fragment_parent_view, mConfigFragment);
        transaction.replace(R.id.fragment_parent_view, mSystemFragment)
                .show(mSystemFragment)
                .commit();
/*
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.bottom_menu_system, R.id.bottom_menu_config)
                .build();

        navController = new NavController(this);
        navController.setGraph(id.re);
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
*/
    }

    // 顶部导航栏事件
    private MainNavigation.ClickCallback mMainNavigationCallBack = new MainNavigation.ClickCallback() {
        @Override
        public void onBackClick() {
            System.out.println("返回按钮");
            System.out.println("写你的逻辑呗~");
        }

        @Override
        public void onRightClick() {
            System.out.println("右侧按钮");
            System.out.println("写你的逻辑呗~");
        }
    };

    // 底部导航事件
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_menu_system: {
                    if (switchFragmentByIndex(0)) {
                        return true;
                    }
                    break;
                }
                case R.id.bottom_menu_config: {
                    if (switchFragmentByIndex(1)) {
                        return true;
                    }
                    break;
                }
            }
            return false;
        }
    };

    // 切换底部页签及视图
    private boolean switchFragmentByIndex(int index) {
        if (index == mLastFragment) {
            return false;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(mAllFragments[mLastFragment]);//隐藏上个Fragment
        if (!mAllFragments[index].isAdded()) {
            transaction.add(R.id.fragment_parent_view, mAllFragments[index]);
        }
        transaction.show(mAllFragments[index]).commitAllowingStateLoss();
        mLastFragment = index;
        return true;
    }

    // @Override
    //public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    //    return false;
    //}

}
