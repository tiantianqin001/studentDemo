package com.telit.zhkt_three.CustomView.QuestionView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Activity.AfterHomeWork.LearnResourceActivity;
import com.telit.zhkt_three.Activity.AfterHomeWork.TypicalAnswersActivity;
import com.telit.zhkt_three.Activity.MistakesCollection.MistakesImproveActivity;
import com.telit.zhkt_three.Activity.MistakesCollection.PerfectAnswerActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.TempSaveItemInfo;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.AnswerItem;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.JavaBean.WorkOwnResult;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.greendao.LocalTextAnswersBeanDao;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTagHandler;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: qzx
 * Date: 2019/5/19 10:38
 * <p>
 * QuestionBank??????---????????????
 * ???????????????????????????????????????
 * <p>
 * ???????????????????????????  getQuestionChannelType()--->???????????????
 * <p>
 * ????????????????????????
 * <p>
 * ???????????????????????????
 * <p>
 * todo ???????????????????????????????????????bug
 */
public class NewKnowledgeQuestionView extends RelativeLayout {

    //???????????????
    private QuestionBank questionBank;

    /**
     * ?????????????????????????????????????????????
     */
    private List<String> saveMultiList;

    /**
     * ???????????????????????????
     */
    private int curPosition;

    /**
     * ????????????
     */
    private TextView Item_Bank_head_title;
    private HtmlTextView Item_Bank_head_content;
    //???????????????????????????
//    private TextView Item_Bank_head_score;

    private TextView Item_Bank_head_promote;

    private TextView Item_Bank_head_good_answer;
    private TextView img_total_typical_answers;
    private TextView img_total_learn_resource;

    private TextView Item_Bank_head_study;

    /**
     * ???List?????????
     */
    private LinearLayout Item_Bank_options_layout;

    /**
     * List?????????
     */
    private LinearLayout Item_Bank_list_question_layout;

    /**
     * ??????
     */
    private ScrollView Item_Bank_Answer_Scroll;

    private LinearLayout Item_Bank_Answer_Layout;
    private TextView Item_Bank_my_Answer;
    private TextView Item_Bank_right_Answer;
    private LinearLayout Item_Bank_Point;
    private ImageView Item_Bank_Img_Point;
    private LinearLayout Item_Bank_Analysis;
    private ImageView Item_Bank_Img_Analysis;
    private LinearLayout Item_Bank_Answer;
    private ImageView Item_Bank_Img_Answer;

    private ImageView iv_collect;

    private TextView Item_Bank_Show_Remark;

    /**
     * 0?????????  1 ?????????  2 ?????????
     * ?????????????????????????????? todoView
     * ????????????????????????????????? showView
     * ???????????????????????????????????????????????? showView plus AnswerView
     */
    private String status;

    /**
     * ?????????????????????
     */
    private boolean isMistaken;

    /**
     * ??????????????????
     */
    private String showAnswerDate;

    private Context context;

    /**
     * ??????????????????
     *
     * @param questionBank ????????????????????????
     * @param status       ????????????????????????????????????
     */
    public void setQuestionInfo(QuestionBank questionBank, int curPosition, String status, boolean isMistaken) {
//        QZXTools.logE("setQuestionInfo status=" + status, null);
        if (status.equals("2")) {
            //???????????????????????????
            questionBank.setShownAnswer(true);
        }
        this.isMistaken = isMistaken;
        this.questionBank = questionBank;
        this.curPosition = curPosition;
        this.status = status;
        showAnswerDate = questionBank.getAnswerPublishDate();
        initData();
    }

    public NewKnowledgeQuestionView(Context context) {
        this(context, null);
    }

    public NewKnowledgeQuestionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewKnowledgeQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.item_new_bank_view_layout, this, true);
        //????????????????????????????????????????????????????????????questionInfo????????????????????????set????????????
        initView();
    }

    private void initView() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "PingFang-SimpleBold.ttf");

        saveMultiList = new ArrayList<>();

        Item_Bank_head_title = findViewById(R.id.Item_Bank_head_title);
        Item_Bank_head_content = findViewById(R.id.Item_Bank_head_content);
//        Item_Bank_head_score = findViewById(R.id.Item_Bank_head_score);
        Item_Bank_head_promote = findViewById(R.id.Item_Bank_head_promote);
        Item_Bank_head_good_answer = findViewById(R.id.Item_Bank_head_good_answer);
        img_total_learn_resource = findViewById(R.id.img_total_learn_resource);
        img_total_typical_answers = findViewById(R.id.img_total_typical_answers);
        Item_Bank_head_study = findViewById(R.id.Item_Bank_head_study);
        Item_Bank_head_title.setTypeface(typeface);
        Item_Bank_head_content.setTypeface(typeface);
