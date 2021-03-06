package com.telit.zhkt_three.Fragment.PreView;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.telit.zhkt_three.Activity.MistakesCollection.RangeDayDecorator;
import com.telit.zhkt_three.Adapter.PreView.PreCloudRVAdapter;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.ToUsePullView;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.Fragment.Dialog.TBSDownloadDialog;
import com.telit.zhkt_three.JavaBean.Gson.PreQueryDiskBeans;
import com.telit.zhkt_three.JavaBean.Gson.PreShareFilesBeans;
import com.telit.zhkt_three.JavaBean.Gson.SubjectiveListBean;
import com.telit.zhkt_three.JavaBean.MistakesCollection.SubjectBean;
import com.telit.zhkt_three.JavaBean.PreView.Disk;
import com.telit.zhkt_three.JavaBean.PreView.PreViewDisplayBean;
import com.telit.zhkt_three.JavaBean.PreView.RecordStatus;
import com.telit.zhkt_three.JavaBean.PreView.SysFileShare;
import com.telit.zhkt_three.MediaTools.CommentActivity;
import com.telit.zhkt_three.MediaTools.CommentCommitActivity;
import com.telit.zhkt_three.MediaTools.audio.AudioPlayActivity;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MediaTools.video.VideoPlayerActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.SerializeUtil;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ViewUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.zbv.basemodel.WpsUtil;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * author: qzx
 * Date: 2019/12/20 13:49
 * <p>
 * ?????????????????????
 * <p>
 * todo ????????????????????????????????????????????????=>http????????????
 * ??????????????????TextView???Tag?????????ids,?????????????????????????????????ids
 * ??????????????????????????????????????????????????????????????????????????????????????????
 * ??????????????????????????????????????????
 * ??????????????????tbs?????????????????????
 * ????????????????????????????????????files/disk
 * todo ??????????????????????????????
 * <p>
 * todo ?????????????????????????????????????????????????????????????????????
 * <p>
 * notes ??????????????????????????????????????????????????????????????????????????????
 * <p>
 * ??????SwipeRefreshLayout + RecyclerView  ???????????? row=4
 */
