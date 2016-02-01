package com.everymatch.saas.ui.questionnaire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.everymatch.saas.R;
import com.everymatch.saas.client.data.QuestionType;
import com.everymatch.saas.client.data.Types;
import com.everymatch.saas.util.EMLog;
import com.everymatch.saas.view.WheelView;

import java.util.ArrayList;

/**
 * Created by PopApp_laptop on 11/01/2016.
 */
public class QuestionTime extends QuestionnaireQuestionBaseFragment {
    public final String TAG = getClass().getName();

    private enum TIME_MODE {TIME_MODE_SEC, TIME_MODE_MIN, TIME_MODE_HOUR}

    //pace -> need to convert unit
    //time -> don't need to convert unit

    private ArrayList<String> Seconds, Minutes, Hours;

    //Views
    WheelView wheelSec, wheelMin, wheelHour;

    //Data
    private float mStep;
    int mMin, mMax;
    private int mValue;
    private TIME_MODE time_mode;
    private boolean isUnitMile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Seconds = new ArrayList<>();
        Minutes = new ArrayList<>();
        Hours = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.question_time, (ViewGroup) view.findViewById(R.id.answers_container));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //init views
        wheelSec = (WheelView) view.findViewById(R.id.wheelSec);
        wheelMin = (WheelView) view.findViewById(R.id.wheelMin);
        wheelHour = (WheelView) view.findViewById(R.id.wheelHour);
        wheelSec.setOffset(1);
        wheelSec.setOnWheelViewListener(secListener);
        wheelMin.setOffset(1);
        wheelMin.setOnWheelViewListener(secListener);
        wheelHour.setOffset(1);
        wheelHour.setOnWheelViewListener(secListener);

        //hide wheel if needed and prepare number list according to min/max/step

        loadQuestionData();

        wheelSec.setItems(Seconds);
        wheelMin.setItems(Minutes);
        wheelHour.setItems(Hours);

        recoverAnswerData();
        setAnswer();
    }

    private void loadQuestionData() {
        mStep = mQuestionAndAnswer.question.step == 0 ? 1 : mQuestionAndAnswer.question.step;
        String[] rangeStr = mQuestionAndAnswer.question.range.split(",");
        mMin = Integer.parseInt(rangeStr[0]);
        mMax = Integer.parseInt(rangeStr[1]);

        //need to change the unit according to user unit
        if (mQuestion.question_type.equals(QuestionType.PACE)) {
            if (ds.getUser().user_settings.distance.equals(Types.UNIT_MILE)) {
                EMLog.d(TAG, "user in mile...");
                isUnitMile = true;
                mMin = (int) ((double) mMin * 1.61);
                mMax = (int) ((double) mMax * 1.61);
            }
        }

        if (mMax < 60) {
            time_mode = TIME_MODE.TIME_MODE_SEC;
        } else if (mMax >= 60 && mStep < 60) {
            time_mode = TIME_MODE.TIME_MODE_MIN;
        } else if ((mMax >= 60 && mStep >= 60) || mMax > 3600) {
            time_mode = TIME_MODE.TIME_MODE_HOUR;
        }


        switch (time_mode) {
            case TIME_MODE_SEC: // hide min and hour
                wheelMin.setVisibility(View.GONE);
                wheelHour.setVisibility(View.GONE);
                getView().findViewById(R.id.tvDotsRight).setVisibility(View.GONE);
                getView().findViewById(R.id.tvDotsLeft).setVisibility(View.GONE);
                break;
            case TIME_MODE_MIN: // hide hours
                wheelHour.setVisibility(View.GONE);
                getView().findViewById(R.id.tvDotsLeft).setVisibility(View.GONE);
                break;
            case TIME_MODE_HOUR:
                break;
        }

        // prepare data lists
        if (time_mode == TIME_MODE.TIME_MODE_SEC) {
            // prepare only seconds
            for (int i = mMin; i < mMax; i += mStep) {
                Seconds.add(String.format("%02d", i));
            }

            //add minutes and hours dummy item
            Minutes.add("0");
            Hours.add("0");
        }


        if (time_mode == TIME_MODE.TIME_MODE_MIN) {
            //prepare minutes
            for (int i = mMin / 60; i < mMax / 60; i++) {
                Minutes.add(String.format("%02d", i));
            }

            //prepare seconds
            for (int i = 0; i < 60; i += mStep) {
                Seconds.add(String.format("%02d", i));
            }

            //add hours dummy item
            Hours.add("0");
        }

        if (time_mode == TIME_MODE.TIME_MODE_HOUR) {
            //prepare hours
            for (int i = mMin / 60 / 60; i < mMax / 60 / 60; i++) {
                Minutes.add(String.format("%02d", i));
            }

            //prepare minutes
            for (int i = mMin / 60; i < mMax / 60; i++) {
                Minutes.add(String.format("%02d", i));
            }

            //prepare seconds
            for (int i = 0; i < 60; i += mStep) {
                Seconds.add(String.format("%02d", i));
            }
        }
    }

    @Override
    public void recoverDefaultAnswer() {
        recoverAnswerData();
    }

    //get answered data from tha qaa
    private void recoverAnswerData() {
        if (mQuestionAndAnswer.userAnswerData != null && mQuestionAndAnswer.userAnswerData.has("value")) {
            try {
                int val = mQuestionAndAnswer.userAnswerData.getInt("value");
                mValue = val;

                int hour = mValue / 3600;
                int remainder = mValue - hour * 3600;
                int min = remainder / 60;
                remainder = remainder - min * 60;
                int sec = remainder;

                //now we get the index of the right values
                int minIndex = wheelMin.getItems().indexOf("" + String.format("%02d", min)) - 1;
                wheelMin.setSeletion(minIndex > 0 ? minIndex : 0);

                int secIndex = wheelSec.getItems().indexOf("" + String.format("%02d", sec)) - 1;
                wheelSec.setSeletion(secIndex > 0 ? secIndex : 0);

                int hourIndex = wheelSec.getItems().indexOf("" + String.format("%02d", hour)) - 1;
                wheelHour.setSeletion(hourIndex > 0 ? hourIndex : 0);

            } catch (Exception ex) {
                EMLog.e(TAG, ex.getMessage());
            }
        }
    }

    @Override
    protected String getAnswerValue() {
        return "" + mValue;
    }

    WheelView.OnWheelViewListener secListener = new WheelView.OnWheelViewListener() {
        @Override
        public void onSelected(int selectedIndex, String item) {
            setAnswer();
        }
    };

    private void setAnswer() {
        int sec = Integer.parseInt(wheelSec.getSeletedItem());
        int min = Integer.parseInt(wheelMin.getSeletedItem());
        int hour = Integer.parseInt(wheelHour.getSeletedItem());

        //update value in seconds
        mValue = sec + min * 60 + hour * 60 * 60;

        if (isUnitMile)
            mValue = (int) ((double) mValue / 1.61);

        if (time_mode == TIME_MODE.TIME_MODE_SEC)
            setAnswer(String.format("%02d", sec));
        else if (time_mode == TIME_MODE.TIME_MODE_MIN)
            setAnswer("" + String.format("%02d", min) + ":" + String.format("%02d", sec));
        if (time_mode == TIME_MODE.TIME_MODE_HOUR)
            setAnswer("" + String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec));

    }
}
