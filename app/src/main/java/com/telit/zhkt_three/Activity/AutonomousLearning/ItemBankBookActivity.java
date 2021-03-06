package com.telit.zhkt_three.Activity.AutonomousLearning;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.telit.zhkt_three.Activity.BaseActivity;
import com.telit.zhkt_three.Adapter.RVQuestionAdapter;
import com.telit.zhkt_three.Adapter.tree_adpter.Node;
import com.telit.zhkt_three.Adapter.tree_adpter.NodeUtils;
import com.telit.zhkt_three.Adapter.tree_adpter.TreeViewAdapter;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.ToUsePullView;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.Fragment.Dialog.NoResultDialog;
import com.telit.zhkt_three.Fragment.Dialog.NoSercerDialog;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionDifficult;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionKnowledge;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionType;
import com.telit.zhkt_three.JavaBean.Gson.KnowledgeParamBean;
import com.telit.zhkt_three.JavaBean.Gson.KnowledgeQuestionsBean;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ???????????????ItemBankKnowledgeActivity???????????????
 * <p>
 * ?????????????????????????????????id
 */
public class ItemBankBookActivity extends BaseActivity implements ToUsePullView.SpinnerClickInterface, View.OnClickListener {

    private Unbinder unbinder;

    @BindView(R.id.knowledge_back)
    ImageView knowledge_back;

    @BindView(R.id.knowledge_rv_chapter)
    RecyclerView knowledge_rv_chapter;
    @BindView(R.id.item_bank_pull_question_type)
    ToUsePullView item_bank_pull_question;
    @BindView(R.id.item_bank_pull_difficulty)
    ToUsePullView item_bank_pull_difficulty;
    @BindView(R.id.knowledge_questions_swipeRefresh)
    SwipeRefreshLayout knowledge_swipeLayout;
    @BindView(R.id.knowledge_rv_questions)
    RecyclerView knowledge_rv_questions;

    //??????
    @BindView(R.id.item_bank_left_pull_layout)
    LinearLayout item_bank_left_pull_layout;
    @BindView(R.id.item_bank_pull_content_layout)
    RelativeLayout item_bank_pull_content_layout;
    @BindView(R.id.item_bank_pull_tag)
    FrameLayout item_bank_pull_tag;
    @BindView(R.id.item_bank_pull_icon)
    ImageView item_bank_pull_icon;

    //-----------????????????????????????
    @BindView(R.id.leak_resource)
    ImageView leak_resource;
    @BindView(R.id.leak_net_layout)
    LinearLayout leak_net_layout;
    @BindView(R.id.link_network)
    TextView link_network;

    //??????Map?????????Map,????????????????????????????????????,?????????key???????????????
    private Map<String, String> questTypeMap;
    private Map<String, String> difficultyMap;

    private String subject;
    private String learning_section;

    //?????????????????????
    private TreeViewAdapter treeAdpter;
    private List<Node> nodesList;

    //??????json?????????,??????????????????????????????
    private String json_chapter = "";
//    private String json_knoledge = "";

    //?????????????????????
    private RVQuestionAdapter questionAdapter;
    private List<QuestionBank> questionBankList;

    private LinearLayoutManager linearLayoutManager;

    //??????????????????
    private CircleProgressDialogFragment circleProgressDialogFragment;

    //??????
    Animation FromRightToLeftAnimation;
    Animation FromLeftToRightAnimation;

    //???????????????
    private int curPageNo = 1;
    //??????????????????????????? ??????????????????
//    private String pageSize = 3 + "";