public class CollectionResourcesFragment extends android.support.v4.app.Fragment implements View.OnClickListener,
        ValueCallback<String>, ZBVPermission.PermPassResult, WpsUtil.WpsInterface, ToUsePullView.SpinnerClickInterface, PreCloudRVAdapter.OnItemCollectionClickListener {
    private LinearLayout mistakes_pull_all;
    private LinearLayout pull_layout;
    private ToUsePullView pull_subject;
    private ToUsePullView pull_date;
    private FrameLayout pull_tag;
    private ImageView pull_icon;
    private LinearLayout mistakes_custom_date_layout;
    private TextView mistakes_start_tv;
    private TextView mistakes_end_tv;

    private Map<String, String> subjectMap;
    private List<String> dateTime;

    private String startDate;
    private String endDate;

    private LinearLayout pre_cloud_bread;
    private TextView pre_bread_tv_home;
    private SwipeRefreshLayout pre_cloud_content_swipeRefresh;
    private RecyclerView pre_cloud_content_recycler;

    private CircleProgressDialogFragment circleProgressDialogFragment;

    //-----------????????????????????????
    private LinearLayout request_retry_layout;
    private TextView request_retry;
    private LinearLayout leak_resource_layout;
    private LinearLayout leak_net_layout;
    TextView link_network;

    private GridLayoutManager gridLayoutManager;
    private PreCloudRVAdapter collectResourcesRVAdapter;
    private List<PreViewDisplayBean> preViewDiaplayBeans;
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operator_Success = 2;
    private static final int Operate_Subject_Query_Success = 3;
    private static final int Operate_Delay_Date_Query = 4;

    private static boolean isShow=false;
    private Handler mHandler = new Handler() {
        @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Server_Error:
                    if (isShow){
                        QZXTools.popToast(getContext(), "??????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        pre_cloud_content_swipeRefresh.setRefreshing(false);

                        if (leak_resource_layout != null || request_retry_layout != null) {
                            leak_resource_layout.setVisibility(View.GONE);
                            request_retry_layout.setVisibility(View.VISIBLE);
                        }

                        if (pre_cloud_content_swipeRefresh != null && pre_cloud_content_swipeRefresh.isRefreshing())
                            pre_cloud_content_swipeRefresh.setRefreshing(false);
                    }
                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popToast(getContext(), "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        if (leak_resource_layout != null || request_retry_layout != null) {
                            leak_resource_layout.setVisibility(View.GONE);
                            request_retry_layout.setVisibility(View.VISIBLE);
                        }

                        if (pre_cloud_content_swipeRefresh != null && pre_cloud_content_swipeRefresh.isRefreshing())
                            pre_cloud_content_swipeRefresh.setRefreshing(false);


                    }
                    break;
                case Operator_Success:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        if (pre_cloud_content_swipeRefresh.isRefreshing())
                            pre_cloud_content_swipeRefresh.setRefreshing(false);

                        QZXTools.logE("mData Size=" + preViewDiaplayBeans.size(), null);

                        if (preViewDiaplayBeans.size() >= totalDataCount) {
                            isNoDatas = true;
                        } else {
                            isNoDatas = false;
                        }

                        //?????????????????????????????????
                        if (preViewDiaplayBeans.size() > 0) {
                            request_retry_layout.setVisibility(View.GONE);
                            leak_resource_layout.setVisibility(View.GONE);
                        } else {
                            request_retry_layout.setVisibility(View.GONE);
                            leak_resource_layout.setVisibility(View.VISIBLE);
                        }
                        collectResourcesRVAdapter.setmDatas(preViewDiaplayBeans);
                    }
                    break;
                case Operate_Subject_Query_Success:
                    if (isShow){
                        List<String> subjectiveList = new ArrayList<String>(subjectMap.keySet());
                        pull_subject.setDataList(subjectiveList);
                        pull_subject.setPullContent(subjectiveList.get(0));
                    }
                    break;
                case Operate_Delay_Date_Query:
                    if (isShow){
                        requestDatas(true, true);
                        datePopup.dismiss();
                    }
                    break;
            }
        }
    };

    /**
     * ?????????????????????
     */
    private boolean isNoDatas = false;

    private int totalDataCount;
    private WindowManager windowManager;
    private View pinglunView;
    private WpsUtil wpsUtil;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_resources_layout, container, false);

        EventBus.getDefault().register(this);
        isShow=true;

        wpsUtil = new WpsUtil(this,"",false,getActivity());

        //?????????????????????????????????
        MyApplication.getInstance().PreMainDian(MyApplication.FLAG_PRE_ONE, null);
        MyApplication.getInstance().PreMainDian(MyApplication.FLAG_PRE_TWO, null);

        pre_cloud_bread = view.findViewById(R.id.pre_cloud_bread);
        pre_bread_tv_home = view.findViewById(R.id.pre_bread_tv_home);
        pre_cloud_content_swipeRefresh = view.findViewById(R.id.collect_resources_content_swipeRefresh);
        pre_cloud_content_recycler = view.findViewById(R.id.collect_resources_content_recycler);

        //????????????
        request_retry_layout = view.findViewById(R.id.request_retry_layout);
        request_retry = view.findViewById(R.id.request_retry);
        leak_resource_layout = view.findViewById(R.id.leak_resource_layout);
        leak_net_layout = view.findViewById(R.id.leak_net_layout);
        link_network = view.findViewById(R.id.link_network);

        request_retry.setOnClickListener(this);
        link_network.setOnClickListener(this);
        pre_bread_tv_home.setOnClickListener(this);

        gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        pre_cloud_content_recycler.setLayoutManager(gridLayoutManager);
        preViewDiaplayBeans = new ArrayList<>();
        collectResourcesRVAdapter = new PreCloudRVAdapter(getActivity(), preViewDiaplayBeans,"2");
        collectResourcesRVAdapter.setOnItemCollectionClickListener(this);
        pre_cloud_content_recycler.setAdapter(collectResourcesRVAdapter);

        pre_cloud_content_swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (pre_cloud_bread.getChildCount() > 1) {
                    requestQueryDir(curIds, isHadParentId, true, true);
                } else {
                    requestDatas(true, true);
                }
            }
        });

        //RV????????????
        pre_cloud_content_recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                QZXTools.logE("isNoDatas=" + isNoDatas, null);

                //??????????????????
                if (isNoDatas) {
                    return;
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        gridLayoutManager.findLastVisibleItemPosition() >= preViewDiaplayBeans.size() - PageSize) {
                    QZXTools.logE("???????????????... curPageNo=" + curPageNo
                            + ";lastPosition=" + gridLayoutManager.findLastVisibleItemPosition(), null);
                    if (pre_cloud_bread.getChildCount() > 1) {
                        requestQueryDir(curIds, isHadParentId, false, false);
                    } else {
                        requestDatas(false, false);
                    }
                }
            }
        });

        mistakes_pull_all = view.findViewById(R.id.mistakes_pull_all);
        pull_layout = view.findViewById(R.id.pull_layout);
        pull_subject = view.findViewById(R.id.pull_subject);
        pull_date = view.findViewById(R.id.pull_date);
        pull_tag = view.findViewById(R.id.pull_tag);
        pull_icon = view.findViewById(R.id.pull_icon);
        mistakes_custom_date_layout = view.findViewById(R.id.mistakes_custom_date_layout);
        mistakes_start_tv = view.findViewById(R.id.mistakes_start_tv);
        mistakes_end_tv = view.findViewById(R.id.mistakes_end_tv);

        pull_tag.setOnClickListener(this);
        mistakes_custom_date_layout.setOnClickListener(this);

        pull_subject.setSpinnerClick(this);
        pull_date.setSpinnerClick(this);

        initData();

        if (!isWpsBack){
            requestDatas(false, true);
        }
        return view;
    }


    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        if (circleProgressDialogFragment != null) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        ZBVPermission.getInstance().recyclerAll();
        mHandler.removeCallbacksAndMessages(null);
        QZXTools.setmToastNull();
        isShow=false;
        super.onDestroyView();

    }
    /**
     * ??????????????????????????????????????????LinearLayout????????????????????????TV??????????????????View?
     */
    private void addBreadView(String breadName, String ids) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bread_item_layout, null);
        TextView tv_bread = view.findViewById(R.id.pre_cloud_tv_bread);
        tv_bread.setText(breadName);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(ids);
        stringBuffer.append(":");
        stringBuffer.append(pre_cloud_bread.getChildCount());
        tv_bread.setTag(stringBuffer.toString());
        tv_bread.setOnClickListener(this);

        //??????view?????????tag
        view.setTag(stringBuffer.toString());

        pre_cloud_bread.addView(view);
    }
    private boolean isShareFileRequestFail = false;

    /**
     * ????????????????????????id
     * ??????????????????????????????????????????
     */
    private String shareTitle;
    private String shareId;

    /**
     * ????????????????????????????????????1
     */
    private int curPageNo = 1;
    private static final int PageSize = 20;

    /**
     * ?????????ShareFile
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestDatas(boolean isRefresh, boolean isInit) {
        if (!QZXTools.isNetworkAvailable()) {
            leak_net_layout.setVisibility(View.VISIBLE);
            return;
        } else {
            leak_net_layout.setVisibility(View.GONE);
        }

        QZXTools.logE("requestDatas isRefresh=" + isRefresh + ";isInit=" + isInit, null);

        if (isInit) {
            if (preViewDiaplayBeans != null && preViewDiaplayBeans.size() > 0) {
                curPageNo = 1;
                preViewDiaplayBeans.clear();
                collectResourcesRVAdapter.notifyDataSetChanged();
            }
        } else {
            curPageNo++;
        }

        if (isRefresh) {
            pre_cloud_content_swipeRefresh.setRefreshing(true);
        } else {
        /*    if (circleProgressDialogFragment == null) {
                circleProgressDialogFragment = new CircleProgressDialogFragment();
            }
            circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());*/

        }


       // String url = UrlUtils.BaseUrl + UrlUtils.ShareFile;
        String url = UrlUtils.BaseUrl + UrlUtils.stuQueryDir;
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("classId", UserUtils.getClassId());
        paraMap.put("studentId", UserUtils.getUserId());
        //paraMap.put("dateSize", -1 + "");
        paraMap.put("pageNo", curPageNo + "");
        //????????????????????????
        paraMap.put("pageSize", PageSize + "");

        //?????????????????????????????????????????????
        if (!TextUtils.isEmpty(subjectMap.get(pull_subject.getPullContent()))) {
            paraMap.put("subjectId", subjectMap.get(pull_subject.getPullContent()));
        }

        paraMap.put("collectionStartTime", startDate);
        paraMap.put("collectionEndTime", endDate);
        paraMap.put("collectionState", "1");

        QZXTools.logE("paraMap:"+new Gson().toJson(paraMap),null);

        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, paraMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
                isShareFileRequestFail = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("share file resultJson=" + resultJson, null);
                    try {
                        Gson gson = new Gson();
                        PreShareFilesBeans preShareFilesBeans = gson.fromJson(resultJson, PreShareFilesBeans.class);
                        List<SysFileShare> fileShares = preShareFilesBeans.getResult();

                        totalDataCount = preShareFilesBeans.getTotal();

                        if (fileShares.size() > 0) {
                            isNoDatas = false;
                        } else {
                            isNoDatas = true;
                        }

                        for (SysFileShare sysFileShare : fileShares) {
                            PreViewDisplayBean preViewDiaplayBean = new PreViewDisplayBean();
                            //????????????
                            if (TextUtils.isEmpty(sysFileShare.getPreviewUrl())) {
                                preViewDiaplayBean.setType(1);
                            } else {
                                preViewDiaplayBean.setType(0);
                                //????????????
                                preViewDiaplayBean.setPreviewUrl(sysFileShare.getPreviewUrl());
                                //????????????
                                preViewDiaplayBean.setFileFormat(sysFileShare.getFileFormat());
                            }

                            //??????
                            if (TextUtils.isEmpty(sysFileShare.getName())) {
                                preViewDiaplayBean.setFileName(sysFileShare.getName());
                            } else {
                                preViewDiaplayBean.setFileName(sysFileShare.getFileName());
                            }

                            //?????????
                            if (!TextUtils.isEmpty(sysFileShare.getPreviewUrl())) {
                                preViewDiaplayBean.setThumbnail(sysFileShare.getPreviewUrl());
                            }

                            //??????
                            preViewDiaplayBean.setAvgStar(sysFileShare.getAvgStar());

                            //??????
                            if (!TextUtils.isEmpty(sysFileShare.getCreateDate())) {
                                preViewDiaplayBean.setCreateDate(sysFileShare.getCreateDate());
                            }

                            preViewDiaplayBean.setCollectionState(sysFileShare.getCollectionState());
                            preViewDiaplayBean.setCollectionTime(sysFileShare.getCollectionTime());
                            preViewDiaplayBean.setCollectionId(sysFileShare.getCollectionId());
                            preViewDiaplayBean.setFileId(sysFileShare.getFileId()+"");

                            //fileId
                            if (!TextUtils.isEmpty(sysFileShare.getFileId()+"")) {
                                preViewDiaplayBean.setFileId(sysFileShare.getFileId()+"");
                            }

                            //source
                          //  preViewDiaplayBean.setSource(sysFileShare.getSource());

                            //savePath
                            preViewDiaplayBean.setSavePath(sysFileShare.getSavePath());

                            //????????????
                            preViewDiaplayBean.setShareTitle(sysFileShare.getName());
                            //??????item id
                            preViewDiaplayBean.setCommentId(sysFileShare.getCommentId());
                            //???????????????
                           // preViewDiaplayBean.setCommentContent(sysFileShare.getResComment());
                            //?????????????????????
                            preViewDiaplayBean.setStatus(sysFileShare.getState());
                            preViewDiaplayBean.setShareId(sysFileShare.getShareId());
                            preViewDiaplayBean.setShareTitle(sysFileShare.getName());
                            preViewDiaplayBeans.add(preViewDiaplayBean);

                        }

                        mHandler.sendEmptyMessage(Operator_Success);
                    }catch (Exception e){
                        e.fillInStackTrace();
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        mHandler.sendEmptyMessage(Server_Error);
                      //  QZXTools.popToast(MyApplication.getInstance(),"????????????",true);
                    }

                } else {
                    mHandler.sendEmptyMessage(Error404);
                    isShareFileRequestFail = true;
                }
            }
        });

    }

    /**
     * ??????????????????????????????ids,??????????????????????????????parentId
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestQueryDir(String ids, boolean isHadParentId, boolean isRefresh, boolean isInit) {
        if (!QZXTools.isNetworkAvailable()) {
            leak_net_layout.setVisibility(View.VISIBLE);
            return;
        } else {
            leak_net_layout.setVisibility(View.GONE);
        }

        QZXTools.logE("requestQueryDir isRefresh=" + isRefresh + ";isInit=" + isInit, null);

        if (isInit) {
            if (preViewDiaplayBeans != null && preViewDiaplayBeans.size() > 0) {
                curPageNo = 1;
                preViewDiaplayBeans.clear();
               // preCloudRVAdapter.notifyDataSetChanged();
            }
        } else {
            curPageNo++;
        }

        if (isRefresh) {
            pre_cloud_content_swipeRefresh.setRefreshing(true);
        } else {
          /*  if (circleProgressDialogFragment == null) {
                circleProgressDialogFragment = new CircleProgressDialogFragment();
            }
            circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());*/
        }
        String url = UrlUtils.BaseUrl + UrlUtils.stuQueryDir;
        Map<String, String> paraMap = new HashMap<>();

        if (!TextUtils.isEmpty(source)) {
            paraMap.put("source", source);
        }

        if (isHadParentId) {
            paraMap.put("parentId", ids);
        } else {
            paraMap.put("ids", ids);
        }

        paraMap.put("pageNo", curPageNo + "");
        //????????????????????????
        paraMap.put("pageSize", PageSize + "");

        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, paraMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("query dir resultJson=" + resultJson, null);
                    Gson gson = new Gson();
                    PreQueryDiskBeans preQueryDiskBeans = gson.fromJson(resultJson, PreQueryDiskBeans.class);
                    List<Disk> fileShares = preQueryDiskBeans.getResult();

                    totalDataCount = preQueryDiskBeans.getTotal();

                    if (fileShares.size() > 0) {
                        isNoDatas = false;
                    } else {
                        isNoDatas = true;
                    }

                    for (Disk disk : fileShares) {
                        PreViewDisplayBean preViewDiaplayBean = new PreViewDisplayBean();
                        //????????????
                        if (TextUtils.isEmpty(disk.getPreviewUrl())) {
                            preViewDiaplayBean.setType(1);
                        } else {
                            preViewDiaplayBean.setType(0);
                            //????????????
                            preViewDiaplayBean.setPreviewUrl(disk.getPreviewUrl());
                            //????????????
                            preViewDiaplayBean.setFileFormat(disk.getFileFormat());
                        }

                        //??????
                        if (TextUtils.isEmpty(disk.getFileName())) {
                            preViewDiaplayBean.setFileName(disk.getName());
                        } else {
                            preViewDiaplayBean.setFileName(disk.getFileName());
                        }

                        //?????????
                        if (!TextUtils.isEmpty(disk.getThumbnail())) {
                            preViewDiaplayBean.setThumbnail(disk.getThumbnail());
                        }

                        //??????
                        preViewDiaplayBean.setAvgStar(disk.getAvgStar());

                        //??????
                        if (!TextUtils.isEmpty(disk.getCreateDate())) {
                            preViewDiaplayBean.setCreateDate(disk.getCreateDate());
                        }

                        preViewDiaplayBean.setCollectionState(disk.getCollectionState());
                        preViewDiaplayBean.setCollectionTime(disk.getCollectionTime());
                        preViewDiaplayBean.setCollectionId(disk.getCollectionId());
                        preViewDiaplayBean.setFileId(disk.getFileId()+"");

                        //id
                        if (disk.getId() != null) {
                            preViewDiaplayBean.setId(disk.getId());
                        }

                        //savePath
                        preViewDiaplayBean.setSavePath(disk.getSavePath());

                        preViewDiaplayBean.setShareTitle(shareTitle);
                        preViewDiaplayBean.setShareId(shareId);
                        preViewDiaplayBean.setResId(disk.getFileId() + "");

                        //???????????????
                        preViewDiaplayBean.setCommentContent(disk.getResComment());

                        preViewDiaplayBeans.add(preViewDiaplayBean);

                    }

                    mHandler.sendEmptyMessage(Operator_Success);
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    private String curIds;

    private int preValue;
    private boolean isShown = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.request_retry:
                //???????????????????????????:share????????????queryDir?????????
                if (isShareFileRequestFail) {
                    requestDatas(false, true);
                } else {
                    requestQueryDir(curIds, isHadParentId, false, true);
                }
                break;
            case R.id.link_network:
                QZXTools.enterWifiSetting(getActivity());
                break;
            case R.id.pre_bread_tv_home:
                isHadParentId = false;
                //????????????????????????
                requestDatas(false, true);
                //???????????????????????????????????????????????????
                pre_cloud_bread.removeViews(1, pre_cloud_bread.getChildCount() - 1);
                break;
            case R.id.pre_cloud_tv_bread:
                //?????????????????????
                String splitString = (String) v.getTag();
                String[] split = splitString.split(":");
                int index = Integer.parseInt(split[1]);
                //?????????
                if (index == 1) {
                    isHadParentId = false;
                } else {
                    isHadParentId = true;
                }
                requestQueryDir(split[0], isHadParentId, false, true);
                // 5??? ?????????3 => 0 0 1 2 3 4
                pre_cloud_bread.removeViews(index + 1, pre_cloud_bread.getChildCount() - index - 1);
                break;
            case R.id.mistakes_custom_date_layout:
                popupCustomDate();
                break;
            case R.id.pull_tag:
                preValue = 0;
                if (isShown) {//??????
                    isShown = false;
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, pull_layout.getMeasuredHeight());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {

                        }
                    });
                    valueAnimator.setDuration(500);
                    valueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            iconRotate(pull_icon, 180.0f, 0.0f);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pull_layout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    valueAnimator.start();
                } else {//??????
                    //??????
                    pull_layout.setVisibility(View.VISIBLE);
                    isShown = true;
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, pull_layout.getMeasuredHeight());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {

                        }
                    });
                    valueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            iconRotate(pull_icon, 0f, 180.0f);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {


                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    valueAnimator.setDuration(500);
                    valueAnimator.start();
                }
                break;
        }
    }

    /**
     * ???????????????180???
     */
    private void iconRotate(View view, float fromDegrees, float toDegrees) {
        RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);
        view.startAnimation(rotateAnimation);
    }

    private boolean isHadParentId = false;

    private TBSDownloadDialog tbsDownloadDialog;

    private static final String[] NeedPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};


    private PreViewDisplayBean clickTV;

    //????????????
    private String source;

   //?????????????????????
    @Subscriber(tag = Constant.click_cloud_item_ping_jia, mode = ThreadMode.MAIN)
    public void handlerCloudClickPingJia(PreViewDisplayBean clickTV) {
        //?????????????????????????????????????????????????????????????????????
        if (clickTV.getStatus().equals("0")){
            Intent intent_comment = new Intent(getContext(), CommentActivity.class);
            intent_comment.putExtra("shareId", clickTV.getShareId() + "");
            intent_comment.putExtra("shareTitle", clickTV.getShareTitle());
            intent_comment.putExtra("resId", clickTV.getFileId());
            intent_comment.putExtra("resName",  clickTV.getFileName());
            intent_comment.putExtra("studentId", UserUtils.getUserId() );
            intent_comment.putExtra("curPosition", clickTV.getCurPosition());
            startActivity(intent_comment);
        }else {
            //?????????????????????
            Intent intent_comment = new Intent(getContext(), CommentCommitActivity.class);
            intent_comment.putExtra("shareId", clickTV.getShareId() + "");
            intent_comment.putExtra("shareTitle", clickTV.getShareTitle());
            intent_comment.putExtra("resId", clickTV.getFileId());
            intent_comment.putExtra("resName",  clickTV.getFileName());
            intent_comment.putExtra("commentId", clickTV.getCommentId() );
            intent_comment.putExtra("resStars", clickTV.getResStars());
            startActivity(intent_comment);
        }
    }

    //????????????????????????
    @Subscriber(tag = Constant.click_cloud_item_ping_jia_submit, mode = ThreadMode.MAIN)
    public void handlerCloudClickPingJiaSubmit(PreViewDisplayBean preViewDisplayBean) {
        if (preViewDisplayBean!=null&&preViewDisplayBean.getCurPosition()!=-1){
            int curPosition = preViewDisplayBean.getCurPosition();
            preViewDiaplayBeans.get(curPosition).setStatus("1");
            preViewDiaplayBeans.get(curPosition).setAvgStar(preViewDisplayBean.getAvgStar());
            preViewDiaplayBeans.get(curPosition).setShareTitle(preViewDisplayBean.getShareTitle());
            collectResourcesRVAdapter.notifyDataSetChanged();

            QZXTools.logE("???????????????"+curPosition, null);
        }
    }

    /**
     * ???????????????????????????
     * <p>
     * ????????????????????????????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscriber(tag = Constant.CLICK_CLOUD_ITEM, mode = ThreadMode.MAIN)
    public void handlerCloudClick(PreViewDisplayBean clickTV) {
        if (!ViewUtils.isFastClick(1000)){
            return;
        }

        this.clickTV = clickTV;
        if (clickTV.getType() == 0) {
            //????????????????????????
            MyApplication.getInstance().PreMainDian(MyApplication.FLAG_PRE_THREE, clickTV.getResId());

            //?????????
            String format = clickTV.getFileFormat();
            QZXTools.logE("format=" + format, null);
            if (format.equals("mp4") || format.equals("avi")) {

                Intent intent_video = new Intent(getContext(), VideoPlayerActivity.class);
                intent_video.putExtra("VideoFilePath", clickTV.getPreviewUrl());
                intent_video.putExtra("VideoTitle", clickTV.getFileName());
                intent_video.putExtra("VideoThumbnail", clickTV.getThumbnail());

                intent_video.putExtra("shareId", clickTV.getShareId() + "");
                intent_video.putExtra("shareTitle", clickTV.getShareTitle());
                intent_video.putExtra("resId", clickTV.getResId());
                intent_video.putExtra("resName", clickTV.getFileName());

                intent_video.putExtra("resComment", clickTV.getCommentContent());

                getContext().startActivity(intent_video);

            } else if (format.equals("mp3")) {

                Intent intent = new Intent(getContext(), AudioPlayActivity.class);
                intent.putExtra("AudioFilePath", clickTV.getPreviewUrl());
                intent.putExtra("AudioFileName", clickTV.getFileName());

                intent.putExtra("shareId", clickTV.getShareId() + "");
                intent.putExtra("shareTitle", clickTV.getShareTitle());
                intent.putExtra("resId", clickTV.getResId());
                intent.putExtra("resName", clickTV.getFileName());

                intent.putExtra("resComment", clickTV.getCommentContent());

                getContext().startActivity(intent);

            } else if (format.equals("jpg") || format.equals("png") || format.equals("gif")) {

                Intent intent_img = new Intent(getContext(), ImageLookActivity.class);
                ArrayList<String> imgFilePathList = new ArrayList<>();
                imgFilePathList.add(clickTV.getPreviewUrl());
                intent_img.putStringArrayListExtra("imgResources", imgFilePathList);
                intent_img.putExtra("curImgIndex", 0);


                intent_img.putExtra("shareId", clickTV.getShareId() + "");
                intent_img.putExtra("shareTitle", clickTV.getShareTitle());
                intent_img.putExtra("resId", clickTV.getResId());
                intent_img.putExtra("resName", clickTV.getFileName());
                intent_img.putExtra("flag", "1");

                intent_img.putExtra("resComment", clickTV.getCommentContent());

//                QZXTools.logE("shareid=" + clickTV.getShareId() + ";shareTitle=" + clickTV.getShareTitle() +
//                        ";resId=" + clickTV.getResId() + ";resname=" + clickTV.getFileName(), null);

                getContext().startActivity(intent_img);

            } else if (format.equals("swf")) {
                //??????swf
//                Intent intent = new Intent(getContext(), UseBrowserActivity.class);
////                String encodeUrl = "";
////                try {
////                    encodeUrl = URLEncoder.encode(clickTV.getPreviewUrl(), Charset.forName("UTF-8").toString());
////                } catch (UnsupportedEncodingException e) {
////                    e.printStackTrace();
////                }
//                String actualPreUrl = "http://172.16.5.160:8090/api/v3/disk/previewSwf?onlinePreviewUrl=" + clickTV.getPreviewUrl();
//                intent.putExtra("previewUrl", actualPreUrl);
//                getContext().startActivity(intent);
                QZXTools.popToast(getContext(), "???????????????swf??????", false);

            } else {
                //?????????  ?????????????????????????????????,????????????????????????????????????
                ZBVPermission.getInstance().setPermPassResult(this);
                if (ZBVPermission.getInstance().hadPermissions(getActivity(), NeedPermission)) {
                    //????????????????????????
                    handlerTBSShow(clickTV);

                } else {
                    ZBVPermission.getInstance().requestPermissions(getActivity(), NeedPermission);
                }
            }

        } else {
            /*
             * ????????????SysFileShare??????fileId???????????????Disk??????fileId?????????
             * */
            if (clickTV.getFileId() != null) {
                curIds = clickTV.getFileId();
                shareId = clickTV.getShareId();
                shareTitle = clickTV.getShareTitle();
                source = clickTV.getSource();
                isHadParentId = false;
            } else {
                isHadParentId = true;
                if (clickTV.getId() != null) {
                    curIds = clickTV.getId() + "";
                } else {
                    return;
                }
            }
            //???????????????
            addBreadView(clickTV.getFileName(), curIds);
            QZXTools.logE("curIds=" + curIds
                    + ";fileName=" + clickTV.getFileName()
                    + ";fileFormat=" + clickTV.getFileFormat(), null);

            requestQueryDir(curIds, isHadParentId, false, true);
        }
    }



    private ArrayList<RecordStatus> recordStatuses = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handlerTBSShow(PreViewDisplayBean clickTV) {
        recordStatuses = null;
        //???????????????????????????
        String saveRecordPath = QZXTools.getExternalStorageForFiles(getContext(), null) + File.separator + "disk/preRecord.txt";
        File file = new File(saveRecordPath);
        if (file.exists()) {
            recordStatuses = (ArrayList<RecordStatus>)
                    SerializeUtil.deSerializeFromFile(file.getAbsolutePath());
            for (RecordStatus recordStatus : recordStatuses) {
                if (recordStatus.getPreviewUrl().equals(clickTV.getPreviewUrl())) {
                    //???????????????
                    //tbs??????
                   /* HashMap<String, String> params = new HashMap<String, String>();
                    params.put("local", "false");
                    params.put("allowAutoDestory", "true");
                    JSONObject Object = new JSONObject();
                    try {
                        Object.put("pkgName", getActivity().getApplicationContext().getPackageName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    params.put("menuData", Object.toString());
                    QbSdk.getMiniQBVersion(getActivity());
                    int ret = QbSdk.openFileReader(getActivity(), recordStatus.getSavedFilePath(),
                            params, PreCloudFragment.this);
                    Log.i("", "handlerTBSShow: "+ret);*/
                    //?????????????????????????????? // TODO: 2020/4/20
                /*    Intent intent=new Intent(getContext(), PdfWordActvity.class);
                    intent.putExtra("url",recordStatus.getSavedFilePath());
                    startActivity(intent);*/
                    //??????????????????wps ???
                    //???????????????wps ??????
                   if (checkWps()){
                     // Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage( "com.kingsoft.moffice_pro");
                    /*  Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage( "cn.wps.moffice_eng");
                       //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                           //??????FileProvider????????????content?????????Uri
                           Uri uri = FileProvider.getUriForFile(getActivity(),
                                   "com.telit.smartclass.desktop.fileprovider", new File(recordStatus.getSavedFilePath()));
                           intent.setData(uri);
                       }else {

                           intent.setData(Uri.parse(recordStatus.getSavedFilePath()));
                       }
                       intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);*/


                       wpsUtil.openDocument(new File(recordStatus.getSavedFilePath()));
                   }else {
                       QZXTools.popToast(getContext(), "????????????WPS Office", false);
                   }
                    return;
                }
            }


        }

        if (tbsDownloadDialog == null) {
            tbsDownloadDialog = new TBSDownloadDialog();
        }
        tbsDownloadDialog.show(getChildFragmentManager(), TBSDownloadDialog.class.getSimpleName());

        String downloadUrl = clickTV.getPreviewUrl();

        String suffix = downloadUrl.substring(downloadUrl.lastIndexOf(".") + 1);
        QZXTools.logE("suffix=" + suffix, null);

        //notes ?????????????????????swf??????????????????savePath????????????
        if (suffix.equals("swf")) {
            downloadUrl = clickTV.getSavePath();
        }
        //??????????????????
        OkHttp3_0Utils.getInstance().downloadSingleFileForOnce(downloadUrl,
                "disk", new OkHttp3_0Utils.DownloadCallback() {
                    @Override
                    public void downloadProcess(int value) {
                        if (tbsDownloadDialog != null) {
                            tbsDownloadDialog.download(value);
                        }
                    }

                    @Override
                    public void downloadComplete(String filePath) {
                        if (tbsDownloadDialog != null) {
                            tbsDownloadDialog.dismissAllowingStateLoss();
                            tbsDownloadDialog = null;
                            //?????????????????????
                           // requestWindowPermission();
                        }
                      //  QZXTools.popToast(getContext(), "????????????????????????" + filePath, false);

                        //??????????????????
                        if (!file.exists()) {
                            try {
                                boolean success = file.createNewFile();
                                if (success) {
                                    RecordStatus recordStatus = new RecordStatus();
                                    recordStatus.setSavedFilePath(filePath);
                                    recordStatus.setPreviewUrl(clickTV.getPreviewUrl());
                                    ArrayList<RecordStatus> recordStatuses = new ArrayList<>();
                                    recordStatuses.add(recordStatus);

                                    //?????????????????????
                                    SerializeUtil.toSerializeToFile(recordStatuses, file.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                QZXTools.logE("createNewFile Failed", null);
                            }
                        } else {
                            RecordStatus recordStatus = new RecordStatus();
                            recordStatus.setSavedFilePath(filePath);
                            recordStatus.setPreviewUrl(clickTV.getPreviewUrl());
                            recordStatuses.add(recordStatus);

                            //?????????????????????
                            SerializeUtil.toSerializeToFile(recordStatuses, file.getAbsolutePath());

                        }

                        //????????????????????????
                        EventBus.getDefault().post("update_cache", Constant.UPDATE_CACHE_VIEW);

                        //tbs??????
                   /*     HashMap<String, String> params = new HashMap<String, String>();
                        *//**
                         * ???true??????????????????????????????????????????????????????????????????false??????????????? miniqb ??????????????????
                         * *//*
                        params.put("local", "true");
                        params.put("allowAutoDestory", "true");
                        JSONObject Object = new JSONObject();
                        try {
                            Object.put("pkgName", getActivity().getApplicationContext().getPackageName());
                            Object.put("className", "com.telit.zhkt_three.Activity.PreView.PreViewActivity");
                            Object.put("menuItems", "[{\"id\": 0,\"iconResId\":" + R.mipmap.icon_user + ",\"text\": \"menu0\"}]");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        params.put("menuData", Object.toString());
                        QbSdk.getMiniQBVersion(getActivity());
                        int ret = QbSdk.openFileReader(getActivity(), filePath, params, PreCloudFragment.this);*/
                        if (checkWps()){
                            // Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage( "com.kingsoft.moffice_pro");
                            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage( "cn.wps.moffice_eng");
                            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                //??????FileProvider????????????content?????????Uri
                                Uri uri = FileProvider.getUriForFile(getActivity(),
                                        "com.telit.smartclass.desktop.fileprovider", new File(filePath));
                                intent.setData(uri);
                            }else {

                                intent.setData(Uri.parse(filePath));
                            }
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }else {
                            QZXTools.popToast(getContext(), "????????????WPS Office", false);
                        }
                        //????????????
                        requestWindowPermission();
                    }

                    @Override
                    public void downloadFailure() {
                        QZXTools.popToast(getContext(), "????????????", false);
                        if (tbsDownloadDialog != null) {
                            tbsDownloadDialog.dismissAllowingStateLoss();
                            tbsDownloadDialog = null;
                        }
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        QZXTools.logE("PreCloudFragment requestCode=" + requestCode + ";resultCode=" + resultCode, null);
        if (requestCode == OverlaysPermissionCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(getActivity())) {

                   // popCommentWindow();
                }
            }
        }
    }



    private static final int OverlaysPermissionCode = 0x107;

    private void requestWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getActivity())) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                //??????Fragment??????onActivityResult
                startActivityForResult(intent, OverlaysPermissionCode);
                return;
            }
        }

        //??????????????????????????????
        if (clickTV.getCommentContent() != null) {
            return;
        }

     //   popCommentWindow();
    }

    /**
     * ??????Window
     */
    private void popCommentWindow() {
        windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);

        pinglunView = LayoutInflater.from(getContext()).inflate(R.layout.simple_button_comment, null);
        pinglunView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String resId = clickTV.getResId();
                String resName = clickTV.getFileName();

                if (TextUtils.isEmpty(shareId) || TextUtils.isEmpty(shareTitle)
                        || TextUtils.isEmpty(resId) || TextUtils.isEmpty(resName)) {
                    QZXTools.popToast(getContext(), "??????????????????????????????", false);
                    windowManager.removeView(pinglunView);
                    return;
                }

                Intent intent_comment = new Intent(getContext(), CommentActivity.class);
                intent_comment.putExtra("shareId", shareId);
                intent_comment.putExtra("shareTitle", shareTitle);
                intent_comment.putExtra("resId", resId);
                intent_comment.putExtra("resName", resName);
                startActivity(intent_comment);

                windowManager.removeView(pinglunView);
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.y336);
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.x72);
        //????????????????????????????????????????????????
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        windowManager.addView(pinglunView, layoutParams);
    }

    /**
     * ???????????????
     *
     * @return boolean ???????????????false?????????????????????????????????????????????????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean handlerBackKey() {

        //???????????????app
      //  appBack();

        if (pre_cloud_bread == null) {
            return false;
        }

        if (pre_cloud_bread.getChildCount() > 1) {
            if (pre_cloud_bread.getChildCount() == 2) {
                pre_bread_tv_home.performClick();
            } else {
                //0 0 1 2 ?????????????????????
                View v = pre_cloud_bread.getChildAt(pre_cloud_bread.getChildCount() - 1 - 1);

                String splitString = (String) v.getTag();

                String[] split = splitString.split(":");
                int index = Integer.parseInt(split[1]);

                if (index == 1) {
                    isHadParentId = false;
                } else {
                    isHadParentId = true;
                }
                requestQueryDir(split[0], isHadParentId, false, true);

                pre_cloud_bread.removeViews(index + 1, 1);
            }

            return true;
        } else {
            return false;
        }
    }



    @SuppressLint("MissingPermission")
    public void appBack() {
        try {
            //??????ActivityManager
            ActivityManager mAm = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            //?????????????????????task
            List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(1);
            for (ActivityManager.RunningTaskInfo rti : taskList) {
                if (rti.topActivity.getPackageName().equals(getContext().getPackageName())) {
                    mAm.moveTaskToFront(rti.id, ActivityManager.MOVE_TASK_WITH_HOME);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveValue(String s) {
        QZXTools.logE("tbs open file callback=" + s, null);
        if (s.equals("fileReaderClosed")){

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void grantPermission() {
        handlerTBSShow(clickTV);
       // requestWindowPermission();
    }

    @Override
    public void denyPermission() {
        QZXTools.popToast(getContext(), "???????????????????????????????????????", false);
    }

    //??????wps ?????????????????????
    private boolean checkWps(){
       // Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.kingsoft.moffice_pro");//WPS??????????????????
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");//WPS??????????????????
        if (intent == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void doRequest(String filePath) {
        Log.d("MainActivity", "????????????????????????????????????");
    }
    private static boolean isWpsBack=false;
    @Override
    public void doFinish() {
       // wpsUtil.appBack();
       // isWpsBack=true;
        if (TextUtils.isEmpty(clickTV.getCommentContent())) {
          //  popCommentWindow();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isWpsBack=false;
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void spinnerClick(View parent, String text) {
        switch (parent.getId()) {
            case R.id.pull_subject:
                curPageNo = 1;
                pull_subject.setPullContent(text);
                break;
            case R.id.pull_date:
                curPageNo = 1;
                pull_date.setPullContent(text);
                if (text.equals("?????????")) {
                    mistakes_custom_date_layout.setVisibility(View.VISIBLE);
                    popupCustomDate();
                    return;
                } else {
                    calculateDateSection(text);
                    mistakes_custom_date_layout.setVisibility(View.INVISIBLE);
                }
                break;
        }

        requestDatas(true, true);
    }

    private PopupWindow datePopup;

    /**
     * ?????????????????????????????????
     */
    private void popupCustomDate() {
        if (datePopup != null) {
            datePopup.dismiss();
        }

        View menuView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_mistakes_date_layout, null);
        datePopup = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        MaterialCalendarView materialCalendarView = menuView.findViewById(R.id.mistakes_pop_calendarView);

        RangeDayDecorator decorator = new RangeDayDecorator(getActivity());
        materialCalendarView.addDecorator(decorator);
        materialCalendarView.setSelectedDate(LocalDate.parse(startDate.split(" ")[0]));
        materialCalendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                if (dates.size() > 0) {
                    String start = FORMATTER.format(dates.get(0).getDate());
                    String end = FORMATTER.format(dates.get(dates.size() - 1).getDate());

                    decorator.addFirstAndLast(dates.get(0), dates.get(dates.size() - 1));
                    materialCalendarView.invalidateDecorators();

                    startDate = start.concat(" 00:00:00");
                    endDate = end.concat(" 00:00:00");

                    mistakes_start_tv.setText(startDate);
                    mistakes_end_tv.setText(endDate);

                    mHandler.sendEmptyMessageDelayed(Operate_Delay_Date_Query, 1000);

                    QZXTools.popCommonToast(getActivity(),
                            "firstDate=" + start + ";secondDate=" + end, false);
                }
            }
        });

        datePopup.setBackgroundDrawable(new ColorDrawable());
        datePopup.setOutsideTouchable(true);

        //popup???????????????????????????????????????????????????????????????
        datePopup.showAsDropDown(mistakes_custom_date_layout, 0, 0);
    }

    /**
     * ??????????????????
     *
     * @param dateStr ????????????????????????
     */
    private void calculateDateSection(String dateStr) {
        //????????????????????????????????? ms/??????
        long timeInterval;
        if (dateStr.equals("??????")) {
            timeInterval = 24 * 60 * 60 * 1000;
        } else if (dateStr.equals("??????")) {
            timeInterval = 7 * 24 * 60 * 60 * 1000;
        } else if (dateStr.equals("??????")) {
            // 2592000000 ????????????int??????????????????L?????????Long????????????????????????
            timeInterval = 30 * 24 * 60 * 60 * 1000L;
        } else {
            return;
        }

        QZXTools.logE("timeInterval=" + timeInterval, null);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        QZXTools.logE("year=" + year + ";month=" + month + ";day=" + day, null);

        int maxDay = QZXTools.calculate(year, month);
        //??????????????????
        if ((day + 1) > maxDay) {
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
            day = 1;
        } else {
            day = day + 1;
        }

        String strDay;
        String strMonth;
        if (day <= 9) {
            strDay = "0" + day;
        } else {
            strDay = day + "";
        }

        if (month <= 9) {
            strMonth = "0" + month;
        } else {
            strMonth = month + "";
        }

        //????????????
        String tomorrowStr = year + "-" + strMonth + "-" + strDay + " " + "00:00:00";
        endDate = tomorrowStr;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(tomorrowStr);
            long endTime = date.getTime();

            long startTime = endTime - timeInterval;
            QZXTools.logE("startTime=" + startTime + ";endTime=" + endTime + ";timeInterval=" + timeInterval, null);

            //?????????????????????Date???????????????
            startDate = simpleDateFormat.format(new Date(startTime));

            QZXTools.logE("tomorrowStr=" + tomorrowStr + ";startDate=" + startDate + ";endDate=" + endDate, null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initData() {
        subjectMap = new LinkedHashMap<>();

        dateTime = new ArrayList<>();
        dateTime.add("??????");
        dateTime.add("??????");
        dateTime.add("??????");
        dateTime.add("?????????");
        pull_date.setDataList(dateTime);
        pull_date.setPullContent(dateTime.get(0));

        //??????????????????
        calculateDateSection(pull_date.getPullContent());

        mistakes_start_tv.setText(startDate);
        mistakes_end_tv.setText(endDate);

        fetchNetSubjectData();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fetchNetSubjectData() {

        String url = UrlUtils.BaseUrl + UrlUtils.MistakesSubjectList;

        Map<String, String> paraMap = new LinkedHashMap<>();
        paraMap.put("studentid", UserUtils.getStudentId());

        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, paraMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resultJson = response.body().string();
                        QZXTools.logE("resultJson=" + resultJson, null);
                        Gson gson = new Gson();
                        SubjectiveListBean subjectiveListBean = gson.fromJson(resultJson, SubjectiveListBean.class);
                        for (SubjectBean subjectBean : subjectiveListBean.getResult()) {
                            subjectMap.put(subjectBean.getName(), subjectBean.getId());
                        }
                        mHandler.sendEmptyMessage(Operate_Subject_Query_Success);

                        QZXTools.logE("resultJson=" + subjectiveListBean.getResult().size()+"", null);

                    }catch (Exception e){
                        e.fillInStackTrace();
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        mHandler.sendEmptyMessage(Server_Error);

                    }
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    //???????????????????????????
    @Subscriber(tag = Constant.Cloud_Share_Collect_Success, mode = ThreadMode.MAIN)
    public void collectionResource(String tag) {
        QZXTools.logE("???????????????????????????",null);
    }

    //??????
    @Override
    public void onItemCollectionClickListener(PreCloudRVAdapter.PreCloudViewHolder holder, int position) {
        QZXTools.logE("?????????"+position,null);

        if (ViewUtils.isFastClick(1000)){
            PreViewDisplayBean preViewDisplayBean = preViewDiaplayBeans.get(position);
            if ("1".equals(preViewDisplayBean.getCollectionState())){
                collectYeOrNo(preViewDisplayBean,UserUtils.getUserId(),preViewDiaplayBeans.get(position).getShareId()
                        ,preViewDiaplayBeans.get(position).getFileId(),preViewDiaplayBeans.get(position).getCollectionId(),"0",holder);
            }else {
                collectYeOrNo(preViewDisplayBean,UserUtils.getUserId(),preViewDiaplayBeans.get(position).getShareId()
                        ,preViewDiaplayBeans.get(position).getFileId(),preViewDiaplayBeans.get(position).getCollectionId(),"1",holder);
            }
        }

    }

    /**
     * ?????????????????????
     *
     * @param preViewDisplayBean
     * @param studentId
     * @param shareId
     */
    private void collectYeOrNo(PreViewDisplayBean preViewDisplayBean, String studentId, String shareId,String fileId, String collectionId,String collectionState,PreCloudRVAdapter.PreCloudViewHolder holder){
        String url = UrlUtils.BaseUrl + UrlUtils.CollectShareYesOrNo;

        Map<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("studentId", studentId);
        mapParams.put("shareId", shareId);
        mapParams.put("fileId", fileId);
        mapParams.put("collectionId", collectionId);
        mapParams.put("collectionState", collectionState);

        QZXTools.logE("param:"+new Gson().toJson(mapParams),null);

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.popToast(getActivity(), "??????????????????", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("commit questions resultJson=" + resultJson, null);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QZXTools.logE("query dir resultJson=" + resultJson, null);

                            JSONObject jsonObject=JSONObject.parseObject(resultJson);
                            String errorCode = jsonObject.getString("errorCode");

                            if ("1".equals(errorCode)){
                                preViewDiaplayBeans.remove(preViewDisplayBean);
                                collectResourcesRVAdapter.notifyDataSetChanged();

                                EventBus.getDefault().post("Resource_Share_Collect_Success", Constant.Resource_Share_Collect_Success);
                            }
                        }
                    });
                } else {
                    QZXTools.popToast(getActivity(), "?????????????????????", false);
                }
            }
        });
    }
}
