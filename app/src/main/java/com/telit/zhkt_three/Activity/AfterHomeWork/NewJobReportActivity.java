package com.telit.zhkt_three.Activity.AfterHomeWork;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.Gson;
import com.telit.zhkt_three.Activity.BaseActivity;
import com.telit.zhkt_three.Activity.HomeScreen.MainActivity;
import com.telit.zhkt_three.Activity.HomeWork.HomeWorkDetailActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.JavaBean.Gson.ChartBeans;
import com.telit.zhkt_three.JavaBean.Gson.ReportDataBean;
import com.telit.zhkt_three.JavaBean.HomeWork.ReportBarXAxisInfo;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class NewJobReportActivity extends BaseActivity implements OnChartValueSelectedListener, View.OnClickListener {

    private Unbinder unbinder;

    @BindView(R.id.report_close)
    ImageView report_close;
    @BindView(R.id.report_title)
    TextView report_title;
    @BindView(R.id.report_enter_detail)
    TextView report_enter_detail;
    @BindView(R.id.pie_chart)
    PieChart pie_chart;
    @BindView(R.id.bar_chart)
    BarChart bar_chart;
    @BindView(R.id.radar_chart)
    RadarChart radar_chart;

    private String homeworkId;

    //??????
    private Typeface tfRegular;
    private Typeface tfLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job_report);
        unbinder = ButterKnife.bind(this);

        String resultBackJson = getIntent().getStringExtra("Report_Json");
        if (!TextUtils.isEmpty(resultBackJson)) {
            reportJson = resultBackJson;
        }

        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        Gson gson = new Gson();
        reportDataBean = gson.fromJson(reportJson, ReportDataBean.class);
        QZXTools.logE("reportDataBean=" + reportDataBean, null);
        if (reportDataBean == null) {
            QZXTools.popCommonToast(this, "??????????????????????????????", false);
            return;
        }

        initView();
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }


    private ReportDataBean reportDataBean;

    //??????Json
    private String reportJson = "{\n" +
            "\t\"chartData\": {\n" +
            "\t\t\"pieChartData\": [{\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": \"2,0,\",\n" +
            "\t\t\t\"questionType\": null,\n" +
            "\t\t\t\"status\": 1,\n" +
            "\t\t\t\"scoreRate\": \"100.0%\"\n" +
            "\t\t}],\n" +
            "\t\t\"radarChartData\": [{\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 0,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"55.6%\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 1,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"50.0%\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 2,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"22.2%\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 3,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"90.0%\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 4,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"27.8%\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 0,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": null,\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": null,\n" +
            "\t\t\t\"questionType\": 5,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": \"50.0%\"\n" +
            "\t\t}],\n" +
            "\t\t\"barChartData\": [{\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 2,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": \"6624\",\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": \"0\",\n" +
            "\t\t\t\"questionType\": 0,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": null\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 2,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": \"79306\",\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": \"1\",\n" +
            "\t\t\t\"questionType\": 1,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": null\n" +
            "\t\t}, {\n" +
            "\t\t\t\"workId\": null,\n" +
            "\t\t\t\"scoreLevel\": null,\n" +
            "\t\t\t\"score\": 0,\n" +
            "\t\t\t\"total\": 5,\n" +
            "\t\t\t\"percentage\": 0,\n" +
            "\t\t\t\"totalFalse\": 0,\n" +
            "\t\t\t\"percentageFalse\": 0,\n" +
            "\t\t\t\"rank\": 0,\n" +
            "\t\t\t\"studentName\": null,\n" +
            "\t\t\t\"classtName\": null,\n" +
            "\t\t\t\"questionId\": \"79488\",\n" +
            "\t\t\t\"index\": null,\n" +
            "\t\t\t\"qindex\": \"2\",\n" +
            "\t\t\t\"questionType\": 3,\n" +
            "\t\t\t\"status\": null,\n" +
            "\t\t\t\"scoreRate\": null\n" +
            "\t\t}]\n" +
            "\t},\n" +
            "\t\"byhand\": \"2\"\n" +
            "}";

    /**
     * ???????????????
     */
    private void initView() {

        //??????????????????????????????????????????
        report_enter_detail.setOnClickListener(this);
        report_close.setOnClickListener(this);

        //-----------------------------------------------------------------??????????????????
        pie_chart.setUsePercentValues(true);
        pie_chart.getDescription().setEnabled(false);
        pie_chart.setExtraOffsets(5, 10, 5, 5);

        pie_chart.setDragDecelerationFrictionCoef(0.95f);

        pie_chart.setCenterTextTypeface(tfRegular);
        pie_chart.setCenterText("?????????????????????");
        pie_chart.setDrawCenterText(true);

        pie_chart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        pie_chart.setDrawHoleEnabled(true);
        pie_chart.setHoleColor(Color.WHITE);

        pie_chart.setTransparentCircleColor(Color.WHITE);
        pie_chart.setTransparentCircleAlpha(110);

        pie_chart.setHoleRadius(58f);
        pie_chart.setTransparentCircleRadius(61f);

        pie_chart.setRotationAngle(0);
        // enable rotation of the pie_chart by touch
        pie_chart.setRotationEnabled(true);
        pie_chart.setHighlightPerTapEnabled(true);

        pie_chart.setOnChartValueSelectedListener(this);

        pie_chart.animateY(1400, Easing.EaseInOutQuad);

        Legend l = pie_chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
//        l.setEnabled(false);//????????????????????????????????????

        PieMarkerView mv_pie = new PieMarkerView(this);
        mv_pie.setChartView(pie_chart);
        pie_chart.setMarker(mv_pie);

        //-----------------------------------------------------------------??????????????????

        //-----------------------------------------------------------------??????????????????
        bar_chart.setOnChartValueSelectedListener(this);

        bar_chart.setDrawBarShadow(false);
        bar_chart.setDrawValueAboveBar(true);

        bar_chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        bar_chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        bar_chart.setPinchZoom(false);

        bar_chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        BarAxisValueFormatter xAxisFormatter = new BarAxisValueFormatter();
        XAxis xAxis = bar_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(6);
        xAxis.setValueFormatter(xAxisFormatter);

        ValueFormatter custom = new MyValueFormatter("???");

        YAxis leftAxis = bar_chart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        //????????????????????????
        YAxis rightAxis = bar_chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        rightAxis.setEnabled(false);//???????????????


        //???????????????
//        Legend l_bar = bar_chart.getLegend();
//        l_bar.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        l_bar.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        l_bar.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        l_bar.setDrawInside(false);
//        l_bar.setForm(Legend.LegendForm.SQUARE);
//        l_bar.setFormSize(9f);
//        l_bar.setTextSize(11f);
//        l_bar.setXEntrySpace(4f);

        //??????????????????????????????
        BarXYMarkerView mv = new BarXYMarkerView(this, xAxisFormatter);
        mv.setChartView(bar_chart); // For bounds control
        bar_chart.setMarker(mv); // Set the marker to the chart
        //-----------------------------------------------------------------??????????????????

        //-----------------------------------------------------------------??????????????????
        //??????????????????
//        radar_chart.setBackgroundColor(Color.rgb(60, 65, 82));

        radar_chart.getDescription().setEnabled(false);

        radar_chart.setWebLineWidth(1f);
        radar_chart.setWebColor(Color.LTGRAY);
        radar_chart.setWebLineWidthInner(1f);
        radar_chart.setWebColorInner(Color.LTGRAY);
        radar_chart.setWebAlpha(100);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        //??????????????????
        MarkerView mv_radar = new RadarMarkerView(this, R.layout.radar_markerview);
        mv_radar.setChartView(radar_chart); // For bounds control
        radar_chart.setMarker(mv_radar); // Set the marker to the chart

        radar_chart.animateXY(1400, 1400, Easing.EaseInOutQuad);

        XAxis xAxis_radar = radar_chart.getXAxis();
        xAxis_radar.setTypeface(tfLight);
        xAxis_radar.setTextSize(9f);
        xAxis_radar.setYOffset(0f);
        xAxis_radar.setXOffset(0f);
        xAxis_radar.setValueFormatter(new ValueFormatter() {

            //?????????????????????
            private final String[] mActivities = new String[]{"???????????????", "???????????????", "?????????", "?????????", "?????????", "?????????"};

            @Override
            public String getFormattedValue(float value) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        //???????????????????????????
        xAxis_radar.setTextColor(Color.BLACK);

        YAxis yAxis = radar_chart.getYAxis();
        yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(6, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(80f);
        yAxis.setDrawLabels(false);

        Legend l_radar = radar_chart.getLegend();
        l_radar.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l_radar.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l_radar.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l_radar.setDrawInside(false);
        l_radar.setTypeface(tfLight);
        l_radar.setXEntrySpace(7f);
        l_radar.setYEntrySpace(5f);
        //?????????????????????
        l_radar.setTextColor(Color.BLACK);
        //-----------------------------------------------------------------??????????????????


        //-------------??????????????????????????????
        List<ChartBeans.ReportBean> pie_beans = reportDataBean.getChartData().getPieChartData();

        //???????????????????????????????????????
        Map<Integer, String> mapIndex = new LinkedHashMap<>();

        Float rightRatio = 0f;
        Float errorRatio = 0f;
        Float subjectRatio = 0f;

        for (ChartBeans.ReportBean pie_bean : pie_beans) {
            //????????????ID
            if (TextUtils.isEmpty(homeworkId)) {
                homeworkId = pie_bean.getWorkId();
            }
            if (pie_bean.getStatus() == 0) {
                //??????
                String scoreRate = pie_bean.getScoreRate();
                scoreRate = scoreRate.replace("%", "");
                errorRatio = Float.parseFloat(scoreRate);
            } else if (pie_bean.getStatus() == 1) {
                //??????
                String scoreRate = pie_bean.getScoreRate();
                scoreRate = scoreRate.replace("%", "");
                rightRatio = Float.parseFloat(scoreRate);
            } else {
                //?????????????????????2 === ??????
                String scoreRate = pie_bean.getScoreRate();
                scoreRate = scoreRate.replace("%", "");
                subjectRatio = Float.parseFloat(scoreRate);
            }
            mapIndex.put(pie_bean.getStatus(), pie_bean.getQindex());
        }

        mv_pie.setMapIndex(mapIndex);

        setPieChartData(rightRatio, errorRatio, subjectRatio);
        //-------------??????????????????????????????


        //-------------?????????????????????????????????
        List<ChartBeans.ReportBean> bar_beans = reportDataBean.getChartData().getBarChartData();

        //????????????
        parseQIndex(bar_beans);

        List<ReportBarXAxisInfo> xAxisStr = new ArrayList<>();

        int maxScore = 0;

        for (int k = 0; k < bar_beans.size(); k++) {
            //????????????
            bar_beans.get(k).setQindex(k + "");

            ReportBarXAxisInfo info = new ReportBarXAxisInfo();
            info.setMyScore(bar_beans.get(k).getScore());
            info.setTotalScore(bar_beans.get(k).getTotal());

            //????????????????????????
            if (maxScore < bar_beans.get(k).getTotal()) {
                maxScore = bar_beans.get(k).getTotal();
            }

            String qIndex = bar_beans.get(k).getQindex();
            int qType = bar_beans.get(k).getQuestionType();
            info.setIndex(Integer.parseInt(qIndex));

            switch (qType) {
                case Constant.Single_Choose:
                    info.setQuestionType("?????????");
                    break;
                case Constant.Multi_Choose:
                    info.setQuestionType("?????????");
                    break;
                case Constant.Fill_Blank:
                    info.setQuestionType("?????????");
                    break;
                case Constant.Subject_Item:
                    info.setQuestionType("?????????");
                    break;
                case Constant.Linked_Line:
                    info.setQuestionType("?????????");
                    break;
                case Constant.Judge_Item:
                    info.setQuestionType("?????????");
                    break;
            }

            xAxisStr.add(info);
        }

        //???????????????????????????Y?????????
        leftAxis.setLabelCount(maxScore, false);

        QZXTools.logE("before sort xAxisStr" + xAxisStr, null);
        Collections.sort(xAxisStr);
        QZXTools.logE("after sort xAxisStr" + xAxisStr, null);

        xAxisFormatter.setxAxisValues(xAxisStr);

        setBarChartData(bar_beans.size(), maxScore, xAxisStr);

        //-------------?????????????????????????????????

        //---------------?????????????????????????????????
        List<Float> list_radar = new ArrayList<>();

        List<ChartBeans.ReportBean> radar_beans = reportDataBean.getChartData().getRadarChartData();
        for (ChartBeans.ReportBean radar_bean : radar_beans) {
            String scoreRate = radar_bean.getScoreRate();
            scoreRate = scoreRate.replace("%", "");
            list_radar.add(Float.parseFloat(scoreRate));
        }

        setRadarChartData(list_radar);
        //---------------?????????????????????????????????
    }

    /**
     * ????????????????????????
     * ???????????????????????????????????????????????????
     */
    private void setPieChartData(float rightRatio, float errorRatio, float subjectRatio) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        if (rightRatio > 0f) {
            entries.add(new PieEntry(rightRatio, "??????", getResources().getDrawable(R.drawable.star)));
        }

        if (errorRatio > 0f) {
            entries.add(new PieEntry(errorRatio, "??????", getResources().getDrawable(R.drawable.star)));
        }

        if (subjectRatio > 0f) {
            entries.add(new PieEntry(subjectRatio, "??????", getResources().getDrawable(R.drawable.star)));
        }


        PieDataSet dataSet = new PieDataSet(entries, "?????????????????????");

        //???????????????
        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();

        colors.add(ColorTemplate.rgb("#00FF00"));
        colors.add(ColorTemplate.rgb("#FF0000"));
        colors.add(ColorTemplate.rgb("#FFFF00"));

        dataSet.setColors(colors);

        //---------------------????????????
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        //---------------------????????????

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pie_chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTypeface(tfLight);
        pie_chart.setData(data);

        // undo all highlights
        pie_chart.highlightValues(null);

        pie_chart.invalidate();
    }

    /**
     * ?????????????????????
     * ??????????????????????????????????????????
     *
     * @param count x??????????????????????????????
     *              //@param xStrList ???????????????????????????+??????
     */
    private void setBarChartData(int count, int maxScore, List<ReportBarXAxisInfo> list) {

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = list.get(i).getMyScore();
            values.add(new BarEntry(i, val, getResources().getDrawable(R.drawable.star)));
        }

        BarDataSet set1;

        if (bar_chart.getData() != null &&
                bar_chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) bar_chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            bar_chart.getData().notifyDataChanged();
            bar_chart.notifyDataSetChanged();

        } else {
            set1 = new BarDataSet(values, "??????????????????????????????");

            set1.setDrawIcons(false);

            //??????????????????
            int startColor1 = getResources().getColor(android.R.color.holo_orange_light);
            int startColor2 = getResources().getColor(android.R.color.holo_blue_light);
            int startColor3 = getResources().getColor(android.R.color.holo_orange_light);
            int startColor4 = getResources().getColor(android.R.color.holo_green_light);
            int startColor5 = getResources().getColor(android.R.color.holo_red_light);
            int startColor6 = getResources().getColor(android.R.color.holo_purple);
            int endColor1 = getResources().getColor(android.R.color.holo_blue_dark);
            int endColor2 = getResources().getColor(android.R.color.holo_purple);
            int endColor3 = getResources().getColor(android.R.color.holo_green_dark);
            int endColor4 = getResources().getColor(android.R.color.holo_red_dark);
            int endColor5 = getResources().getColor(android.R.color.holo_orange_dark);
            int endColor6 = getResources().getColor(android.R.color.holo_blue_dark);

            List<GradientColor> gradientColors = new ArrayList<>();
            gradientColors.add(new GradientColor(startColor1, endColor1));
            gradientColors.add(new GradientColor(startColor2, endColor2));
            gradientColors.add(new GradientColor(startColor3, endColor3));
            gradientColors.add(new GradientColor(startColor4, endColor4));
            gradientColors.add(new GradientColor(startColor5, endColor5));
            gradientColors.add(new GradientColor(startColor6, endColor6));

            set1.setGradientColors(gradientColors);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data_bar = new BarData(dataSets);
            data_bar.setValueTextSize(10f);
            data_bar.setValueTypeface(tfLight);
            data_bar.setBarWidth(0.9f);

            bar_chart.setData(data_bar);
        }

    }

    /**
     * ?????????????????????
     * ?????????????????????????????????????????????????????????
     */
    private void setRadarChartData(List<Float> floats) {
        ArrayList<RadarEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < floats.size(); i++) {
            entries.add(new RadarEntry(floats.get(i)));
        }

        RadarDataSet set2 = new RadarDataSet(entries, "??????????????????");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set2);

        RadarData data_radar = new RadarData(sets);
        data_radar.setValueTypeface(tfLight);
        data_radar.setValueTextSize(8f);
        data_radar.setDrawValues(false);
        data_radar.setValueTextColor(Color.WHITE);

        radar_chart.setData(data_radar);
        radar_chart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;

        if (e instanceof PieEntry) {
            QZXTools.logE("pie entry ......", null);
            PieEntry pieEntry = (PieEntry) e;
        }

        QZXTools.logE("Value: " + e.getY() + ", xIndex: " + e.getX()
                + ", DataSet index: " + h.getDataSetIndex(), null);

    }

    @Override
    public void onNothingSelected() {
        QZXTools.logE("onNothingSelected", null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report_close:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                finish();
                break;
            case R.id.report_enter_detail:
                Intent intent_detail = new Intent(this, HomeWorkDetailActivity.class);
                //???????????????????????????
                intent_detail.putExtra("Report_Json", reportJson);
                intent_detail.putExtra("homeworkId", homeworkId);
                intent_detail.putExtra("status", "2");//?????????
                intent_detail.putExtra("byHand", reportDataBean.getByhand());//1-byHand
                startActivity(intent_detail);

                finish();
                break;
        }
    }

    /**
     * ????????????????????????0-0 0-1 1-0 1-1 ???
     */
    private void parseQIndex(List<ChartBeans.ReportBean> bar_beans) {
        for (ChartBeans.ReportBean reportBean : bar_beans) {
            String qIndex = reportBean.getQindex();
            String replace = qIndex.replace("-", "");
            reportBean.setQindex(replace);
        }

        QZXTools.logE("before bar_beans=" + bar_beans, null);

        //??????
        Collections.sort(bar_beans, new Comparator<ChartBeans.ReportBean>() {
            @Override
            public int compare(ChartBeans.ReportBean o1, ChartBeans.ReportBean o2) {
                int diff = Integer.parseInt(o1.getQindex()) - Integer.parseInt(o2.getQindex());
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });

        QZXTools.logE("after bar_beans=" + bar_beans, null);
    }

}