    private static final int Default_Count_Load = 3;
    private static  boolean isShow=false;

    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operate_Conditions_Success = 3;
    private static final int Refresh_Start = 7;
    private static final int Refresh_End = 8;
    private static final int No_Resource = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Server_Error:
                    if (isShow){
                        QZXTools.popToast(ItemBankBookActivity.this, "??????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        if (knowledge_swipeLayout.isRefreshing())
                            knowledge_swipeLayout.setRefreshing(false);
                    }

                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popToast(ItemBankBookActivity.this, "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        if (knowledge_swipeLayout.isRefreshing())
                            knowledge_swipeLayout.setRefreshing(false);


                    }

                    break;
                case Operate_Conditions_Success:
                    if (isShow){
                        List<String> questTypeList = new ArrayList<String>(questTypeMap.keySet());
                        item_bank_pull_question.setDataList(questTypeList);
                        if (questTypeList.size() > 0) {
                            item_bank_pull_question.setPullContent(questTypeList.get(0));
                        }

                        List<String> difficultyList = new ArrayList<String>(difficultyMap.keySet());
                        item_bank_pull_difficulty.setDataList(difficultyList);
                        if (difficultyList.size() > 0) {
                            item_bank_pull_difficulty.setPullContent(difficultyList.get(0));
                        }

                        treeAdpter.setDatas(nodesList);
                        NodeUtils.tidyNodes(nodesList);
                        NodeUtils.setShowLevel(nodesList, 0);
                        knowledge_rv_chapter.setAdapter(treeAdpter);

                        //?????????????????????id????????????????????? TODO  ????????????????????????????????????0
                        assumpChapterIds();

                        //??????????????????
                        requestQuestions(false, item_bank_pull_question.getPullContent(), item_bank_pull_difficulty.getPullContent());

                        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                    }

                    break;

                case Refresh_Start:
                    if (isShow){

                        knowledge_swipeLayout.setRefreshing(true);
                    }
                    break;
                case Refresh_End:
                    if (isShow){
                        leak_resource.setVisibility(View.GONE);
                        if (knowledge_swipeLayout.isRefreshing())
                            knowledge_swipeLayout.setRefreshing(false);
                        questionAdapter.notifyDataSetChanged();
                    }

                    break;
                case No_Resource:
                    if (isShow){
                        leak_resource.setVisibility(View.VISIBLE);
                        if (knowledge_swipeLayout.isRefreshing())
                            knowledge_swipeLayout.setRefreshing(false);
                        questionBankList.clear();
                        questionAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_bank_knowledge);
        unbinder = ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        isShow=true;
        initData();

        Intent intent = getIntent();
        if (intent != null) {
            //?????????????????????
            subject = intent.getStringExtra("subject");
            learning_section = intent.getStringExtra("learning_section");
            String chapterId = intent.getStringExtra("chapterId");
            //??????????????????
            requestChapterData(chapterId);
        }
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }

        EventBus.getDefault().register(this);

        /**
         * ??????????????????
         * */
        mHandler.removeCallbacksAndMessages(null);
        QZXTools.setmToastNull();
        isShow=false;
        super.onDestroy();
        //???????????????????????? todo ????????????????????????????????????????????????????????????????????????????????????????????????
        BuriedPointUtils.buriedPoint("2033","","","","");
    }

    private void initData() {

        FromRightToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.right_to_left_show);
        FromLeftToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.left_to_right_hide);
        item_bank_pull_content_layout.setVisibility(View.GONE);

        //????????????
        knowledge_back.setOnClickListener(this);

        item_bank_pull_tag.setOnClickListener(this);

        //????????????
        link_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QZXTools.enterWifiSetting(ItemBankBookActivity.this);
            }
        });

        questTypeMap = new LinkedHashMap<>();
        difficultyMap = new LinkedHashMap<>();
        //????????????---???????????????
