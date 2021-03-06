package com.everymatch.saas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everymatch.saas.R;
import com.everymatch.saas.client.data.DataManager;
import com.everymatch.saas.client.data.DataStore;
import com.everymatch.saas.client.data.EMColor;
import com.everymatch.saas.server.Data.DataEvent;
import com.everymatch.saas.util.EMLog;
import com.everymatch.saas.util.ShapeDrawableUtils;
import com.everymatch.saas.util.Utils;
import com.everymatch.saas.view.BaseIconTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by PopApp_laptop on 09/09/2015.
 */
public class EventsAdapter extends EmBaseAdapter<DataEvent> {
    public final String TAG = getClass().getName();

    private LayoutInflater inflater;
    private int imageWidth;

    public static final int TYPE_STATUS = 1;
    public static final int TYPE_MATCH = 2;
    public static final int TYPE_NONE = 3;

    private int showType;
    private boolean showActivityLabel;

    public EventsAdapter(ArrayList<DataEvent> data, Context con, int type, boolean showActivityLabel) {
        mData = data;
        this.mContext = con;
        inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        showType = type;
        this.showActivityLabel = showActivityLabel;

        // Width of screen minus left and right padding
        imageWidth = con.getResources().getDisplayMetrics().widthPixels - con.getResources().getDimensionPixelSize(R.dimen.margin_s);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getFinalView(int i, View view, ViewGroup viewGroup) {

        DataEvent event = getItem(i);

        if (view == null) {
            view = inflater.inflate(R.layout.view_event, viewGroup, false);
            RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rlEventHolder);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rl.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            rl.setLayoutParams(params);

            ImageView imagePlaceHolder = (ImageView) view.findViewById(R.id.view_event_image_placeholder);
            imagePlaceHolder.setBackgroundDrawable(DataManager.getInstance().getEventDrawable());
        }

        TextView title = (TextView) view.findViewById(R.id.view_event_text_title);
        TextView subTitle = (TextView) view.findViewById(R.id.view_event_text_sub_title);
        TextView participants = (TextView) view.findViewById(R.id.view_event_text_participants);
        TextView match = (TextView) view.findViewById(R.id.view_event_text_match);
        BaseIconTextView matchIcon = (BaseIconTextView) view.findViewById(R.id.view_event_tex);
        ImageView image = (ImageView) view.findViewById(R.id.view_event_image);

        //activity title
        try {
            String activityTitle = DataStore.getInstance().getApplicationData().getActivityTitleByEventId(event.client_id);
            RelativeLayout rlActivityTitleHolder = (RelativeLayout) view.findViewById(R.id.rlActivityLabelHolder);
            rlActivityTitleHolder.setVisibility(showActivityLabel ? View.VISIBLE : View.GONE);
            TextView tvActivityLabel = (TextView) view.findViewById(R.id.tvActivityLabel);
            tvActivityLabel.setText(activityTitle);
            rlActivityTitleHolder.setBackgroundDrawable(ShapeDrawableUtils.getRoundendButton(DataStore.getInstance().getIntColor(EMColor.WHITE), 0));
            rlActivityTitleHolder.setVisibility(showActivityLabel ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            EMLog.e(TAG, ex.getMessage());
        }


        if (event != null && event.dataPublicEvent != null) {
            title.setText(event.dataPublicEvent.event_title);
            subTitle.setText(Utils.getEventDate(event.dataPublicEvent.schedule.from));

            int participantsResId;

            // Unlimited
            if (event.dataPublicEvent.spots == -1) {
                participants.setText(event.dataPublicEvent.user_count + " " + DataManager.getInstance().getResourceText(R.string.Participants));
            } else {
                participants.setText(event.dataPublicEvent.user_count + "/" + event.dataPublicEvent.spots + " " +
                        DataManager.getInstance().getResourceText(R.string.Participants));
            }

            switch (showType) {
                case TYPE_MATCH:
                    match.setText(event.dataPublicEvent.match + "%");
                    break;

                case TYPE_STATUS:
                    try {
                        match.setText(event.dataPublicEvent.user_event_status.status.toString().toUpperCase());
                    } catch (Exception ex) {
                        EMLog.e(TAG, ex.getMessage());
                    }
                    break;
                case TYPE_NONE:
                    match.setText("");
                    break;
            }
            //aviram asked to hide match percentage anyway
            // matchIcon.setVisibility(showType == TYPE_MATCH ? View.VISIBLE : View.INVISIBLE);
            matchIcon.setVisibility(View.INVISIBLE);


            try {
                Picasso.with(mContext).load(Utils.getImageUrl(
                        event.dataPublicEvent.image_url, imageWidth, image.getLayoutParams().height, "crop")).into(image);
            } catch (Exception ex) {
                image.setImageDrawable(null);
            }
        }
        return view;
    }

    @Override
    public boolean filterObject(DataEvent dataEvent, String constraint) {
        return dataEvent.dataPublicEvent.event_title.toLowerCase().contains(constraint);
    }
}
