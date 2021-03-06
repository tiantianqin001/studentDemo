package com.telit.zhkt_three.Fragment.Interactive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Adapter.interactive.DiscussCommunicationRVAdapter;
import com.telit.zhkt_three.Adapter.interactive.DiscussMemberRVAdapter;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.Fragment.Dialog.DiscussConclusionFragment;
import com.telit.zhkt_three.Fragment.Dialog.TBSDownloadDialog;
import com.telit.zhkt_three.JavaBean.Gson.GroupListBean;
import com.telit.zhkt_three.JavaBean.InterActive.DiscussBean;
import com.telit.zhkt_three.JavaBean.InterActive.DiscussListBeanTwo;
import com.telit.zhkt_three.JavaBean.PreView.RecordStatus;
import com.telit.zhkt_three.MediaTools.CropActivity;
import com.telit.zhkt_three.MediaTools.audio.AudioPlayActivity;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MediaTools.video.VideoPlayerActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.CyptoUtils;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.SerializeUtil;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.telit.zhkt_three.customNetty.MsgUtils;
import com.telit.zhkt_three.customNetty.SimpleClientNetty;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * author: qzx
 * Date: 2019/6/24 16:42
 * <p>
 * todo ??????????????????????????????????????????
 * todo ?????????????????????????????????
 * todo ?????????????????????????????????1000x600
 * <p>
 * ????????????????????????????????????????????????????????????????????????????????????
 * <p>
 * ??????????????????????????????????????????
 * <p>
 * ?????????????????????
 */
