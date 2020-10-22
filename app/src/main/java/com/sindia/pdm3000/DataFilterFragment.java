package com.sindia.pdm3000;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sindia.pdm3000.helper.PropertyHelper;
import com.sindia.pdm3000.util.PDFileUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataFilterFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private Spinner mLineNameSpinner;
    private Spinner mLinePosSpinner;
    private EditText mStartDateEdit;
    private EditText mEndDateEdit;

    List<String> mLineNameList;
    List<String> mLinePosList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_filter, container, false);

        // 控件赋值
        mLineNameSpinner = root.findViewById(R.id.spinnerLineName);
        mLinePosSpinner = root.findViewById(R.id.spinnerLinePos);
        mStartDateEdit = root.findViewById(R.id.editTextStartDate);
        mEndDateEdit = root.findViewById(R.id.editTextEndDate);

        // 控件初始化
        mLineNameSpinner.setOnItemSelectedListener(this);
        mLinePosSpinner.setOnItemSelectedListener(this);
        mStartDateEdit.setOnTouchListener(this);
        mEndDateEdit.setOnTouchListener(this);

        // 设置线路名称数据源
        Context context = root.getContext();
        mLineNameList = PDFileUtil.getLineNames(context);
        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.spinitem_common, R.id.textViewTitle, mLineNameList);
        mLineNameSpinner.setAdapter(adapter);

        // 读取上次默认配置
        PropertyHelper helper = new PropertyHelper(context);
        helper.loadFromFile("data_filter.prop");
        String lineName = helper.getString("line_name");
        String linePos = helper.getString("line_pos");
        String startDate = helper.getString("start_date");
        String endDate = helper.getString("end_date");

        // 设置界面默认数据
        int lineNameIndex = mLineNameList.indexOf(lineName);
        mLineNameSpinner.setSelection(lineNameIndex, false);

        int linePosIndex = mLineNameList.indexOf(linePos);
        mLinePosSpinner.setSelection(linePosIndex, false);

        final Calendar calendar = Calendar.getInstance(Locale.CHINA);
        if (startDate.isEmpty()) {
            startDate = mDateformat.format(calendar.getTime());
        }
        if (endDate.isEmpty()) {
            endDate = mDateformat.format(calendar.getTime());
        }
        mStartDateEdit.setText(startDate);
        mEndDateEdit.setText(endDate);

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int id = adapterView.getId();
        if (id == R.id.spinnerLineName) {// adapterView.equals(mLineNameSpinner)) { // 线路名称
            String lineName = (String) mLineNameSpinner.getSelectedItem();
            mLinePosList = PDFileUtil.getLinePosNames(getActivity(), lineName);
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.spinitem_common, R.id.textViewTitle, mLinePosList);
            mLinePosSpinner.setAdapter(adapter);
            //mLinePosSpinner.setOnItemSelectedListener(this);

            saveRuntimeProperties();
        } else if (id == R.id.spinnerLinePos) {
            saveRuntimeProperties();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.e("", "");
    }

    //创建SimpleDateFormat对象实例并定义好转换格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss");

    //DateFormat format =  DateFormat.getDateTimeInstance();
    private static final DateFormat mDateformat =  DateFormat.getDateInstance(DateFormat.SHORT); // XXXX/XX/XX

    //获取日期格式器对象
    //Calendar calendar = Calendar.getInstance(Locale.CHINA);

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

                    saveRuntimeProperties();
                }
            }, year, month, day);
            datePickerDialog.show();
            //updateTimeShow();
            //将页面TextView的显示更新为最新时间
            return true;
        }
        /*
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = View.inflate(this, R.layout.date_time_dialog, null);
            final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
            final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
            builder.setView(view);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(Calendar.MINUTE);

            if (v.getId() == R.id.et_start_time) {
                final int inType = etStartTime.getInputType();
                etStartTime.setInputType(InputType.TYPE_NULL);
                etStartTime.onTouchEvent(event);
                etStartTime.setInputType(inType);
                etStartTime.setSelection(etStartTime.getText().length());

                builder.setTitle("选取起始时间");
                builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getCurrentHour())
                                .append(":").append(timePicker.getCurrentMinute());

                        etStartTime.setText(sb);
                        etEndTime.requestFocus();

                        dialog.cancel();
                    }
                });

            } else if (v.getId() == R.id.et_end_time) {
                int inType = etEndTime.getInputType();
                etEndTime.setInputType(InputType.TYPE_NULL);
                etEndTime.onTouchEvent(event);
                etEndTime.setInputType(inType);
                etEndTime.setSelection(etEndTime.getText().length());

                builder.setTitle("选取结束时间");
                builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getCurrentHour())
                                .append(":").append(timePicker.getCurrentMinute());
                        etEndTime.setText(sb);

                        dialog.cancel();
                    }
                });
            }

            Dialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }

 */
    return false;
    }

    public void onShowFragment() {
    }

    public void onHideFragment() {

    }

    // 保存运行时的属性
    private void saveRuntimeProperties() {
        String lineName = (String) mLineNameSpinner.getSelectedItem();
        String linePos = (String) mLinePosSpinner.getSelectedItem();
        String startDate = mStartDateEdit.getText().toString();
        String endDate = mEndDateEdit.getText().toString();

        PropertyHelper helper = new PropertyHelper(getContext());
        helper.setString("line_name", lineName);
        helper.setString("line_pos", linePos);
        helper.setString("start_date", startDate);
        helper.setString("end_date", endDate);
        helper.saveToFile("data_filter.prop");
    }
}
