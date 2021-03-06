package com.everymatch.saas.server.Data;

import android.text.TextUtils;

import com.everymatch.saas.server.responses.ResponseEvent;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Dacid on 29/06/2015.
 */
public class DataEvent implements Serializable {
    public String _id;

    @SerializedName("public")
    public DataPublicEvent dataPublicEvent;
    public DataProfile profile;
    private ArrayList<DataEventActions> event_actions;
    private String role_name;
    public String owner_user_id;
    public String activity_client_id;
    public String client_id;
    public String map_image_url;
    private DataDisplaySettings display_settings;
    public DataPrivacySettings privacy_settings;
    public DataEntity entity;


    public DataDisplaySettings getDisplay_settings() {
        if (display_settings == null)
            display_settings = new DataDisplaySettings();
        return display_settings;
    }

    @SerializedName("users")
    private ArrayList<DataPeople> trend_users;

    public ArrayList<DataPeople> getTrend_users() {
        if (trend_users == null)
            trend_users = new ArrayList<>();
        return trend_users;
    }

    public DataEvent() {
    }

    public DataEvent(ResponseEvent responseEvent) {
        this._id = responseEvent.get_id();
        this.dataPublicEvent = responseEvent.getDataPublicEvent();
        this.profile = responseEvent.getProfile();
        this.setEvent_actions(responseEvent.getEvent_actions());
        this.dataPublicEvent.user_event_status = responseEvent.getDataPublicEvent().user_event_status;
        this.activity_client_id = responseEvent.activity_client_id;
        this.client_id = responseEvent.client_id;
        this.display_settings = responseEvent.display_settings;
        this.privacy_settings = responseEvent.privacy_settings;
        this.setRoleName(responseEvent.role_name);
    }

    public String getRole_name() {
        if (role_name == null)
            role_name = "";
        return role_name;
    }

    public void setRoleName(String role_name) {
        this.role_name = role_name;
    }

    public ArrayList<DataEventActions> getEvent_actions() {
        if (event_actions == null)
            event_actions = new ArrayList<>();
        return event_actions;
    }

    public void setEvent_actions(ArrayList<DataEventActions> event_actions) {
        this.event_actions = event_actions;
    }

    public void setEvent(ResponseEvent event) {
        this.dataPublicEvent = event.getDataPublicEvent();
        this.profile = event.getProfile();
        this.event_actions = event.getEvent_actions();
        this.role_name = event.role_name;
        this.owner_user_id = event.owner_user_id;
        this.activity_client_id = event.activity_client_id;
        this.display_settings = event.display_settings;
        this.privacy_settings = event.privacy_settings;
        this.role_name = event.role_name;
        this._id = event._id;
        client_id = event.client_id;
    }

    public boolean showPrecentage() {
        try {
            String mStatus = dataPublicEvent.user_event_status.status;
            if (mStatus.equals("saved") || mStatus.equals("guest"))
                return true;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public String getLocationText() {
        if (TextUtils.isEmpty(dataPublicEvent.getLocation().place_name)) {
            return (dataPublicEvent.getLocation().text_address);
        } else {
            return (dataPublicEvent.getLocation().text_address);
        }
    }
}