//        questTypeMap.put("??????", "0");
//        difficultyMap.put("??????", "0");

        item_bank_pull_question.setSpinnerClick(this);
        item_bank_pull_difficulty.setSpinnerClick(this);

        knowledge_rv_chapter.setLayoutManager(new LinearLayoutManager(this));
        knowledge_rv_chapter.setOverScrollMode(View.OVER_SCROLL_NEVER);

        treeAdpter = new TreeViewAdapter();
        nodesList = new ArrayList<>();

        knowledge_swipeLayout.setColorSchemeResources(R.color.colorAccent);
        knowledge_swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //????????????????????????
                requestQuestions(false, item_bank_pull_question.getPullContent(), item_bank_pull_difficulty.getPullContent());
            }
        });

        questionBankList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        knowledge_rv_questions.setOverScrollMode(View.OVER_SCROLL_NEVER);
        knowledge_rv_questions.setLayoutManager(linearLayoutManager);
        knowledge_rv_questions.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 7, 0, 7);
            }
        });

        questionAdapter = new RVQuestionAdapter(this, questionBankList);
        knowledge_rv_questions.setAdapter(questionAdapter);

        knowledge_rv_questions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (questionAdapter.isAllEnd()) {
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        linearLayoutManager.findLastVisibleItemPosition() >= questionBankList.size() - Default_Count_Load) {
                    questionAdapter.setFootVisible(true);
                    String questionType = item_bank_pull_question.getPullContent();
                    String difficulty = item_bank_pull_difficulty.getPullContent();
                    curPageNo++;
                    QZXTools.logE("???????????????... curPageNo=" + curPageNo, null);
                    requestQuestions(true, questionType, difficulty);
                }
            }
        });
    }

    /**
     * @param chapterId ??????????????????????????????????????????
     */
    private void requestChapterData(String chapterId) {

        if (learning_section == null || learning_section.equals("") || subject == null || subject.equals("")) {
            return;
        }

        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());
        //todo
        String url = UrlUtils.BaseUrl + UrlUtils.QuestionKnoledgeBookChapter;
        Map<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("xd", learning_section);
        mapParams.put("chid", subject);
        mapParams.put("isQueryParams", "true");
        mapParams.put("chapterId", chapterId);

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        //??????????????????
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
//                            QZXTools.logE("resultJson=" + resultJson, null);

                    Gson gson = new Gson();
                    KnowledgeParamBean knowledgeChapterBean = gson.fromJson(resultJson, KnowledgeParamBean.class);
                    QZXTools.logE("knowledgeChapterBean=" + knowledgeChapterBean, null);

                    for (int i = 0; i < knowledgeChapterBean.getResult().getQuestionTypes().size(); i++) {
                        QuestionType questionType = knowledgeChapterBean.getResult().getQuestionTypes().get(i);
                        questTypeMap.put(questionType.getQuestionTypeName(), questionType.getQuestionChannelType() + "");
                    }

                    for (int i = 0; i < knowledgeChapterBean.getResult().getQuestionDifficults().size(); i++) {
                        QuestionDifficult questionDifficult = knowledgeChapterBean.getResult().getQuestionDifficults().get(i);
                        difficultyMap.put(questionDifficult.getDifficultName(), questionDifficult.getDifficultIndex() + "");
                    }

                    //?????????
                    if (knowledgeChapterBean.getResult().getQuestionKnowledge() != null) {
                        //???????????????
                        treeInfo(knowledgeChapterBean.getResult().getQuestionKnowledge(), 0);
                    } else if (knowledgeChapterBean.getResult().getQuestionUnit() != null) {
                        //??????????????????
                        treeInfo(knowledgeChapterBean.getResult().getQuestionUnit(), 0);
                    }

                    //??????Ui
                    mHandler.sendEmptyMessage(Operate_Conditions_Success);
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    /**
     * ?????????---??????
     */
    private void treeInfo(List<QuestionKnowledge> questionKnowledgeList, int level) {
        for (int i = 0; i < questionKnowledgeList.size(); i++) {
            //???????????????
            Node node = new Node(questionKnowledgeList.get(i).getKnowledgeId() + "",
                    questionKnowledgeList.get(i).getParentId(),
                    level, questionKnowledgeList.get(i).getName());
            nodesList.add(node);
            //?????????
            if (questionKnowledgeList.get(i).getHaschild() == 1) {
                int tempLevel = level + 1;
                treeInfo(questionKnowledgeList.get(i).getQuestionKnowledgeList(), tempLevel);
            }
        }
    }

    /**
     * @param loadMore   ???????????????????????????
     * @param type       ????????????
     * @param difficulty ????????????
     */
    private void requestQuestions(boolean loadMore, String type, String difficulty) {

        if (learning_section == null || learning_section.equals("") || subject == null || subject.equals("")) {
            return;
        }

        //??????????????????
        if (!QZXTools.isNetworkAvailable()) {
            leak_net_layout.setVisibility(View.VISIBLE);
            return;
        } else {
            leak_net_layout.setVisibility(View.GONE);
        }

        //???????????????????????????????????????
        if (!loadMore) {
            //?????????????????????
            curPageNo = 1;
            questionAdapter.setAllEnd(false);
            questionBankList.clear();

            mHandler.sendEmptyMessage(Refresh_Start);
        }

        String url = UrlUtils.BaseUrl + UrlUtils.QuestionKnowledgeBankQuery;
        Map<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("xd", learning_section); //??????
        mapParams.put("chid", subject); //??????
        if (questTypeMap.get(type) != null) {
           // mapParams.put("questionChannelType", questTypeMap.get(type)); //??????
        }
        if (difficultyMap.get(difficulty) != null) {
            // TODO: 2021/1/15  ?????????????????? ???????????? ????????????
          //  mapParams.put("difficultIndex", difficultyMap.get(difficulty)); //???????????????
        }
        mapParams.put("chapterIds", json_chapter);
//        mapParams.put("tKnowledge", json_knoledge);
        mapParams.put("pageNo", curPageNo + "");

        QZXTools.logE("xd=" + learning_section + ";chid=" + subject
                + ";questionChannelType=" + questTypeMap.get(type) +
                ";difficultIndex=" + difficultyMap.get(difficulty)
                + ";chapterIds=" + json_chapter, null);

//        mapParams.put("pageSize", pageSize);

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        //??????????????????
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {

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
                    KnowledgeQuestionsBean knowledgeQuestionsBean = gson.fromJson(resultJson, KnowledgeQuestionsBean.class);
//                    QZXTools.logE("knowledgeQuestionsBean=" + knowledgeQuestionsBean, null);

                    if (knowledgeQuestionsBean.getResult().size() <= 0) {
                        if (!loadMore) {
                            mHandler.sendEmptyMessage(No_Resource);
                            return;
                        } else {
                            //???????????????
                            questionAdapter.setAllEnd(true);
                        }
                    } else {
                        for (QuestionBank questionBank : knowledgeQuestionsBean.getResult()) {
                            questionBankList.add(questionBank);
                        }
                        //??????????????????????????????
                        questionAdapter.setFootVisible(false);
                    }
                    mHandler.sendEmptyMessage(Refresh_End);
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    /**
     * ???????????????json_knoledge??????
     */
    @Subscriber(tag = Constant.Event_Choose_Tree, mode = ThreadMode.MAIN)
    public void onTreeChoose(String chooseTree) {
        if (chooseTree.equals("chooseTree")) {

            StringBuilder stringBuilder = new StringBuilder();
            //???????????????????????????
            for (int i = 0; i < nodesList.size(); i++) {
                if (nodesList.get(i).getChoosed() != Node.CHOOSE_NONE) {
                    stringBuilder.append(nodesList.get(i).getId());
                    stringBuilder.append(",");
                }
            }

            String resultJson = stringBuilder.toString();
            if (!resultJson.equals("")) {
                if (resultJson.contains(",")) {
                    json_chapter = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(","));
                } else {
                    json_chapter = resultJson;
                }
            } else {
                json_chapter = "";
            }
            QZXTools.logE("book resultJson=" + resultJson + ";json_chapter=" + json_chapter, null);

            //????????????????????????
            requestQuestions(false, item_bank_pull_question.getPullContent(),
                    item_bank_pull_difficulty.getPullContent());
        }
    }

    /**
     * ?????????????????????????????????????????????id.?????????level=0????????????  TODO 1/15 ??????????????????????????????
     */
    private void assumpChapterIds() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.get(i).getLevel() == 1) {
                stringBuilder.append(nodesList.get(i).getId());
                stringBuilder.append(",");
            }
        }

        String resultJson = stringBuilder.toString();
        if (!resultJson.equals("")) {
            if (resultJson.contains(",")) {
                json_chapter = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(","));
            } else {
                json_chapter = resultJson;
            }
        } else {
            json_chapter = "";
        }
    }

    /**
     * ??????View??????
     */
    @Override
    public void spinnerClick(View parent, String text) {
        switch (parent.getId()) {
            case R.id.item_bank_pull_question_type:
                item_bank_pull_question.setPullContent(text);
                break;
            case R.id.item_bank_pull_difficulty:
                item_bank_pull_difficulty.setPullContent(text);
                break;
        }
        //????????????????????????
        requestQuestions(false, item_bank_pull_question.getPullContent(), item_bank_pull_difficulty.getPullContent());
    }

    private boolean isShown = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_back:
                finish();
                break;
            case R.id.item_bank_pull_tag:
                if (isShown) {
                    item_bank_pull_content_layout.startAnimation(FromLeftToRightAnimation);
                    FromLeftToRightAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            iconRotate(item_bank_pull_icon, 180.0f, 0.0f);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            isShown = false;
                            item_bank_pull_content_layout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    //?????????????????????????????????
                    item_bank_pull_content_layout.setVisibility(View.VISIBLE);
                    item_bank_pull_content_layout.startAnimation(FromRightToLeftAnimation);
                    FromRightToLeftAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            iconRotate(item_bank_pull_icon, 0.0f, 180.0f);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            isShown = true;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
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
}
