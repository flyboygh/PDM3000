package com.sindia.pdm3000;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sindia.pdm3000.adapter.CatalogueAdapter;
import com.sindia.pdm3000.http.PdHttpRequest;
import com.sindia.pdm3000.model.CatalogueData;
import com.sindia.pdm3000.model.FilterCatalogueData;
import com.sindia.pdm3000.model.LineData;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataReadFragment extends Fragment  implements AdapterView.OnItemSelectedListener, View.OnTouchListener,CatalogueAdapter.Callback,CompoundButton.OnCheckedChangeListener{
    private ViewStub mNoDataView;
    private Button mBtnRefresh;
    private Button mBtnReadCatalogue;
    private TextView mTextConnectInfo;
    private TextView mTextCheckedNum;
    private Spinner mSpinnerLine;
    private Spinner mSpinnerJoint;
    private Spinner mSpinnerPdType;
    private EditText mEditStartDate;
    private EditText mEditEndDate;
    private ListView mListViewCatalogue;
    private CatalogueAdapter mCatalogueAdapter;
    private View  mInflate;
    private CheckBox mCheckAll;
    private ArrayList<LineData>  mLineDataList;

    private static final DateFormat mDateformat =  DateFormat.getDateInstance(DateFormat.SHORT);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_read, container, false);
        //控件初始化
        mNoDataView = (ViewStub) root.findViewById(R.id.viewNoData);
        mBtnRefresh = root.findViewById(R.id.btnRefresh);
        mBtnReadCatalogue = root.findViewById(R.id.btnReadData);
        mTextConnectInfo = root.findViewById(R.id.textViewConnectInfo);
        mTextCheckedNum =  root.findViewById(R.id.textViewCheckNum);
        mTextCheckedNum.setText("已选中0项");
        mCheckAll = root.findViewById(R.id.btnCheckAll);
        mCheckAll.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
        //下拉框初始化
        mSpinnerLine = root.findViewById(R.id.spinnerLine);
        mSpinnerJoint = root.findViewById(R.id.spinnerJoint);
        mSpinnerPdType = root.findViewById(R.id.spinnerPdType);
        mSpinnerLine.setOnItemSelectedListener(this);
        mSpinnerJoint.setOnItemSelectedListener(this);
        mSpinnerPdType.setOnItemSelectedListener(this);
        //起止时间控件初始化
        mEditStartDate = root.findViewById(R.id.editTextDateStart);
        mEditEndDate = root.findViewById(R.id.editTextDateEnd);
        //线路和接头的下拉框内容
        List<String> lineNameList = new ArrayList<String>();
        List<String> jointNameList = new ArrayList<String>();
        lineNameList.add("请选择线路");
        jointNameList.add("请选择接头");
        Context context = root.getContext();
        ArrayAdapter adapterLine = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, lineNameList);
        mSpinnerLine.setAdapter(adapterLine);
        ArrayAdapter adapterjoint = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, jointNameList);
        mSpinnerJoint.setAdapter(adapterjoint);

        mListViewCatalogue = root.findViewById(R.id.listViewCatalogue);
        mListViewCatalogue.setVisibility(View.INVISIBLE);
        mCatalogueAdapter = new CatalogueAdapter(root.getContext(),(CatalogueAdapter.Callback)this);

        //设置默认的起止时间
        final Calendar calendar = Calendar.getInstance(Locale.CHINA);
        final Calendar calendarStart = Calendar.getInstance(Locale.CHINA);
        calendarStart.add(Calendar.DATE,-1);
        String startDate = mDateformat.format(calendarStart.getTime());
        String endDate = mDateformat.format(calendar.getTime());
        mEditStartDate.setText(startDate);
        mEditEndDate.setText(endDate);
        mEditStartDate.setOnTouchListener(this);
        mEditEndDate.setOnTouchListener(this);
        //判断是否连接到局放设备，连接是否成功显示内容不同
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity.getIs_connected()){
            mBtnRefresh.setVisibility(View.VISIBLE);
            mTextConnectInfo.setText("已连接设备：局放处理与采集装置");
            mBtnReadCatalogue.setEnabled(true);
        }
        else{
            //mBtnRefresh.setVisibility(View.INVISIBLE);
            mTextConnectInfo.setText("设备未连接，请检查网络链接或确认设备是否开启");
            mBtnReadCatalogue.setBackgroundColor(Color.parseColor("#C0C0C0"));
            mBtnReadCatalogue.setEnabled(false);
            showNoDataView();
        }
        //按钮点击响应操作
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  //for test
                ArrayList lineList = new ArrayList<LineData>();
                LineData line1 = new LineData();
                line1.lineName = "线路1";
                line1.arrJointName = new ArrayList<String>();
                line1.arrJointName.add("接头11");
                line1.arrJointName.add("接头12");
                line1.arrJointName.add("接头13");
                lineList.add(line1);
                LineData line2 = new LineData();
                line2.lineName = "线路2";
                line2.arrJointName = new ArrayList<String>();
                line2.arrJointName.add("接头21");
                line2.arrJointName.add("接头22");
                line2.arrJointName.add("接头23");
                lineList.add(line2);
                LineData line3 = new LineData();
                line3.lineName = "线路3";
                line3.arrJointName = new ArrayList<String>();
                line3.arrJointName.add("接头31");
                line3.arrJointName.add("接头32");
                line3.arrJointName.add("接头33");
                lineList.add(line3);
                updateLineJoint(lineList);
 //               PdHttpRequest.doGetLineInfo(new PdHttpRequest.Callback() {//                   @Override
