package com.everymatch.saas.ui.event;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.everymatch.saas.R;
import com.everymatch.saas.adapter.EventsAdapter;
import com.everymatch.saas.client.data.EventType;
import com.everymatch.saas.server.Data.DataEventHolder;
import com.everymatch.saas.server.ServerConnector;
import com.everymatch.saas.server.requests.RequestEvents;
import com.everymatch.saas.server.responses.BaseResponse;
import com.everymatch.saas.server.responses.ErrorResponse;
import com.everymatch.saas.server.responses.ResponseEvents;
import com.everymatch.saas.singeltones.Consts;
import com.everymatch.saas.singeltones.PusherManager;
import com.everymatch.saas.ui.base.BaseEventListFragment;
import com.everymatch.saas.ui.dialog.EventTypeSelectionDialog;
import com.everymatch.saas.ui.dialog.menus.MenuCreateEvent;
import com.everymatch.saas.ui.dialog.menus.MenuMyEvents;
import com.everymatch.saas.ui.discover.DiscoverActivity;
import com.everymatch.saas.ui.questionnaire.QuestionnaireActivity;
import com.everymatch.saas.util.EMLog;
import com.everymatch.saas.util.EmptyViewFactory;
import com.everymatch.saas.view.EventHeader;

import java.io.Serializable;
import java.util.Map;


/**
 * My events
 */
public class MyEventsFragment extends BaseEventListFragment implements EmptyViewFactory.ButtonListener {

    public static final String TAG = MyEventsFragment.class.getSimpleName();
    private static final int CODE_EVENT_SELECTION = 1212;
    public static final String ARG_OPEN_CREATE_EVENT_MENU = "arg.open.create.event.menu";

    //Views
    private TextView mTextEventType;
    private EventTypeSelectionDialog mDialog;
    private EventHeader mHeader;

    //Data
    boolean mOpenCreateEventMenu;
    private boolean mHasEventsAtAll; // Indicates that all the users in all the hashmaps are empty

    public static MyEventsFragment getInstance() {
        MyEventsFragment fragment = new MyEventsFragment();
        return fragment;
    }

    public static MyEventsFragment getInstance(boolean openMenu) {
        MyEventsFragment fragment = new MyEventsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(ARG_OPEN_CREATE_EVENT_MENU, true);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventMap = ds.getUser().getAllEvents();
        mCurrentEventKey = getCurrentEventKey();
        if (getArguments() != null && getArguments().containsKey(ARG_OPEN_CREATE_EVENT_MENU))
            mOpenCreateEventMenu = true;
    }

    /**
     * Iterate through the event map the comes from server
     * The priority is:
     * UPCOMING
     * HOSTING
     * any thing that contains events
     */
    private String getCurrentEventKey() {
        String answer = "";

        if (mEventMap.containsKey(EventType.UPCOMING) && mEventMap.get(EventType.UPCOMING).count > 0) {
            answer = EventType.UPCOMING;
            mHasEventsAtAll = true;
        }

        if (answer == null && mEventMap.containsKey(EventType.HOSTING) &&
                mEventMap.get(EventType.HOSTING).count > 0) {
            answer = EventType.HOSTING;
            mHasEventsAtAll = true;
        }

        if (answer == null) {
            for (Map.Entry<String, DataEventHolder> entry : mEventMap.entrySet()) {
                if (entry.getValue().count > 0) {
                    answer = entry.getKey();
                    mHasEventsAtAll = true;
                    break;
                }
            }
        }

        if (!mHasEventsAtAll) {
            answer = EventType.UPCOMING; // Set default to upcoming
        }

        return answer;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((DiscoverActivity) getActivity()).setSelectedMenuItem(DiscoverActivity.DISCOVER_MENU_ITEMS.EVENTS);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        setTypeSelectionView(inflater);
        mAbsListView.setPadding(mAbsListView.getPaddingLeft(), 0, mAbsListView.getPaddingRight(), mAbsListView.getPaddingBottom());

        // If no events at all - show the general empty view
        if (!mHasEventsAtAll) {
            setGeneralEmptyView(inflater);
            mTopContainer.setVisibility(View.GONE);
        } else {
            setEmptyView(inflater);
        }

        //check if we came from discover emptyView
        if (mOpenCreateEventMenu)
            onOneIconClicked();
    }

