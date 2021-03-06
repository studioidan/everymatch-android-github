package com.everymatch.saas.server.Data;

import java.io.Serializable;

/**
 * Created by Dacid on 30/06/2015.
 */
public class DataActivity implements Serializable {
    public int _id;
    public boolean is_default;
    public String locked_by;
    public String version;
    public DataDate created_date;
    public String created_by;
    public DataDate updated_date;
    public int distance_value;
    public String updated_by;
    public String text_title;
    public DataIcon icon;
    public String status;
    public DataQuestion questions[];
    public DataLanguage languages[];

    public DataEvent_Activity[] getEvents() {
        if (events == null)
            events = new DataEvent_Activity[0];
        return events;
    }

    //public DataEvent events[];
    private DataEvent_Activity events[];
    public DataGroup groups[];
    public DataUrl background;
    public String client_id;

    public String text_welcome;
    public String image_url;
    public String text_description;

    public class DataUrl implements Serializable {
        public String url;
    }

    public DataEvent_Activity getEvent_activityById(String id) {
        for (DataEvent_Activity event : getEvents()) {
            if (event.event_id.equals(id)) {
                return event;
            }
        }
        return null;
    }
}