public class GroupDiscussFragment extends Fragment implements View.OnClickListener
        , ZBVPermission.PermPassResult, ValueCallback<String> {

    private Unbinder unbinder;

    @BindView(R.id.discuss_topic_name)
    TextView discuss_topic_name;
    @BindView(R.id.discuss_file_frame)
    FrameLayout discuss_file_frame;
    @BindView(R.id.discuss_file_dot)
    ImageView discuss_file_dot;
    @BindView(R.id.discuss_member_info)
    TextView discuss_member_info;
    @BindView(R.id.discuss_rv_member)
    RecyclerView discuss_rv_member;
    @BindView(R.id.discuss_rv_communication)
    RecyclerView discuss_rv_communication;
    @BindView(R.id.discuss_edit)
    EditText discuss_edit;
    @BindView(R.id.discuss_send_pics)
    ImageView discuss_send_pics;
    @BindView(R.id.discuss_send_btn)
    Button discuss_send_btn;

    /**
     * ????????????ID
     */
    private String discussId;

    public void setDiscussId(String discussId) {
        QZXTools.logE("discussId=" + discussId, null);
        this.discussId = discussId;
    }

    //??????ID
    private int discussGroupId;
    private String groupIndex;


    //????????????
    private DiscussMemberRVAdapter discussMemberRVAdapter;
    private List<DiscussListBeanTwo> discussMemberList;

    //????????????
    private DiscussCommunicationRVAdapter discussCommunicationRVAdapter;
    private List<DiscussBean> discussBeanList;

    private CircleProgressDialogFragment circleProgressDialogFragment;

    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operator_Member_success = 2;
    private static final int Alter_Photo_Result = 3;

    //?????????????????????????????????????????????????????????????????????????????????
    private static boolean  isShow=false;

    private Handler mHandler = new Handler() {
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

                    }

                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popToast(getContext(), "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                    }

                    break;
                case Operator_Member_success:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        //??????????????????ID?????????MAP????????????
                        HashMap<String, String> avatarBindMap = (HashMap<String, String>) msg.obj;
                        discussCommunicationRVAdapter.setAvatarMap(avatarBindMap);

                        //??????????????????
                        if (TextUtils.isEmpty(discussMemberList.get(0).getFileUrl())) {
                            if (discuss_file_frame!=null)
                            discuss_file_frame.setVisibility(View.GONE);
                        } else {
                            if (discuss_file_frame!=null)
                            discuss_file_frame.setVisibility(View.VISIBLE);
                            if (fileList == null) {
                                fileList = new ArrayList<>();
                            }
                            fileList.add(discussMemberList.get(0).getFileUrl());
                        }

                        discussGroupId = discussMemberList.get(0).getDiscussGroupId();
                        groupIndex = discussMemberList.get(0).getGroupIndex();

                        //??????????????????
                        String groupName = discussMemberList.get(0).getGroupName();
                        int memberCount = discussMemberList.size();

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(groupName);
                        stringBuilder.append("???????????????(");
                        stringBuilder.append(memberCount);
                        stringBuilder.append("???)");
                        discuss_member_info.setText(stringBuilder.toString());

                        //??????
                        String topic = discussMemberList.get(0).getTheme();
                        if (TextUtils.isEmpty(topic)) {
                            topic = "????????????";
                        }
                        discuss_topic_name.setText(topic);

                        //??????????????????
                        for (int i = 0; i < discussMemberList.size(); i++) {
                            discussMemberRVAdapter.notifyItemChanged(i);
                        }
                    }

                    break;
                case Alter_Photo_Result:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        DiscussBean discussBean = (DiscussBean) msg.obj;
                        SimpleClientNetty.getInstance().sendMsgToServer(MsgUtils.HEAD_DISCUSS, MsgUtils.createDiscuss(discussBean));
                    }

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_discuss_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        isShow=true;
        //???????????????????????????
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        discuss_rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
        discuss_rv_member.setOverScrollMode(View.OVER_SCROLL_NEVER);
        //recyclerView???Item???????????????
        discussMemberList = new ArrayList<>();
        OvershootFromRightAnim overshootFromRightAnim = new OvershootFromRightAnim();
        overshootFromRightAnim.setAddDuration(1000);
        discuss_rv_member.setItemAnimator(overshootFromRightAnim);
        discussMemberRVAdapter = new DiscussMemberRVAdapter(getContext(), discussMemberList);
        discuss_rv_member.setAdapter(discussMemberRVAdapter);

        discuss_rv_communication.setLayoutManager(new LinearLayoutManager(getContext()));
        discuss_rv_communication.setOverScrollMode(View.OVER_SCROLL_NEVER);
        discussBeanList = new ArrayList<>();
        discussCommunicationRVAdapter = new DiscussCommunicationRVAdapter(getContext(), discussBeanList);
        discuss_rv_communication.setAdapter(discussCommunicationRVAdapter);

        discuss_send_btn.setOnClickListener(this);
        discuss_send_pics.setOnClickListener(this);
        discuss_file_frame.setOnClickListener(this);

        //????????????
        discuss_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    //?????????????????????
                    discuss_send_pics.setVisibility(View.GONE);
                    discuss_send_btn.setVisibility(View.VISIBLE);
                } else {
                    discuss_send_pics.setVisibility(View.VISIBLE);
                    discuss_send_btn.setVisibility(View.GONE);
                }
            }
        });

        fetchGroupInfo();

        return view;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }

        //???????????????????????????????????????????????????
        if (circleProgressDialogFragment != null) {
            circleProgressDialogFragment = null;
        }

        ZBVPermission.getInstance().recyclerAll();
        EventBus.getDefault().unregister(this);
        //????????????
        mHandler.removeCallbacksAndMessages(null);
        QZXTools.setmToastNull();
        //????????????????????????
        isShow=false;
        super.onDestroyView();
    }

    /**
     * ???????????????????????????????????????
     */
    public void showConclusionView() {
        if (discussMemberList == null || discussMemberList.size() <= 0) {
            return;
        }
        //??????????????????

        String recorderUserId = discussMemberList.get(0).getGroupLeader();
        String ownUserId = UserUtils.getUserId();

        if (recorderUserId.equals(ownUserId)) {
            DiscussConclusionFragment discussConclusionFragment = new DiscussConclusionFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("discussGroupId", discussGroupId);
            bundle.putString("discussId", discussId);
            bundle.putString("groupIndex", groupIndex);
            discussConclusionFragment.setArguments(bundle);
            discussConclusionFragment.show(getChildFragmentManager(), DiscussConclusionFragment.class.getSimpleName());
        } else {
            //????????????????????????
            EventBus.getDefault().post("", Constant.Show_Conclusion);
        }
    }

    @Subscriber(tag = Constant.Discuss_Send_Pic, mode = ThreadMode.MAIN)
    public void sendPic(String filePath) {
        sendDiscussMsg(filePath, MsgUtils.TYPE_PICTURE);
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * ????????????receiveMsgInfo?????????????????????????????????????????????
     */
    @Subscriber(tag = Constant.Discuss_Message, mode = ThreadMode.MAIN)
    public void receiveMsgInfo(String discussMsg) {
        QZXTools.logE("receive=" + discussMsg, null);
        Gson gson = new Gson();
        DiscussBean discussBean = gson.fromJson(discussMsg, DiscussBean.class);
//        //?????????????????????
//        if (discussBean.getType() == MsgUtils.TYPE_TEXT) {
//            //??????
//            String content = discussBean.getContent();
//            discussBean.setContent(CyptoUtils.decode(Constant.DESKey, content));
//        }
        discussBeanList.add(discussBean);
        discussCommunicationRVAdapter.notifyDataSetChanged();
        discuss_rv_communication.smoothScrollToPosition(discussBeanList.size() - 1);

        //??????????????????
        for (DiscussListBeanTwo discussListBean : discussMemberList) {
            if (discussListBean.getUserId().equals(discussBean.getStudentId())) {
                discussListBean.setSpeakTime(QZXTools.DateOrTimeStrShow(discussBean.getTime()));
            }
        }
        discussMemberRVAdapter.notifyDataSetChanged();

        //?????????????????????
        discussBean.setDiscussId(discussId);
        QZXTools.logE("start save receive msg="+discussMsg,null);
        MyApplication.getInstance().getDaoSession().getDiscussBeanDao().insertOrReplace(discussBean);
        QZXTools.logE("end save receive msg="+discussMsg,null);
    }

    private static final String[] needPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean isDiscussFile = false;

    private boolean isCamera = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discuss_send_btn:
                if (discuss_edit.getText().toString().trim().equals("")) {
                    QZXTools.popCommonToast(getContext(), "?????????????????????", false);
                    return;
                }

                sendDiscussMsg(discuss_edit.getText().toString().trim(), MsgUtils.TYPE_TEXT);

                //??????
                discuss_edit.setText("");
                //????????????????????? ?????????????????????
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(discuss_send_btn.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                break;
            case R.id.discuss_send_pics:
                if (picPopup != null && picPopup.isShowing()) {
                    picPopup.dismiss();
                    picPopup = null;
                } else {
                    popupPic(v);
                }
                break;
            case R.id.discuss_file_frame:
                //????????????
                popupSelectMenu(v);
                break;
            case R.id.group_pic_camera:
                //????????????
                ZBVPermission.getInstance().setPermPassResult(this);

                if (!ZBVPermission.getInstance().hadPermissions(getActivity(), needPermissions)) {
                    isCamera = true;
                    isDiscussFile = false;
                    ZBVPermission.getInstance().requestPermissions(getActivity(), needPermissions);
                } else {
                    //??????????????????
                    QZXTools.logD("?????????????????????????????????");
                    openCamera();
                }
                break;
            case R.id.group_pic_album:
                //????????????
                ZBVPermission.getInstance().setPermPassResult(this);

                if (!ZBVPermission.getInstance().hadPermissions(getActivity(), needPermissions)) {
                    isDiscussFile = false;
                    isCamera = false;
                    ZBVPermission.getInstance().requestPermissions(getActivity(), needPermissions);
                } else {
                    //??????????????????
                    QZXTools.logD("?????????????????????????????????");
                    openSysAlbum();
                }
                break;
        }
    }

    private PopupWindow picPopup;

    /**
     * ????????????????????????
     */
    private void popupPic(View v) {
        if (picPopup != null) {
            picPopup.dismiss();
            picPopup = null;
        }

        View picView = LayoutInflater.from(getContext()).inflate(R.layout.group_pop_pic, null);

        picPopup = new PopupWindow(picView, (int) getResources().getDimension(R.dimen.x150),
                (int) getResources().getDimension(R.dimen.x120));

        picPopup.setBackgroundDrawable(new ColorDrawable());
        picPopup.setOutsideTouchable(true);

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "PingFang-SimpleBold.ttf");

        TextView pic_camera = picView.findViewById(R.id.group_pic_camera);
        TextView pic_album = picView.findViewById(R.id.group_pic_album);

        pic_camera.setTypeface(typeface);
        pic_album.setTypeface(typeface);

        pic_camera.setOnClickListener(this);
        pic_album.setOnClickListener(this);

        // picPop.isShowing?????????,??????pop???????????? dialog.setFocusable(true);
        picPopup.setFocusable(true);

        picView.measure(0, 0);

        picPopup.showAsDropDown(v, -(picView.getMeasuredWidth() - v.getWidth()) / 2,
                -(int) getResources().getDimension(R.dimen.x5));
    }


    private PopupWindow menuPopup;

    /**
     * ?????????????????????????????????????????????
     */
    private void popupSelectMenu(View v) {
        if (menuPopup != null) {
            menuPopup.dismiss();
            menuPopup = null;
        }

        View menuView = LayoutInflater.from(getContext()).inflate(R.layout.pull_rv_menu_layout, null);

        menuPopup = new PopupWindow(menuView, (int) getResources().getDimension(R.dimen.x400), ViewGroup.LayoutParams.WRAP_CONTENT);

        menuPopup.setBackgroundDrawable(new ColorDrawable());
        menuPopup.setOutsideTouchable(true);

        ConstraintLayout constraintLayout = menuView.findViewById(R.id.pull_menu_bg);
        //????????????????????????
        constraintLayout.setBackgroundColor(getResources().getColor(R.color.word_gray));

        RecyclerView recyclerView = menuView.findViewById(R.id.pull_menu_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RVPullMenuAdapter adapter = new RVPullMenuAdapter();
        recyclerView.setAdapter(adapter);

        //popup???????????????????????????????????????????????????????????????
        menuPopup.showAsDropDown(v, 0, 0);
    }

    private List<String> fileList;

    private String fileUrl;

    //?????????????????????????????????
    private int dotCount;

    /**
     * author: qzx
     * Date: 2019/5/15 15:16
     */
    public class RVPullMenuAdapter extends RecyclerView.Adapter<RVPullMenuAdapter.RVPullMenuViewHolder> {

        @NonNull
        @Override
        public RVPullMenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new RVPullMenuViewHolder(LayoutInflater.from(getContext())
                    .inflate(R.layout.adapter_item_discuss_file, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RVPullMenuViewHolder rvPullMenuViewHolder, int i) {
            rvPullMenuViewHolder.textView.setText("????????????" + (i + 1));
            String url = fileList.get(i);
            String format = url.substring(url.lastIndexOf(".") + 1);
            if (format.equals("mp4") || format.equals("avi")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.video);
            } else if (format.equals("mp3")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.voice);
            } else if (format.equals("jpg") || format.equals("png") || format.equals("gif")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.picture);
            } else if (format.equals("ppt") || format.equals("pptx")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.ppt);
            } else if (format.equals("doc") || format.equals("docx") || format.equals("txt")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.word);
            } else if (format.equals("xls") || format.equals("xlsx")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.excel);
            } else if (format.equals("pdf")) {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.pdf);
            } else {
                rvPullMenuViewHolder.imageView.setImageResource(R.mipmap.file);
            }
        }

        @Override
        public int getItemCount() {
            return fileList != null ? fileList.size() : 0;
        }

        public class RVPullMenuViewHolder extends RecyclerView.ViewHolder {

            private TextView textView;
            private ImageView imageView;

            public RVPullMenuViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.dicuss_file_name);
                imageView = itemView.findViewById(R.id.discuss_file_sign);

                itemView.setTag("false");

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (menuPopup != null && menuPopup.isShowing()) {
                            menuPopup.dismiss();
                            menuPopup = null;
                        }

                        if (v.getTag().equals("false")) {
                            v.setTag("true");
                            dotCount++;

                            if (dotCount == fileList.size()) {
                                changeDot();
                            }
                        }

                        String url = fileList.get(getLayoutPosition());
                        String format = url.substring(url.lastIndexOf(".") + 1);

                        String actualUrl = url;

                        if (format.equals("mp4") || format.equals("avi")) {

                            Intent intent_video = new Intent(getContext(), VideoPlayerActivity.class);
                            intent_video.putExtra("VideoFilePath", actualUrl);
                            getContext().startActivity(intent_video);

                        } else if (format.equals("mp3")) {

                            Intent intent = new Intent(getContext(), AudioPlayActivity.class);
                            intent.putExtra("AudioFilePath", actualUrl);
                            getContext().startActivity(intent);

                        } else if (format.equals("jpg") || format.equals("png") || format.equals("gif") || format.equals("jpeg")) {

                            Intent intent_img = new Intent(getContext(), ImageLookActivity.class);
                            ArrayList<String> imgFilePathList = new ArrayList<>();
                            imgFilePathList.add(actualUrl);
                            intent_img.putStringArrayListExtra("imgResources", imgFilePathList);
                            intent_img.putExtra("curImgIndex", 0);
                            getContext().startActivity(intent_img);

                        } else {
                            ZBVPermission.getInstance().setPermPassResult(GroupDiscussFragment.this);
                            if (ZBVPermission.getInstance().hadPermissions(getActivity(), needPermissions)) {
                                fileUrl = actualUrl;
                                handlerTBSShow();
                            } else {
                                isDiscussFile = true;
                                ZBVPermission.getInstance().requestPermissions(getActivity(), needPermissions);
                            }
                        }
                    }
                });

            }
        }
    }

    private TBSDownloadDialog tbsDownloadDialog;
    private ArrayList<RecordStatus> recordStatuses = null;

    private void handlerTBSShow() {
        if (TextUtils.isEmpty(fileUrl)) {
            return;
        }

        String preViewUrl = fileUrl;

        recordStatuses = null;
        //???????????????????????????
        String saveRecordPath = QZXTools.getExternalStorageForFiles(getContext(), null) + File.separator + "discuss/preRecord.txt";
        File file = new File(saveRecordPath);
        if (file.exists()) {
            recordStatuses = (ArrayList<RecordStatus>)
                    SerializeUtil.deSerializeFromFile(file.getAbsolutePath());
            for (RecordStatus recordStatus : recordStatuses) {
                if (recordStatus.getPreviewUrl().equals(preViewUrl)) {
                    //???????????????
                    //tbs??????
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("local", "true");
                    params.put("allowAutoDestory", "true");
                    JSONObject Object = new JSONObject();
                    try {
                        Object.put("pkgName", getActivity().getApplicationContext().getPackageName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    params.put("menuData", Object.toString());

                    //todo  ????????????????????? ?????????x5  ????????????????????????wps  ??????
                   /* QbSdk.getMiniQBVersion(getActivity());
                    int ret = QbSdk.openFileReader(getActivity(), recordStatus.getSavedFilePath(),
                            params, GroupDiscussFragment.this);*/
                    return;
                }
            }
        }

        if (tbsDownloadDialog == null) {
            tbsDownloadDialog = new TBSDownloadDialog();
        }
        tbsDownloadDialog.show(getChildFragmentManager(), TBSDownloadDialog.class.getSimpleName());

        OkHttp3_0Utils.getInstance().downloadSingleFileForOnce(preViewUrl,
                "discuss", new OkHttp3_0Utils.DownloadCallback() {
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
                        }
                        QZXTools.popToast(getContext(), "????????????????????????" + filePath, false);

                        //??????????????????
                        if (!file.exists()) {
                            try {
                                boolean success = file.createNewFile();
                                if (success) {
                                    RecordStatus recordStatus = new RecordStatus();
                                    recordStatus.setSavedFilePath(filePath);
                                    recordStatus.setPreviewUrl(preViewUrl);
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
                            recordStatus.setPreviewUrl(preViewUrl);
                            recordStatuses.add(recordStatus);

                            //?????????????????????
                            SerializeUtil.toSerializeToFile(recordStatuses, file.getAbsolutePath());

                        }

                        //????????????????????????
                        EventBus.getDefault().post("update_cache", Constant.UPDATE_CACHE_VIEW);

                        //tbs??????
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("local", "true");
                        params.put("allowAutoDestory", "true");
                        JSONObject Object = new JSONObject();
                        try {
                            Object.put("pkgName", getActivity().getApplicationContext().getPackageName());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        params.put("menuData", Object.toString());
                        //todo  ????????????????????? ?????????x5  ????????????????????????wps  ??????
                     /*   QbSdk.getMiniQBVersion(getActivity());
                        int ret = QbSdk.openFileReader(getActivity(), filePath, params, GroupDiscussFragment.this);*/

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

    public void changeDot() {
        if (discuss_file_dot != null)
            discuss_file_dot.setVisibility(View.GONE);
    }

    @Override
    public void onReceiveValue(String s) {
        QZXTools.logE("discuss receiveValue=" + s, null);
    }

    /**
     * ?????????????????????
     *
     * @param data ??????????????????????????????????????? ????????????????????????????????????????????????????????????web?????????
     * @param type ????????????0 ????????????1
     */
    StringBuffer stringBuffer=new StringBuffer();
    public void sendDiscussMsg(String data, int type) {

//        //???????????????
//        discussBeanList.add(discussBean);
//        discussCommunicationRVAdapter.notifyDataSetChanged();
            //????????????????????? ???????????????????????????????????????
        String[] strings = data.split("");
        for (String string : strings) {
            if (TextUtils.isEmpty(string) || string.equals(" ")){
                continue;
            }
            stringBuffer.append(string);
        }


        if (type == MsgUtils.TYPE_TEXT) {
            String originalData = stringBuffer.toString();
            //DES??????
            String desData = CyptoUtils.encode(Constant.DESKey, originalData);
            QZXTools.logE("originalData=" + originalData + ";desData=" + originalData, null);
            DiscussBean discussBean = MsgUtils.getDiscussBean(originalData, "", type, discussGroupId, groupIndex);
            discussBean.setDiscussId(discussId);
            SimpleClientNetty.getInstance().sendMsgToServer(MsgUtils.HEAD_DISCUSS, MsgUtils.createDiscuss(discussBean));
            stringBuffer.setLength(0);
        } else if (type == MsgUtils.TYPE_PICTURE) {
            uploadDiscussPic(data);
        }
    }

    /**
     * {
     * "success": true,
     * "errorCode": "1",
     * "msg": "????????????",
     * "result": ["http://172.16.4.40:8090/filesystem/liaotian/opted_9e9d45090095f04bac0aae30a09e3f10bef1.jpg",
     * "http://172.16.4.40:8090/filesystem/liaotian/9e9d45090095f04bac0aae30a09e3f10bef1.jpg"],
     * "total": 0,
     * "pageNo": 0
     * }
     * <p>
     * ?????????????????????
     */
    private void uploadDiscussPic(String filePath) {
        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());

        //????????????????????????
        String url = UrlUtils.BaseUrl + UrlUtils.DiscussImgUpload;
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("userId", UserUtils.getUserId());
        OkHttp3_0Utils.getInstance().asyncPostSingleOkHttp(url, "attachement",
                paraMap, new File(filePath), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        QZXTools.logE("onFailure e=" + e, null);
                        //???????????????,???????????????????????????????????????
                        mHandler.sendEmptyMessage(Server_Error);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String resultJson = response.body().string();
                            QZXTools.logE("resultJson=" + resultJson, null);

                            Gson gson = new Gson();
                            Map<String, Object> results = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                            }.getType());

                            List<String> imgsString = (List<String>) results.get("result");

                            if (imgsString == null || imgsString.size() == 0) {
                                //???????????? ??????
                                mHandler.sendEmptyMessage(Server_Error);
                            } else {
                                DiscussBean discussBean = MsgUtils.getDiscussBean(imgsString.get(1), imgsString.get(0),
                                        MsgUtils.TYPE_PICTURE, discussGroupId, groupIndex);
                                discussBean.setDiscussId(discussId);
                                Message message = mHandler.obtainMessage();
                                message.what = Alter_Photo_Result;
                                message.obj = discussBean;
                                mHandler.sendMessage(message);
                            }
                        } else {
                            mHandler.sendEmptyMessage(Error404);
                        }
                    }
                });
    }

    private int curRetryCount = 0;

    /**
     * {
     * "success": true,
     * "errorCode": "1",
     * "msg": "????????????",
     * "result": [{
     * "studentid": "0442b5d559f74afeada49f51bdfc229a",
     * "studentName": "?????????1",
     * "photo": "http://172.16.4.40:8090/filesystem/headImg/66666702496IMG_20190530_095504.jpg",
     * "groupName": "1",
     * "isLeader": 1
     * }, {
     * "studentid": "70b45737e82b4e5d9b8fb0bc3605b7bd",
     * "studentName": "?????????",
     * "photo": null,
     * "groupName": "1",
     * "isLeader": 0
     * }],
     * "total": 0,
     * "pageNo": 0
     * }
     * <p>
     * ???????????????????????????
     */
    private void fetchGroupInfo() {
        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getChildFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());

        curRetryCount++;

//        String url = UrlUtils.BaseUrl + UrlUtils.DiscussGroup;
        String url = UrlUtils.BaseUrl + UrlUtils.DiscussGroupTwo;

        Map<String, String> paraMap = new LinkedHashMap<>();
//        paraMap.put("studentid", UserUtils.getStudentId());
//        paraMap.put("id", discussId);

        paraMap.put("userId", UserUtils.getUserId());
        paraMap.put("discussId", discussId);
        paraMap.put("classId", UserUtils.getClassId());

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
                    QZXTools.logE("resultJson=" + resultJson, null);
                    Gson gson = new Gson();
                    GroupListBean groupListBean = gson.fromJson(resultJson, GroupListBean.class);
                    if (groupListBean.getResult() != null && groupListBean.getResult().size() > 0) {
                        for (DiscussListBeanTwo discussListBeanTwo : groupListBean.getResult()) {

                            //??????????????????????????????????????????????????????????????????

                            discussMemberList.add(discussListBeanTwo);
                        }

                        Collections.sort(discussMemberList);

                        //???????????????ID??????
                        Map<String, String> avatarBindMap = new HashMap<>();
                        DiscussListBeanTwo newBeanTwo = null;
                        DiscussListBeanTwo oldBeanTwo = null;
                        for (DiscussListBeanTwo discussListBeanTwo : discussMemberList) {
                            avatarBindMap.put(discussListBeanTwo.getUserId(), discussListBeanTwo.getPhoto());
                            if (discussListBeanTwo.getGroupLeader().equals(discussListBeanTwo.getUserId())) {
                                //?????????
                                newBeanTwo = discussListBeanTwo;
                                oldBeanTwo = discussListBeanTwo;
                            }
                        }

                        if (oldBeanTwo != null && newBeanTwo != null) {
                            discussMemberList.remove(oldBeanTwo);
                            discussMemberList.add(0, newBeanTwo);
                        }


                        QZXTools.logE("map=" + avatarBindMap, null);
                        Message message = mHandler.obtainMessage();
                        message.what = Operator_Member_success;
                        message.obj = avatarBindMap;
                        mHandler.sendMessage(message);
                    } else {
                        mHandler.sendEmptyMessage(Error404);
                    }
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    public static final int CODE_SYS_ALBUM = 2;//????????????RequestCode

    public static final int CODE_SYS_CROP = 3;//????????????RequestCode

    public static final int CODE_SYS_CAMERA = 5;//????????????RequestCode


    public static final int CODE_CUSTOM_CROP = 7;//???????????????RequestCode

    /**
     * ?????????????????????
     *
     * @param originalUri ???Uri
     * @param savedUri    ?????????Uri
     */
    public static void cropPhotoTwo(Activity activity, Uri originalUri, Uri savedUri) {
        Intent intent = new Intent(activity, CropActivity.class);
        intent.setDataAndType(originalUri, "image/*");
        intent.putExtra("save_path", savedUri);
        activity.startActivityForResult(intent, CODE_SYS_CROP);
    }

    /**
     * ??????
     *
     * @param sourceUri ??????????????????Uri????????????????????????
     * @param outputUri ?????????????????????Uri
     */
    public static void cropPhoto(Activity activity, Uri sourceUri, Uri outputUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //??????7.0????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //??????????????????Uri????????????
        intent.setDataAndType(sourceUri, "image/*");
        //????????????????????????true?????????????????????
        intent.putExtra("crop", "true");
        //X????????????
        intent.putExtra("aspectX", 1);
        //Y????????????
        intent.putExtra("aspectY", 1);
        //???????????????
        intent.putExtra("outputX", 1000);
        //???????????????
        intent.putExtra("outputY", 600);
        //??????????????????
        intent.putExtra("scale", true);
        //??????????????????????????????Bitmap?????????
        intent.putExtra("return-data", false);
        //????????????????????????
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        //????????????????????????????????????
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //??????????????????
        intent.putExtra("noFaceDetection", true);
        activity.startActivityForResult(intent, CODE_SYS_CROP);
    }

    public static Uri cameraUri;

    /**
     * ??????????????????
     * /storage/emulated/0/Android/data/com.ahtelit.zbv.myapplication/files/PicturesVIDEO_yyyMMdd_HHmmss.mp4
     */
    private void openCamera() {
        if (picPopup != null && picPopup.isShowing()) {
            picPopup.dismiss();
            picPopup = null;
        }

        String fileDir = QZXTools.getExternalStorageForFiles(MyApplication.getInstance(), Environment.DIRECTORY_PICTURES);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMG_");
        stringBuilder.append(simpleDateFormat.format(new Date()));
        stringBuilder.append(".jpg");
        File cameraFile = new File(fileDir, stringBuilder.toString());
        cameraUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName()
                    + ".fileprovider", cameraFile);
        } else {
            cameraUri = Uri.fromFile(cameraFile);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //???????????????????????????????????????????????????Uri??????????????????
        }
        //?????????????????????????????????????????????????????????onActivityResult????????????Intent??????
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        getActivity().startActivityForResult(cameraIntent, CODE_SYS_CAMERA);
    }

    /**
     * ??????????????????
     */
    private void openSysAlbum() {
        if (picPopup != null && picPopup.isShowing()) {
            picPopup.dismiss();
            picPopup = null;
        }

        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        getActivity().startActivityForResult(albumIntent, CODE_SYS_ALBUM);

        //?????????????????????????????????https://blog.csdn.net/qq_38228254/article/details/79623618
//        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        albumIntent.setType("image/*");
//        getActivity().startActivityForResult(albumIntent, CODE_SYS_ALBUM);
    }

    @Override
    public void grantPermission() {
        QZXTools.logD("?????????SD????????????");
        if (isDiscussFile) {
            handlerTBSShow();
        } else {
            if (isCamera) {
                openCamera();
            } else {
                openSysAlbum();
            }
        }
    }

    @Override
    public void denyPermission() {
        QZXTools.logD("???????????????");
        Toast.makeText(getActivity(), "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
    }


}