    @Override
    protected void setHeader() {
        super.setHeader();
        mEventHeader.getIconThree().setVisibility(View.GONE);
        mEventHeader.setVisibility(View.GONE);

        //get header from discover activity
        mHeader = ((DiscoverActivity) getActivity()).getmHeader();
        mHeader.setListener(this);
        mHeader.getBackButton().setVisibility(View.GONE);
        mHeader.getIconOne().setText(Consts.Icons.icon_CreateEvent);
        mHeader.getIconOne().setVisibility(View.VISIBLE);
        mHeader.getIconTwo().setVisibility(View.GONE);
        mHeader.getIconThree().setVisibility(View.GONE);
        mHeader.setTitle(dm.getResourceText(R.string.Events));
        mHeader.getTitle().setOnClickListener(null);
        mHeader.setArrowDownVisibility(false);

        //initSearch();
    }

    private void initSearch() {
        mEventHeader.getEditTitle().addTextChangedListener(this);
    }

    /**
     * Set empty view
     */
    private void setEmptyView(LayoutInflater inflater) {
        View emptyView = inflater.inflate(R.layout.view_no_events_from_type, mAbsListView, false);
        ViewGroup viewGroup = (ViewGroup) mAbsListView.getParent();
        viewGroup.addView(emptyView);
        mAbsListView.setEmptyView(emptyView);
    }

    /**
     * Set general empty view, if we have no events att all
     */
    private void setGeneralEmptyView(LayoutInflater inflater) {
        View emptyView = EmptyViewFactory.createEmptyView(EmptyViewFactory.TYPE_EVENTS, this);
        ViewGroup viewGroup = (ViewGroup) mAbsListView.getParent();
        viewGroup.addView(emptyView);
        mAbsListView.setEmptyView(emptyView);
    }