//                    public void onResponse(PdHttpRequest.ResponseBase resp) {
//                        if(resp.respCode == 0)//返回正确
//                        {
//                            PdHttpRequest.GetLineInfoResp getLineInfoResp = (PdHttpRequest.GetLineInfoResp)resp;
//                            updateLineJoint(getLineInfoResp.lineList);
//                        }
//                        else{//返回失败
//                            String msg = String.format("未知错误：%d", resp.errCode);
//                            if (!resp.errMsg.isEmpty()) {
//                                msg = resp.errMsg;
//                            }
//                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                            builder.setTitle("错误")
//                                    .setMessage(msg)
//                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                        }
//                                    })
//                                    .show();
//                        }
//                    }});

            }});
        mBtnReadCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }});
        return root;
    }

    private void showNoDataView(){
        if(mInflate == null){
            mInflate = mNoDataView.inflate();
        }
        mNoDataView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(view != null)
        {
            ( (TextView)view ).setTextSize(14);
        }
        int ctrl_id = parent.getId();
        if( ctrl_id == R.id.spinnerLine ){
            String lineName = (String) mSpinnerLine.getSelectedItem();
            if(mLineDataList != null){
                for(LineData lineData : mLineDataList)
                {
                    if(lineData.lineName.equals(lineName)){
                        ArrayAdapter adapterjoint = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, lineData.arrJointName);
                        mSpinnerJoint.setAdapter(adapterjoint);
                    }
                }
            }
        }
        else if (ctrl_id == R.id.spinnerJoint){

        }
        else if (ctrl_id == R.id.spinnerPdType){

        }
        //向设备请求获取测量目录信息
        //requestCatalogue();
        //for test
        ArrayList<CatalogueData> arrCatalogueData = new ArrayList<CatalogueData>();
        for ( int i = 0; i<20; i++ )
        {
            CatalogueData data1 = new CatalogueData();
            data1.lineName = "线路"+i;
            data1.jointName = "接头"+i;
            data1.dateInfo = "20210615";
            data1.conclusion = (i%2==0)?1:0;
            data1.arrTimeInfo = new ArrayList<String>();
            data1.arrTimeInfo.add("180506");
            data1.arrTimeInfo.add("180706");
            data1.arrTimeInfo.add("180906");
            arrCatalogueData.add(data1);
        }
        updateCatalogueList(arrCatalogueData);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onTouch(View touchView, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //生成一个DatePickerDialog对象，并显示。显示的DatePickerDialog控件可以选择年月日，并设置
            final EditText editText = (EditText) touchView;
            final Calendar calendar = Calendar.getInstance(Locale.CHINA);
            try {
                Editable editable = editText.getText();
                String str = editable.toString();
                Date date = mDateformat.parse(str);

                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    //修改日历控件的年，月，日
                    //这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
                    calendar.set(Calendar.YEAR,year);
                    calendar.set(Calendar.MONTH,month);
                    calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                    //将页面TextView的显示更新为最新时间
                    editText.setText(mDateformat.format(calendar.getTime()));
                     //向设备请求获取测量目录信息
                    //requestCatalogue();
                }
            }, year, month, day);
            datePickerDialog.show();
            return true;
        }

        return false;
    }
    //从设备获取线路和接头信息后更新下拉列表
    private void updateLineJoint(ArrayList<LineData>  lineList){
        mLineDataList = lineList;
        List<String> lineNameList = new ArrayList<String>();
        List<String> jointNameList = new ArrayList<String>();;
        for(LineData line:mLineDataList)
        {
            if(lineNameList.isEmpty()){
                jointNameList = line.arrJointName;
            }
            lineNameList.add(line.lineName);
        }
        ArrayAdapter adapterLine = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, lineNameList);
        mSpinnerLine.setAdapter(adapterLine);
        ArrayAdapter adapterjoint = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, jointNameList);
        mSpinnerJoint.setAdapter(adapterjoint);
    }
    //从设备获取目录信息
    private void requestCatalogue()
    {
        //从界面获取过滤条件
        FilterCatalogueData filterCatalogue = new FilterCatalogueData();
        filterCatalogue.lineName = (String) mSpinnerLine.getSelectedItem();
        filterCatalogue.jointName = (String) mSpinnerJoint.getSelectedItem();
        String pdType = (String) mSpinnerPdType.getSelectedItem();

        if(pdType.equals("检测到放电信号"))filterCatalogue.conclusionType = "1";
        else if(pdType.equals("未检测到放电信号"))filterCatalogue.conclusionType = "0";
        else if(pdType.equals("全部"))filterCatalogue.conclusionType = "";

        String startDateString = mEditStartDate.getText().toString();
        String endDateString = mEditEndDate.getText().toString();
        try {
            Date startDate = mDateformat.parse(startDateString);
            Date endDate = mDateformat.parse(endDateString);
            filterCatalogue.startTime = startDate.getTime();
            filterCatalogue.endTime = endDate.getTime();
        }
        catch(ParseException e){
            e.printStackTrace();
        }
        //通过http消息请求设备获取目录
        PdHttpRequest.doFilterCatalogue(filterCatalogue,new PdHttpRequest.Callback() {
           @Override
            public void onResponse(PdHttpRequest.ResponseBase resp) {
                if(resp.respCode == 0)//返回正确
                {
                    PdHttpRequest.FilterCatalogueResp catalogueResp = (PdHttpRequest.FilterCatalogueResp)resp;
                    updateCatalogueList(catalogueResp.catalogueList);
                }
                else{//返回失败
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
             }}});
    }
    //更新目录列表
    private void updateCatalogueList(ArrayList<CatalogueData> arrCatalogueData)
    {
        if(arrCatalogueData.size()>0){
            mListViewCatalogue.setVisibility(View.VISIBLE);
            mNoDataView.setVisibility(View.GONE);

            mCatalogueAdapter.setList(arrCatalogueData);
            mListViewCatalogue.setAdapter(mCatalogueAdapter);
        }
        else{
            mListViewCatalogue.setVisibility(View.GONE);
            mNoDataView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void selectCatalogue(int nIndex) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        if(buttonView.getId() == R.id.btnCheckAll)
        {
            int i = mListViewCatalogue.getCount();

            if(isChecked){
                 mTextCheckedNum.setText("已选中5项");
                 mBtnReadCatalogue.setEnabled(true);
                 mBtnReadCatalogue.setBackgroundColor(Color.parseColor("#5891F8"));
             }
             else{
                 mTextCheckedNum.setText("已选中0项");
                 mBtnReadCatalogue.setEnabled(false);
                 mBtnReadCatalogue.setBackgroundColor(Color.parseColor("#C0C0C0"));
             }
        }
    }
}