//        Item_Bank_head_score.setTypeface(typeface);
        Item_Bank_head_promote.setTypeface(typeface);
        Item_Bank_head_good_answer.setTypeface(typeface);
        img_total_learn_resource.setTypeface(typeface);
        img_total_typical_answers.setTypeface(typeface);
        Item_Bank_head_study.setTypeface(typeface);


        Item_Bank_options_layout = findViewById(R.id.Item_Bank_options_layout);
        Item_Bank_list_question_layout = findViewById(R.id.Item_Bank_list_question_layout);

        Item_Bank_Answer_Scroll = findViewById(R.id.Item_Bank_Answer_Scroll);
        Item_Bank_Answer_Layout = findViewById(R.id.Item_Bank_Answer_Layout);
        Item_Bank_my_Answer = findViewById(R.id.Item_Bank_my_Answer);
        Item_Bank_my_Answer.setTypeface(typeface);

        Item_Bank_right_Answer = findViewById(R.id.Item_Bank_right_Answer);
        Item_Bank_right_Answer.setTypeface(typeface);

        Item_Bank_Point = findViewById(R.id.Item_Bank_Point);
        Item_Bank_Img_Point = findViewById(R.id.Item_Bank_Img_Point);
        Item_Bank_Analysis = findViewById(R.id.Item_Bank_Analysis);
        Item_Bank_Img_Analysis = findViewById(R.id.Item_Bank_Img_Analysis);
        Item_Bank_Answer = findViewById(R.id.Item_Bank_Answer);
        Item_Bank_Img_Answer = findViewById(R.id.Item_Bank_Img_Answer);

        Item_Bank_Show_Remark = findViewById(R.id.Item_Bank_Show_Remark);
        Item_Bank_Show_Remark.setTypeface(typeface);

        TextView Item_Bank_Tv_Point = findViewById(R.id.Item_Bank_Tv_Point);
        TextView Item_Bank_Tv_Analysis = findViewById(R.id.Item_Bank_Tv_Analysis);
        TextView Item_Bank_Tv_Answer = findViewById(R.id.Item_Bank_Tv_Answer);
        Item_Bank_Tv_Point.setTypeface(typeface);
        Item_Bank_Tv_Analysis.setTypeface(typeface);
        Item_Bank_Tv_Answer.setTypeface(typeface);


        iv_collect = findViewById(R.id.iv_collect);
        iv_collect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCollectClickListener!=null){
                    onCollectClickListener.OnCollectClickListener(questionBank,curPosition);
                }
            }
        });

        Item_Bank_Show_Remark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> imgFilePathList = (ArrayList<String>) questionBank.getTeaDescFile();
                Intent intent = new Intent(getContext(), ImageLookActivity.class);
                intent.putStringArrayListExtra("imgResources", imgFilePathList);
                intent.putExtra("NeedComment", false);
                intent.putExtra("curImgIndex", 0);
                getContext().startActivity(intent);
            }
        });

        Item_Bank_head_promote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getBundle();
                String knowledge_json = bundle.getString("knowledge_json");
                if (TextUtils.isEmpty(knowledge_json)) {
                    QZXTools.popCommonToast(getContext(), "????????????????????????", false);
                    return;
                }

                /**
                 * ???????????????????????????
                 * */
                Intent intent = new Intent(getContext(), MistakesImproveActivity.class);
                intent.putExtra("improvement", getBundle());
                getContext().startActivity(intent);

                QZXTools.logE("?????????",null);

                //?????????????????? ??????  TODO ???????????????
                BuriedPointUtils.buriedPoint("2021","","","","");
            }
        });

        Item_Bank_head_study.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Item_Bank_head_good_answer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PerfectAnswerActivity.class);
                intent.putExtra("questionId", questionBank.getId() + "");
                intent.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent);
            }
        });

        img_total_learn_resource.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_learn_resource = new Intent(getContext(), LearnResourceActivity.class);
                intent_learn_resource.putExtra("questionId", questionBank.getId() + "");
                intent_learn_resource.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent_learn_resource);
            }
        });

        img_total_typical_answers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_typical_answers = new Intent(getContext(), TypicalAnswersActivity.class);
                intent_typical_answers.putExtra("questionId", questionBank.getId() + "");
                intent_typical_answers.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent_typical_answers);
            }
        });
    }

    private void initData() {
        Item_Bank_options_layout.removeAllViews();
        Item_Bank_list_question_layout.removeAllViews();

        if (isMistaken && bundle != null && !TextUtils.isEmpty(bundle.getString("knowledge_json"))) {
            Item_Bank_head_promote.setVisibility(VISIBLE);
        } else {
            Item_Bank_head_promote.setVisibility(GONE);
        }

        //???????????????????????????????????????
        Item_Bank_head_good_answer.setVisibility(GONE);
        img_total_typical_answers.setVisibility(GONE);

        QZXTools.logE("NewKnowledgeQuestionView",null);

        //????????????????????????
        if (questionBank.getTeaDescFile() == null || questionBank.getTeaDescFile().size() <= 0) {
            Item_Bank_Show_Remark.setVisibility(GONE);
        } else {
            Item_Bank_Show_Remark.setVisibility(VISIBLE);
        }

        getHeadAndOptionsInfo();

        if ("2".equals(status) || !TextUtils.isEmpty(showAnswerDate)) {
            showResumeAnswer();
        }

        if (bundle != null) {
            Item_Bank_head_promote.setVisibility(VISIBLE);
        } else {
            Item_Bank_head_promote.setVisibility(GONE);
        }

        if ("0".equals(status)||(bundle!=null&&"1".equals(bundle.getString("flag")))) {
            iv_collect.setVisibility(GONE);
        }

        setCollect(questionBank.getIsCollect());
    }

    /**
     * ????????????
     *
     * @param isCollect
     */
    public void setCollect(String isCollect){
        if ("0".equals(isCollect)){
            iv_collect.setImageResource(R.mipmap.collect_gray_icon);
        }else {
            iv_collect.setImageResource(R.mipmap.collect_red_icon);
        }
    }

    /**
     * ???????????????ID
     */
    public static String subjQuestionId;

    /**
     * ??????????????????????????????
     * ??????ownList
     * <p>
     * "ownList": [
     * {
     * "state": "2",
     * "score": "0",
     * "answerContent": "Fggb"
     * }
     * ],
     */
    private void getHeadAndOptionsInfo() {
        //????????????List
//        QZXTools.logE("List=" + questionBank.getList(), null);
        if (TextUtils.isEmpty(questionBank.getList()) || questionBank.getList().equals("NULL")) {
            //??????Item_Bank_options_layout??????
            LayoutParams layoutParams = (LayoutParams) Item_Bank_Answer_Scroll.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, Item_Bank_options_layout.getId());

            Item_Bank_options_layout.setVisibility(VISIBLE);
            Item_Bank_list_question_layout.setVisibility(GONE);

            String optionJson = questionBank.getAnswerOptions();
            //????????????
            switch (questionBank.getQuestionChannelType()) {
                case Constant.Single_Choose:
                case Constant.Judge_Item:
                    if (questionBank.getQuestionChannelType() == Constant.Judge_Item) {
                        Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    } else {
                        Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    }

                    if (status.equals(Constant.Todo_Status) || status.equals(Constant.Retry_Status)) {
                        //????????????
                        if (!TextUtils.isEmpty(optionJson)) {
                            Gson gson = new Gson();
                            Map<String, String> optionMap = gson.fromJson(optionJson, new TypeToken<Map<String, String>>() {
                            }.getType());
                            Iterator<Map.Entry<String, String>> iterator = optionMap.entrySet().iterator();

                            while (iterator.hasNext()) {
                                Map.Entry<String, String> entry = iterator.next();

                                //?????????????????????
                                LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                                JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                judgeSelectToDoView.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //????????????????????????????????????
                                        for (int i = 0; i < Item_Bank_options_layout.getChildCount(); i++) {
                                            JudgeSelectToDoView childJudeSelectedToDoView = (JudgeSelectToDoView) Item_Bank_options_layout.getChildAt(i);
                                            if (childJudeSelectedToDoView.isSelected()) {
                                                childJudeSelectedToDoView.handSelectedStatus();
                                            }
                                        }

                                        JudgeSelectToDoView selectedView = (JudgeSelectToDoView) v;

                                        selectedView.handSelectedStatus();

                                        //?????????????????????
                                        int selectedIndex = Item_Bank_options_layout.indexOfChild(selectedView);

                                        //-------------------------?????????????????????????????????id
                                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                                        localTextAnswersBean.setHomeworkId(questionBank.getHomeworkId());
                                        localTextAnswersBean.setQuestionId(questionBank.getId() + "");
                                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                                        localTextAnswersBean.setQuestionType(questionBank.getQuestionChannelType());
                                        List<AnswerItem> answerItems = new ArrayList<>();
                                        AnswerItem answerItem = new AnswerItem();
                                        answerItem.setContent(entry.getKey());
                                        answerItems.add(answerItem);
                                        localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                                        //???????????????????????????
                                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                                        //-------------------------?????????????????????????????????id
                                    }
                                });
                                judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());
                                //???????????????????????????--------------???????????????????????????????????????????????????
                                if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                    List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                    for (AnswerItem answerItem : answerItems) {
                                        if (entry.getKey().equals(answerItem.getContent())) {
                                            judgeSelectToDoView.handSelectedStatus();
                                        }
                                    }
                                }
                                Item_Bank_options_layout.addView(judgeSelectToDoView);
                            }
                        }
                    } else {
                        //?????????????????????
                        if (!TextUtils.isEmpty(optionJson)) {
                            Gson gson = new Gson();
                            Map<String, String> optionMap = gson.fromJson(optionJson, new TypeToken<Map<String, String>>() {
                            }.getType());
                            Iterator<Map.Entry<String, String>> iterator = optionMap.entrySet().iterator();

                            while (iterator.hasNext()) {
                                Map.Entry<String, String> entry = iterator.next();

                                if (questionBank.getOwnList() != null && questionBank.getOwnList().size() > 0) {
                                    JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                    judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());
                                    //???????????????selectBeans.get(i).getOptions()
                                    String myAnswer = questionBank.getOwnList().get(0).getAnswerContent();
                                    if (myAnswer.equals(entry.getKey())) {
                                        judgeSelectToDoView.handSelectedStatus();
                                    }
                                    Item_Bank_options_layout.addView(judgeSelectToDoView);
                                } else {
                                    //?????????????????????
                                    LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                            .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                                    LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                                    LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                                    JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                    judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());

                                    //???????????????????????????--------------???????????????????????????????????????????????????
                                    if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                        List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                        for (AnswerItem answerItem : answerItems) {
                                            if (entry.getKey().equals(answerItem.getContent())) {
                                                judgeSelectToDoView.handSelectedStatus();
                                            }
                                        }
                                    }
                                    Item_Bank_options_layout.addView(judgeSelectToDoView);
                                }
                            }
                        }
                    }
                    break;
                case Constant.Multi_Choose:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");

                    if (status.equals(Constant.Todo_Status)) {

                        //????????????
                        if (!TextUtils.isEmpty(optionJson)) {
                            Gson gson = new Gson();
                            Map<String, String> optionMap = gson.fromJson(optionJson, new TypeToken<Map<String, String>>() {
                            }.getType());

                            //?????????????????????,???????????????????????????????????????
                            LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                            Iterator<Map.Entry<String, String>> iterator = optionMap.entrySet().iterator();

                            while (iterator.hasNext()) {

                                Map.Entry<String, String> entry = iterator.next();
//                                QZXTools.logE("Multi_Choose key=" + entry.getKey() + ";value=" + entry.getValue(), null);

                                saveMultiList.add(entry.getKey());

                                JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                judgeSelectToDoView.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        JudgeSelectToDoView selectedView = (JudgeSelectToDoView) v;

                                        selectedView.handSelectedStatus();

                                        //-------------------------?????????????????????????????????id
                                        //??????????????????
                                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                                        localTextAnswersBean.setHomeworkId(questionBank.getHomeworkId());
                                        localTextAnswersBean.setQuestionId(questionBank.getId() + "");
                                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                                        localTextAnswersBean.setQuestionType(questionBank.getQuestionChannelType());
                                        List<AnswerItem> answerItems = new ArrayList<>();
                                        for (int j = 0; j < Item_Bank_options_layout.getChildCount(); j++) {
                                            //????????????
                                            if (Item_Bank_options_layout.getChildAt(j).isSelected()) {
                                                AnswerItem answerItem = new AnswerItem();
                                                answerItem.setContent(saveMultiList.get(j));
                                                answerItems.add(answerItem);
                                            }
                                        }

//                                        QZXTools.logE("save multi choose answerItems=" + answerItems, null);

                                        localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                                        //???????????????????????????
                                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                                        //-------------------------?????????????????????????????????id

                                    }
                                });
                                judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());
                                //???????????????????????????
                                if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                    List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                    for (AnswerItem answerItem : answerItems) {
                                        if (entry.getKey().equals(answerItem.getContent())) {
                                            judgeSelectToDoView.handSelectedStatus();
                                        }
                                    }
                                }
                                Item_Bank_options_layout.addView(judgeSelectToDoView);
                            }
                        }

                    } else {

                        //????????????
                        if (!TextUtils.isEmpty(optionJson)) {
                            Gson gson = new Gson();
                            Map<String, String> optionMap = gson.fromJson(optionJson, new TypeToken<Map<String, String>>() {
                            }.getType());
                            Iterator<Map.Entry<String, String>> iterator = optionMap.entrySet().iterator();

                            while (iterator.hasNext()) {
                                Map.Entry<String, String> entry = iterator.next();

                                if (questionBank.getOwnList() != null && questionBank.getOwnList().size() > 0) {
                                    JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                    judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());
                                    //???????????????selectBeans.get(i).getOptions()
                                    List<WorkOwnResult> myAnswers = questionBank.getOwnList();
                                    for (WorkOwnResult myAnswer : myAnswers) {
                                        if ((myAnswer.getAnswerContent()).equals(entry.getKey())) {
                                            judgeSelectToDoView.handSelectedStatus();
                                        }
                                    }
                                    Item_Bank_options_layout.addView(judgeSelectToDoView);
                                } else {
                                    //?????????????????????,???????????????????????????????????????
                                    LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                            .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                                    LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                                    LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                                    JudgeSelectToDoView judgeSelectToDoView = new JudgeSelectToDoView(getContext());
                                    judgeSelectToDoView.fillOptionAndContent(entry.getKey(), entry.getValue());

                                    //???????????????????????????
                                    if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                        List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                        for (AnswerItem answerItem : answerItems) {
                                            if (entry.getKey().equals(answerItem.getContent())) {
                                                judgeSelectToDoView.handSelectedStatus();
                                            }
                                        }
                                    }
                                    Item_Bank_options_layout.addView(judgeSelectToDoView);
                                }
                            }
                        }

                    }

                    break;
                case Constant.Fill_Blank:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");


                    if (status.equals(Constant.Todo_Status)) {

                        //?????????????????????,???????????????????????????????????????
                        LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                        String ItemBankTitle = questionBank.getQuestionText();
                        //??????"^__\\d+__$"??????
                        String reg = "__\\d+__";
                        Pattern pattern = Pattern.compile(reg);
                        Matcher matcher = pattern.matcher(ItemBankTitle);

                        int i = -1;
                        while (matcher.find()) {
                            i++;

                            //?????????????????????
                            FillBlankToDoView fillBlankToDoView = new FillBlankToDoView(getContext());

                            //?????????????????????????????????????????????????????????
                            fillBlankToDoView.fillDatas(i);
                            //???????????????????????????
                            if (localTextAnswersBean != null) {
//                            QZXTools.logE("fill blank Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                for (int k = 0; k < answerItems.size(); k++) {
//                                    String content = answerItem.getContent();
//                                    String[] splitContent = content.split(":");
//                                    //?????????
//                                    if (Integer.parseInt(splitContent[0]) == i) {
//                                        //??????????????????????????????(??????+?????? )????????????????????????
//                                        if (splitContent.length > 1) {
//                                            //??????????????????????????????????????????????????????
//                                            fillBlankToDoView.fillDatas(Integer.parseInt(splitContent[0]), splitContent[1]);
//                                        } else {
//                                            fillBlankToDoView.fillDatas(Integer.parseInt(splitContent[0]), "");
//                                        }
//                                    }

                                    if ((i + 1) == Integer.parseInt(answerItems.get(k).getBlanknum())) {
                                        //?????????????????????????????????????????????
                                        String content = answerItems.get(k).getContent();
                                        String blankNum = answerItems.get(k).getBlanknum();

                                        if (content == null) {
                                            fillBlankToDoView.fillDatas(Integer.parseInt(blankNum) - 1, "");
                                        } else {
                                            fillBlankToDoView.fillDatas(Integer.parseInt(blankNum) - 1, content);
                                        }

                                        break;
                                    }
                                }
                            }

                            //??????????????????????????????????????????????????????????????????????????????
                            fillBlankToDoView.fill_blank_content.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    //???????????????????????????
                                    //-------------------------?????????????????????????????????id
                                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                                    localTextAnswersBean.setHomeworkId(questionBank.getHomeworkId());
                                    localTextAnswersBean.setQuestionId(questionBank.getId() + "");
                                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                                    localTextAnswersBean.setQuestionType(questionBank.getQuestionChannelType());
                                    List<AnswerItem> answerItems = new ArrayList<>();
                                    for (int j = 0; j < Item_Bank_options_layout.getChildCount(); j++) {
                                        //???????????????????????????????????????
                                        FillBlankToDoView blankedView = (FillBlankToDoView) Item_Bank_options_layout.getChildAt(j);
                                        AnswerItem answerItem = new AnswerItem();
                                        //??????????????????:??????index:content??????,blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum((j + 1) + "");
                                        answerItem.setContent(blankedView.fill_blank_content.getText().toString().trim());
                                        //??????????????????????????????
//                                        answerItem.setContent(j + ":" + blankedView.fill_blank_content.getText().toString().trim());
                                        answerItems.add(answerItem);
                                    }
                                    localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                                    //???????????????????????????
                                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                                    //-------------------------?????????????????????????????????id
                                }
                            });
                            Item_Bank_options_layout.addView(fillBlankToDoView);
                        }

                    } else {

                        if (questionBank.getOwnList() != null && questionBank.getOwnList().size() > 0) {

                            //????????????????????????????????????????????????key???????????????????????????????????????????????????
                            List<WorkOwnResult> myAnswers = questionBank.getOwnList();


                            StringBuilder stringBuilder = new StringBuilder();

                            for (int i = 0; i < myAnswers.size(); i++) {
                                try {
                                    JSONObject jsonObject1 = new JSONObject(myAnswers.get(i).getAnswerContent());
                                    String s = jsonObject1.toString();
                                    // {"2":"?????????"}
                                    if (i > 0) {
                                        s = s.replace("{", "");
                                    }

                                    if (i < (myAnswers.size() - 1)) {
                                        s = s.replace("}", ",");
                                    }
                                    QZXTools.logE("s=" + s, null);

                                    stringBuilder.append(s);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            try {
                                JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                                String ItemBankTitle = questionBank.getQuestionText();
                                //??????"^__\\d+__$"??????
                                String reg = "__\\d+__";
                                Pattern pattern = Pattern.compile(reg);
                                Matcher matcher = pattern.matcher(ItemBankTitle);

                                int i = -1;
                                while (matcher.find()) {
                                    i++;

                                    //?????????????????????
                                    FillBlankToDoView fillBlankToDoView = new FillBlankToDoView(getContext());
                                    fillBlankToDoView.setNormalTV();

                                    //???????????????????????????????????????????????????
                                    if (i < myAnswers.size()) {
                                        //??????json????????????
                                        fillBlankToDoView.fillDatas(i, jsonObject.getString((i + 1) + ""));
                                    } else {
                                        fillBlankToDoView.fillDatas(i);
                                    }

                                    Item_Bank_options_layout.addView(fillBlankToDoView);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {

                            //?????????????????????,???????????????????????????????????????
                            LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                            String ItemBankTitle = questionBank.getQuestionText();
                            //??????"^__\\d+__$"??????
                            String reg = "__\\d+__";
                            Pattern pattern = Pattern.compile(reg);
                            Matcher matcher = pattern.matcher(ItemBankTitle);

                            int i = -1;
                            while (matcher.find()) {
                                i++;

                                //?????????????????????
                                FillBlankToDoView fillBlankToDoView = new FillBlankToDoView(getContext());
                                fillBlankToDoView.setNormalTV();

                                //?????????????????????????????????????????????????????????
                                fillBlankToDoView.fillDatas(i);
                                //???????????????????????????
                                if (localTextAnswersBean != null) {
//                            QZXTools.logE("fill blank Answer localTextAnswersBean=" + localTextAnswersBean, null);
                                    List<AnswerItem> answerItems = localTextAnswersBean.getList();
                                    for (int k = 0; k < answerItems.size(); k++) {
//                                        String content = answerItem.getContent();
//                                        String[] splitContent = content.split(":");
//                                        //?????????
//                                        if (Integer.parseInt(splitContent[0]) == i) {
//                                            //??????????????????????????????(??????+?????? )????????????????????????
//                                            if (splitContent.length > 1) {
//                                                //??????????????????????????????????????????????????????
//                                                fillBlankToDoView.fillDatas(Integer.parseInt(splitContent[0]), splitContent[1]);
//                                            } else {
//                                                fillBlankToDoView.fillDatas(Integer.parseInt(splitContent[0]), "");
//                                            }
//                                        }

                                        if ((i + 1) == Integer.parseInt(answerItems.get(k).getBlanknum())) {
                                            //?????????????????????????????????????????????
                                            String content = answerItems.get(k).getContent();
                                            String blankNum = answerItems.get(k).getBlanknum();

                                            if (content == null) {
                                                fillBlankToDoView.fillDatas(Integer.parseInt(blankNum), "");
                                            } else {
                                                fillBlankToDoView.fillDatas(Integer.parseInt(blankNum), content);
                                            }

                                            break;
                                        }
                                    }
                                }
                                Item_Bank_options_layout.addView(fillBlankToDoView);
                            }

                        }

                    }
                    break;
                case Constant.Subject_Item:

                    if (status.equals(Constant.Todo_Status)) {

                        Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");

                        //?????????????????????,???????????????????????????????????????
                        LocalTextAnswersBean localTextAnswersBean_sub = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                        //?????????????????????
                        BankSubjectiveToDoView subjectiveToDoView = new BankSubjectiveToDoView(getContext());
                        subjectiveToDoView.setQuestionInfo(questionBank);
                        //????????????,?????????????????????localTextAnswersBean???????????????
                        subjectiveToDoView.showImgsAndContent(localTextAnswersBean_sub);
                        //???????????????
                        subjectiveToDoView.hideAnswerTools(false);

                        Item_Bank_options_layout.addView(subjectiveToDoView);

                    } else {

                        //??????????????????
                       Item_Bank_head_good_answer.setVisibility(VISIBLE);
                       img_total_typical_answers.setVisibility(VISIBLE);

                        Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");

                        //?????????????????????,???????????????????????????????????????
                        LocalTextAnswersBean localTextAnswersBean_sub = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                        //?????????????????????
                        BankSubjectiveToDoView subjectiveToDoView = new BankSubjectiveToDoView(getContext());
                        subjectiveToDoView.hideAnswerTools(true);
                        subjectiveToDoView.setQuestionInfo(questionBank);
                        //????????????,?????????????????????localTextAnswersBean???????????????
                        subjectiveToDoView.showImgsAndContent(localTextAnswersBean_sub);

                        Item_Bank_options_layout.addView(subjectiveToDoView);

                    }
                    break;
            }
        } else {
            //??????Item_Bank_list_question_layout??????
            LayoutParams layoutParams = (LayoutParams) Item_Bank_Answer_Scroll.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, Item_Bank_list_question_layout.getId());

            Item_Bank_options_layout.setVisibility(GONE);
            Item_Bank_list_question_layout.setVisibility(VISIBLE);

            //??????
            switch (questionBank.getQuestionChannelType()) {
                case Constant.Single_Choose:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    break;
                case Constant.Multi_Choose:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    break;
                case Constant.Fill_Blank:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    break;
                case Constant.Subject_Item:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    break;
                case Constant.Judge_Item:
                    Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                    break;
            }

            List<QuestionBank> questionBankList = questionBank.getQuestionBanks();
            for (int i = 0; i < questionBankList.size(); i++) {
                NewKnowledgeToDoView newKnowledgeToDoView = new NewKnowledgeToDoView(getContext());
                newKnowledgeToDoView.fillDatas(questionBankList.get(i), i, status, questionBank.getQuestionChannelType(),
                        questionBank.getHomeworkId());
                Item_Bank_list_question_layout.addView(newKnowledgeToDoView);
            }
        }
        //????????????
        String ItemBankTitle = questionBank.getQuestionText();
        if (TextUtils.isEmpty(ItemBankTitle))return;
//        Item_Bank_head_content.setHtml(ItemBankTitle, new HtmlHttpImageGetter(Item_Bank_head_content));

        //?????????????????? ---????????????????????????????????????????????????

        /**
         *     SpannableString spannableString = new SpannableString("??????????????????");
         spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 2,
         spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         tv5.setText(spannableString);
         * */

        String score = questionBank.getScore();
        String scoreStr;
        if (status.equals("2")) {
            /**
             * notes:??????????????????ownscore????????????ownList??????score???
             * */
            String myScore = questionBank.getOwnscore();

//            double totalDScore = 0.0;

//            boolean isException = false;

//            List<WorkOwnResult> workOwnResults = questionBank.getOwnList();
//            if (workOwnResults != null && workOwnResults.size() > 0) {
//                for (WorkOwnResult workOwnResult : workOwnResults) {
//                    String scoreTag = workOwnResult.getScore();
//                    try {
//                        double dScore = Double.parseDouble(scoreTag);
//                        totalDScore += dScore;
//                    } catch (Exception e) {
//                        isException = true;
//                        //????????????????????????Double????????????????????????
//                        e.printStackTrace();
//                        CrashReport.postCatchedException(e);
//                    }
//                }
//            }
//
//            if (isException) {
//                scoreStr = "(????????????" + score + "???,???????????????" + totalDScore + "???;????????????????????????";
//            } else {
//                scoreStr = "(????????????" + score + "???,???????????????" + totalDScore + "??????";
//            }
//            Item_Bank_head_score.setText("(????????????" + score + "???,???????????????" + myScore + "??????");

            scoreStr = "(????????????" + score + "???,???????????????" + myScore + "??????";

        } else {
            scoreStr = "(" + score + "???)";
//            Item_Bank_head_score.setText("(" + score + "???)");
        }

        /**
         * ??????????????????TextView??????????????????????????????
         * */
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        spannableStringBuilder.append(ItemBankTitle);
//        spannableStringBuilder.append(scoreStr);
//        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#ff4444"));
//        spannableStringBuilder.setSpan(foregroundColorSpan, ItemBankTitle.length(), spannableStringBuilder.length(),
//                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        Item_Bank_head_content.setText(spannableStringBuilder);


        /**
         * ???????????????HtmlView???setHtml??????,???????????????????????????????????????????????????
         * */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ItemBankTitle);
        stringBuilder.append(scoreStr);
        //???????????????????????????
        Item_Bank_head_content.setHtml(stringBuilder.toString(), new HtmlHttpImageGetter(Item_Bank_head_content),
                true, new HtmlTagHandler.FillBlankInterface() {
                    @Override
                    public void addSpans(Editable output) {
                        String content = output.toString();
                        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#ff4444"));
                        output.setSpan(foregroundColorSpan, ItemBankTitle.length(), content.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                });

    }

    /**
     * ????????????
     */
    private void getAnswerInfo() {

        //??????????????????
        if (questionBank.isShownAnswer() || !TextUtils.isEmpty(showAnswerDate)) {
            Item_Bank_Answer_Layout.setVisibility(VISIBLE);
        } else {
            Item_Bank_Answer_Layout.setVisibility(GONE);
            return;
        }

        //????????????
        Date date = new Date();
        if (!TextUtils.isEmpty(showAnswerDate) && QZXTools.getDateValue(showAnswerDate, "yyyy-MM-dd HH:mm:ss")
                > date.getTime()) {
            //????????????????????????????????????????????????????????????
            Item_Bank_Answer_Layout.setVisibility(GONE);
            return;
        } else if (!TextUtils.isEmpty(showAnswerDate) && QZXTools.getDateValue(showAnswerDate, "yyyy-MM-dd HH:mm:ss")
                <= date.getTime()) {
            //?????????????????????????????????
            Item_Bank_Answer_Layout.setVisibility(VISIBLE);
        }

        //????????????
        if (questionBank.getQuestionChannelType() == Constant.ItemBank_Judge
                || questionBank.getQuestionChannelType() == Constant.Single_Choose
                || questionBank.getQuestionChannelType() == Constant.Multi_Choose) {

            if (questionBank.getSaveInfos() != null) {
                StringBuffer stringBuffer = new StringBuffer();
                for (TempSaveItemInfo tempSaveItemInfo : questionBank.getSaveInfos()) {
                    stringBuffer.append(tempSaveItemInfo.getKey());
                    stringBuffer.append(" ");
                }
                Item_Bank_my_Answer.setVisibility(VISIBLE);
                Item_Bank_my_Answer.setText("???????????????" + stringBuffer.toString().trim());
            } else {
                Item_Bank_my_Answer.setVisibility(GONE);
            }
        }

        //????????????
        if (TextUtils.isEmpty(questionBank.getAnswerText())) {
            Item_Bank_right_Answer.setVisibility(GONE);
        } else {
            Item_Bank_right_Answer.setVisibility(VISIBLE);
            Item_Bank_right_Answer.setText("???????????????" + questionBank.getAnswerText());
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getKnowledge())) {
            Item_Bank_Point.setVisibility(GONE);
        } else {
            Item_Bank_Point.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getKnowledge();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Point);
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getExplanation())) {
            Item_Bank_Analysis.setVisibility(GONE);
        } else {
            Item_Bank_Analysis.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getExplanation();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Analysis);
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getAnswer())) {
            Item_Bank_Answer.setVisibility(GONE);
        } else {
            Item_Bank_Answer.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getAnswer();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Answer);
        }
    }

    /**
     * ???????????????????????????
     */
    private void showResumeAnswer() {
        //????????????list
        if (Item_Bank_list_question_layout.getVisibility() == VISIBLE) {
            for (int i = 0; i < Item_Bank_list_question_layout.getChildCount(); i++) {
                NewKnowledgeToDoView newKnowledgeToDoView = (NewKnowledgeToDoView) Item_Bank_list_question_layout.getChildAt(i);

                if (newKnowledgeToDoView.getQuestionBank().isShownAnswer()) {
                    newKnowledgeToDoView.getQuestionBank().setShownAnswer(false);
                } else {
                    newKnowledgeToDoView.getQuestionBank().setShownAnswer(true);
                }

                newKnowledgeToDoView.getAnswerInfo();
            }
        }

        //????????????
//        if (questionBank.isShownAnswer()) {
//            questionBank.setShownAnswer(false);
//        } else {
//            questionBank.setShownAnswer(true);
//        }
        getAnswerInfo();
    }

    /**
     * ???????????????Bundle
     */
    private Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    private OnCollectClickListener onCollectClickListener;

    public interface OnCollectClickListener {
        void OnCollectClickListener(QuestionBank questionBank,int curPosition);
    }

    public void setOnCollectClickListener(OnCollectClickListener onCollectClickListener) {
        this.onCollectClickListener = onCollectClickListener;
    }
}