    /**
     * set selection view and prepare the arrays
     *
     * @param inflater
     */
    private void setTypeSelectionView(LayoutInflater inflater) {

        // selection title
        inflater.inflate(R.layout.view_list_header_text, mTopContainer, true);
        mTextEventType = (TextView) mTopContainer.findViewById(R.id.view_list_header_text);
        mTopContainer.findViewById(R.id.view_list_header_text_icon).setVisibility(View.VISIBLE);

        mTextEventType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* mDialog = EventTypeSelectionDialog.create(mEventMap, mCurrentEventKey, Utils.dpToPx(3),
                        mEventHeader.getMeasuredHeight() + mTopContainer.getMeasuredHeight(), Utils.dpToPx(220));
                mDialog.setTargetFragment(MyEventsFragment.this, CODE_EVENT_SELECTION);
                mDialog.show(getFragmentManager(), EventTypeSelectionDialog.class.getSimpleName());*/

                MenuMyEvents menuMyEvents = new MenuMyEvents();
                menuMyEvents.setTargetFragment(MyEventsFragment.this, MenuMyEvents.REQUEST_CODE_MY_EVENTS);
                menuMyEvents.show(getChildFragmentManager(), "");
            }
        });

        // Set the first value
        setEventTypeTitle(mEventMap.get(mCurrentEventKey));
    }

    @Override
    public EventsAdapter createAdapter() {
        return new EventsAdapter(mEventMap.get(mCurrentEventKey).getEvents(), getActivity(), EventsAdapter.TYPE_STATUS,true);
    }

    @Override
    protected void fetchNextPage() {

        final String requestedEventType = mCurrentEventKey;

        RequestEvents requestEvents = new RequestEvents(mEventMap.get(mCurrentEventKey).filter, mAdapter.getCount(), PAGE_COUNT);
        ServerConnector.getInstance().processRequest(requestEvents, new ServerConnector.OnResultListener() {
            @Override
            public void onSuccess(BaseResponse baseResponse) {
                EMLog.i(TAG, "onSuccess");

                ResponseEvents responseEvents = (ResponseEvents) baseResponse;

                if (responseEvents == null) {
                    setNoMoreResults();
                } else {
                    addEventsToAdapter(requestedEventType, responseEvents.getEvents());
                }

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(ErrorResponse errorResponse) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onOneIconClicked() {
        MenuCreateEvent menuCreateEvent = MenuCreateEvent.getInstance(mEventHeader.getMeasuredHeight());
        menuCreateEvent.setTargetFragment(MyEventsFragment.this, MenuCreateEvent.REQUEST_CODE_CREATE_EVENT);
        menuCreateEvent.show(getChildFragmentManager(), "");
    }

    @Override
    public void onTwoIconClicked() {
        onSearchIconClick();
    }

    @Override
    public void onThreeIconClicked() {
    }

    private void setEventTypeTitle(DataEventHolder dataEventHolder) {
        String text = String.format(Consts.EVENT_TYPE_FORMAT, dataEventHolder.text_title, dataEventHolder.getEvents().size());
        mTextEventType.setText(text);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_EVENT_SELECTION && resultCode == Activity.RESULT_OK) {
            mDialog.dismiss();

            String key = data.getStringExtra(EventTypeSelectionDialog.EXTRA_RESULT);

            if (key == null || mCurrentEventKey.equals(key)) {
                return;
            }
            mCurrentEventKey = key;
            EMLog.i(TAG, "Selected event type - " + mCurrentEventKey);
            setEventTypeTitle(mEventMap.get(mCurrentEventKey));
            mAdapter.refreshData(mEventMap.get(mCurrentEventKey).getEvents());

            // Initialize pagination data for the next type
            mIsNoMoreResults = false;
        } else if (requestCode == MenuCreateEvent.REQUEST_CODE_CREATE_EVENT && resultCode == Activity.RESULT_OK) {
            int activityId = data.getIntExtra(QuestionnaireActivity.EXTRA_ACTIVITY_ID, 0);
            String eventId = data.getStringExtra(QuestionnaireActivity.EXTRA_SELECTED_EVENT_ID);
            QuestionnaireActivity.createEvent(getActivity(), activityId, eventId);
        } else if (requestCode == MenuMyEvents.REQUEST_CODE_MY_EVENTS) {
            DataEventHolder eventHolder = (DataEventHolder) data.getSerializableExtra(MenuMyEvents.EXTRA_EVENT_HOLDER);
            mCurrentEventKey = eventHolder.filter;
            EMLog.i(TAG, "Selected event type - " + mCurrentEventKey);
            setEventTypeTitle(mEventMap.get(mCurrentEventKey));
            mAdapter.refreshData(mEventMap.get(mCurrentEventKey).getEvents());

            // Initialize pagination data for the next type
            mIsNoMoreResults = false;
        }
    }

    @Override
    protected void handleBroadcast(Serializable eventObject, String eventName) {
        // super.handleBroadcast(eventObject, eventName);
        if (PusherManager.PUSHER_EVENT_MY_EVENT_UPDATE.equals(eventName)) {

            //check if event
            if (mCurrentEventKey != getCurrentEventKey()) {
                mCurrentEventKey = getCurrentEventKey();
                setEventTypeTitle(mEventMap.get(mCurrentEventKey));
            }

            mAdapter.refreshData(mEventMap.get(mCurrentEventKey).getEvents());

            if (ds.getUser().getTotalEventsCount() > 0) {
                mTopContainer.setVisibility(View.VISIBLE);
                setEventTypeTitle(mEventMap.get(mCurrentEventKey));
            }

        }
    }

    @Override
    protected boolean shouldFetchMoreData() {

        if (!mIsSearching && mEventMap.get(mCurrentEventKey).count > mAdapter.getCount()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onEmptyViewFirstButtonClick() {
        ((DiscoverActivity) getActivity()).dmiDiscover.performClick();
    }

    @Override
    public void onEmptyViewSecondButtonClick() {
        //createEvent();

        //simulate create event click
        onOneIconClicked();
    }
}
