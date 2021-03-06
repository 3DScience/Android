package com.laoschool.screen;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.laoschool.LaoSchoolSingleton;
import com.laoschool.R;
import com.laoschool.adapter.ListNotificationAdapter;
import com.laoschool.entities.Message;
import com.laoschool.listener.OnLoadMoreListener;
import com.laoschool.model.AsyncCallback;
import com.laoschool.model.DataAccessInterface;
import com.laoschool.model.sqlite.DataAccessNotification;
import com.laoschool.shared.LaoSchoolShared;
import com.laoschool.view.FragmentLifecycle;
import com.laoschool.view.ViewpagerDisableSwipeLeft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.laoschool.screen.ScreenAnnouncements.NotificationList.refeshNotificationPagerInbox;
import static com.laoschool.screen.ScreenAnnouncements.NotificationList.refeshNotificationPagerUnread;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenAnnouncements extends Fragment implements FragmentLifecycle {
    private static final String TAG = ScreenAnnouncements.class.getSimpleName();
    private static final int TAB_INBOX_0 = 0;
    private static final int TAB_UNREAD_1 = 1;
    private static ScreenAnnouncements thiz;
    private static FragmentManager fr;
    private int containerId;
    private String currentRole;
    private static Context context;

    private static DataAccessInterface service;
    private static DataAccessNotification accessNotification;
    private Message notification;
    boolean alreadyExecuted = false;

    //Item for student
    private RecyclerView mRecylerViewNotificationStudent;

    //Item for teacher
    static ViewpagerDisableSwipeLeft viewPage;
    static PagerSlidingTabStrip tabs;

    static NotificationPagerAdapter notificationPagerAdapter;

    static NotificationList currentPage;

    private static LinearLayout mAnnouncement;
    private static ProgressBar mProgressBar;
    private static View mError;
    private static View mNoData;
    private View mExpanSearch;
    private MenuItem itemSearch;
    private View mBacgroundSearch;
    private RecyclerView mSearchResults;
    private SearchView mSearch;
    private boolean onSearch = false;
    private FirebaseAnalytics mFirebaseAnalytics;


    public Message getNotification() {
        return notification;
    }

    public void setNotification(Message message) {
        this.notification = message;
    }


    public interface IScreenAnnouncements {

        void gotoScreenAnnouncementDetails(Message notificaiton);

        void _gotoCreateAnnouncement();

        void logoutApplication();

    }

    public IScreenAnnouncements iScreenAnnouncements;


    public static ScreenAnnouncements instantiate(int containerId, String currentRole) {
        Log.d(TAG, "instantiate()");
        ScreenAnnouncements fragment = new ScreenAnnouncements();
        Bundle args = new Bundle();
        args.putInt(LaoSchoolShared.CONTAINER_ID, containerId);
        args.putString(LaoSchoolShared.CURRENT_ROLE, currentRole);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenAnnouncements() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            containerId = getArguments().getInt(LaoSchoolShared.CONTAINER_ID);
            currentRole = getArguments().getString(LaoSchoolShared.CURRENT_ROLE);
            if (currentRole != null) {
                Log.d(TAG, "-Container Id:" + containerId + ",currentRole:" + currentRole);
            } else {
                Log.d(TAG, "-Container Id:" + containerId + ",currentRole null");
            }
        }
        this.thiz = this;
        this.context = getActivity();
        this.service = LaoSchoolSingleton.getInstance().getDataAccessService();
        this.fr = getFragmentManager();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(TAG, bundle);


    }

    private void _defineData() {
        Log.d(TAG, "_defineData()");
        _showProgressLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int userId = LaoSchoolShared.myProfile.getId();
                int countLocal = accessNotification.getNotificationCountForUserId(userId);
                Log.d(TAG, "_defineData():getNotificationCountForUserId(" + userId + ")=" + countLocal);
                if (countLocal > 0) {
                    getNewNotification();
                    //getDataFormLocal();
                } else {
                    getDataFormServer();
                }
                _handlerPageChange();
                alreadyExecuted = true;

            }
        }, LaoSchoolShared.LOADING_TIME);
        onErrorNodata();
    }

    private void getNewNotification() {
        final int form_id = DataAccessNotification.getMaxNotificationID(LaoSchoolShared.myProfile.getId());
        service.getNotification(form_id, new AsyncCallback<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> result) {
                try {
                    if (result != null) {
                        int resultsSize = result.size();
                        int end = 0;
                        for (int i = 0; i < resultsSize; i++) {
                            Message message = result.get(i);
                            accessNotification.addOrUpdateNotification(message);
                            end = i;
                        }
                        if (end == (resultsSize - 1)) {
                            _initPageData();
                        } else {
                            getDataFormLocal();
                        }
                        _showProgressLoading(false);
                    } else {
                        showNodata();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getNewNotification()/getNotification(" + form_id + ") Exception=" + e.getMessage());
                    showError();
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "getDataFormServer()/getNotification(" + form_id + ") onFailure=" + message);
                showError();
            }

            @Override
            public void onAuthFail(String message) {
                LaoSchoolShared.goBackToLoginPage(context);
            }
        });
    }

    private void onErrorNodata() {
        View.OnClickListener reloadDataClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _showProgressLoading(true);
                getDataFormServer();
            }
        };
        mNoData.setOnClickListener(reloadDataClick);
        mError.setOnClickListener(reloadDataClick);
    }

    public static void getDataFormServer() {
        try {
            service.getNotification(new AsyncCallback<List<Message>>() {
                @Override
                public void onSuccess(final List<Message> result) {
                    try {
                        if (result != null) {
                            for (Message message : result) {
                                accessNotification.addOrUpdateNotification(message);
                            }
                            getDataFormLocal(-1);
                        } else {
                            showNodata();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getDataFormServer()/getNotification() Exception=" + e.getMessage());
                        showError();

                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "getDataFormServer()/getNotification() onFailure=" + message);
                    showError();
                }

                @Override
                public void onAuthFail(String message) {
                    LaoSchoolShared.goBackToLoginPage(context);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError();
        }
    }

    private static void showNodata() {
        mNoData.setVisibility(View.VISIBLE);
        mError.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mAnnouncement.setVisibility(View.GONE);
    }

    private static void showError() {
        mError.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mAnnouncement.setVisibility(View.GONE);
        mNoData.setVisibility(View.GONE);

    }

    private static void _showProgressLoading(boolean b) {
        if (b) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAnnouncement.setVisibility(View.GONE);
        } else {
            mAnnouncement.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
        mError.setVisibility(View.GONE);
        mNoData.setVisibility(View.GONE);
    }


    public static void getDataFormServer(final int form_id, final int position) {
        service.getNotification(form_id, new AsyncCallback<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> result) {
                try {
                    if (result != null) {
                        int resultsSize = result.size();
                        int end = 0;
                        for (int i = 0; i < resultsSize; i++) {
                            Message message = result.get(i);
                            accessNotification.addOrUpdateNotification(message);
                            end = i;
                        }
                        if (end == (resultsSize - 1)) {
                            reloadDataFormLocal();
                        }
                    } else {
                        showNodata();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getDataFormServer()/getNotification(" + form_id + ") Exception=" + e.getMessage());
                    showError();
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "getDataFormServer()/getNotification(" + form_id + ") onFailure=" + message);
                showError();
            }

            @Override
            public void onAuthFail(String message) {
                LaoSchoolShared.goBackToLoginPage(context);
            }
        });
    }

    private static void getDataFormLocal() {
        getDataFormLocal(-1);
    }

    private static void getDataFormLocal(int position) {
        try {
            _initPageData();
            if (position > -1) {
                viewPage.setCurrentItem(position);
            }
            _showProgressLoading(false);
        } catch (Exception e) {
            Log.e(TAG, "getDataFormLocal() Exception = " + e.getMessage());
        }
    }

    private static void _initPageData() {
        List<Message> notificationForUserInbox = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, 1);
        List<Message> notificationForUserUnread = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, 0);

        notificationPagerAdapter = new NotificationPagerAdapter(fr, notificationForUserInbox, notificationForUserUnread);

        viewPage.setAdapter(notificationPagerAdapter);

        tabs.setViewPager(viewPage);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()/getUserVisibleHint():" + getUserVisibleHint());
        if (currentRole == null)
            return inflater.inflate(R.layout.screen_error_application, container, false);
        else {
            // Inflate the layout for this fragment
            //  View view = inflater.inflate(R.layout.screen_announcements_student, container, false);
//            if (currentRole.equals(LaoSchoolShared.ROLE_STUDENT))
//                return _defineStudentView(view);
//            else {
            //}
            return _defineTeacherView(inflater, container);
        }

    }

    private View _defineTeacherView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.screen_announcements, container, false);
        mAnnouncement = (LinearLayout) view.findViewById(R.id.mAnnouncement);

        //error,nodata progress view
        mError = view.findViewById(R.id.mError);
        mNoData = view.findViewById(R.id.mNoData);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgress);

        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        viewPage = (ViewpagerDisableSwipeLeft) view.findViewById(R.id.notificationViewPage);
        viewPage.setAllowedSwipeDirection(HomeActivity.SwipeDirection.all);

        //init seach
        mExpanSearch = view.findViewById(R.id.mExpanSearch);
        mBacgroundSearch = view.findViewById(R.id.mBacgroundSearch);
        mSearchResults = (RecyclerView) view.findViewById(R.id.mSearchResults);
        mSearchResults.setLayoutManager(new LinearLayoutManager(context));

        //
        onExpanSearch();
        onCollapseSearch();

        if (!alreadyExecuted && getUserVisibleHint()) {
            _defineData();
        }

        return view;
    }

    private void onCollapseSearch() {
        mBacgroundSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MenuItemCompat.isActionViewExpanded(itemSearch)) {
                    MenuItemCompat.collapseActionView(itemSearch);
                }
            }
        });
    }

    private void onExpanSearch() {
        mExpanSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuItemCompat.expandActionView(itemSearch);
                itemSearch.setVisible(true);
            }
        });
    }


    private void _handlerPageChange() {
        ViewPager.OnPageChangeListener onPageNotificationChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "_handlerPageChange() onPageSelected() = " + position);
//                List<Message> notificationForUser = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, (position == 0) ? 1 : 0);
//                NotificationList notifragment = ((NotificationPagerAdapter) (viewPage.getAdapter())).getFragment(position);
//                notifragment._setListNotification(notificationForUser, position);
                if (position == TAB_INBOX_0) {
                    refeshNotificationPagerInbox();
                } else if (position == TAB_UNREAD_1) {
                    refeshNotificationPagerUnread();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPage.addOnPageChangeListener(onPageNotificationChangeListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (currentRole != null) {
            inflater.inflate(R.menu.menu_screen_announcement, menu);
            MenuItem itemCreateAnnocement = menu.findItem(R.id.action_create_announcement);
            itemSearch = menu.findItem(R.id.search);
            itemSearch.setVisible(false);
            mSearch = (SearchView) itemSearch.getActionView();
            mSearch.setQueryHint(context.getString(R.string.SCCommon_Search));
            if (currentRole.equals(LaoSchoolShared.ROLE_TEARCHER))
                itemCreateAnnocement.setVisible(true);
            else itemCreateAnnocement.setVisible(false);

            onExpandCollapseSearch();

        }


    }

    private void onExpandCollapseSearch() {
        MenuItemCompat.setOnActionExpandListener(itemSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                itemSearch.setVisible(true);
                viewPage.setVisibility(View.VISIBLE);
                mBacgroundSearch.setVisibility(View.VISIBLE);

                mExpanSearch.setVisibility(View.GONE);
                tabs.setVisibility(View.GONE);

                ((HomeActivity) getActivity()).displaySearch();
                ((HomeActivity) getActivity()).hideBottomBar();

                expanSearch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mAnnouncement.setVisibility(View.VISIBLE);
                tabs.setVisibility(View.VISIBLE);
                viewPage.setVisibility(View.VISIBLE);
                mExpanSearch.setVisibility(View.VISIBLE);
                itemSearch.setVisible(false);
                mBacgroundSearch.setVisibility(View.GONE);
                mSearchResults.setVisibility(View.GONE);

                ((HomeActivity) getActivity()).cancelSearch();
                ((HomeActivity) getActivity()).showBottomBar();
                return true;
            }
        });
    }

    private void expanSearch() {
        final int currentPosition = viewPage.getCurrentItem();
        notificationPagerAdapter.getFragment(currentPosition);
        int isRead = 0;
        if (currentPosition == 0) {
            isRead = 1;
        }

        //search
        final int finalIsRead = isRead;
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    onSearch = true;
                    mAnnouncement.setVisibility(View.GONE);
                    mBacgroundSearch.setVisibility(View.GONE);
                    mSearchResults.setVisibility(View.VISIBLE);
                    List<Message> notificationList = DataAccessNotification.searchNotificationInbox(LaoSchoolShared.myProfile.getId(), finalIsRead, newText);
                    ListNotificationAdapter listNotiAdapter = new ListNotificationAdapter(thiz, mSearchResults, currentPosition, notificationList);
                    mSearchResults.setAdapter(listNotiAdapter);

                    if (newText.length()>5){
                        Bundle bundle = new Bundle();
                        bundle.putString(LaoSchoolShared.FA_QUERY_TEXT,newText);
                        mFirebaseAnalytics.logEvent(TAG, bundle);
                    }
                }
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentRole != null) {
            if (currentRole.equals(LaoSchoolShared.ROLE_TEARCHER)) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_create_announcement:
                        iScreenAnnouncements._gotoCreateAnnouncement();
                        return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        Log.d(TAG, "onResumeFragment()/getUserVisibleHint():" + getUserVisibleHint());
        if (!alreadyExecuted && getUserVisibleHint()) {
            _defineData();
        }
        if (onSearch) {
            MenuItemCompat.collapseActionView(itemSearch);
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        iScreenAnnouncements = (IScreenAnnouncements) context;
    }

    public static class NotificationPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager mFragmentManager;
        private Map<Integer, String> mFragmentTags;
        private List<Message> notificationForUserInbox;
        private List<Message> notificationForUserUnread;


        public NotificationPagerAdapter(FragmentManager fm, List<Message> notificationForUserInbox, List<Message> notificationForUserUnread) {
            super(fm);
            mFragmentManager = fm;
            mFragmentTags = new HashMap<Integer, String>();
            this.notificationForUserInbox = notificationForUserInbox;
            this.notificationForUserUnread = notificationForUserUnread;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return context.getString(R.string.SCCommon_Inbox);
            else
                return context.getString(R.string.SCCommon_Unread);

        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                currentPage = new NotificationList(0, notificationForUserInbox);
                return currentPage;
            } else {
                currentPage = new NotificationList(1, notificationForUserUnread);
                return currentPage;
            }

        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                // record the fragment tag here.
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        public NotificationList getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null)
                return null;
            return (NotificationList) mFragmentManager.findFragmentByTag(tag);
        }
    }

    @SuppressLint("ValidFragment")
    public static class NotificationList extends Fragment {
        private int position;
        private RecyclerView mRecyclerListMessage;
        private Context context;
        SwipeRefreshLayout mSwipeRefreshLayout;
        private List<Message> notificationForUser;
        private View mNoNotification;

        public NotificationList() {

        }

        public NotificationList(int position, List<Message> notificationForUser) {
            this.position = position;
            this.notificationForUser = notificationForUser;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.context = getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.view_notification_pager, container, false);
            mNoNotification = view.findViewById(R.id.mNoNotification);
            mRecyclerListMessage = (RecyclerView) view.findViewById(R.id.mListNotification);
            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            //set adapter
            mRecyclerListMessage.setLayoutManager(linearLayoutManager);
            _setListNotification(notificationForUser, position);
            return view;
        }

        private void _handlerSwipeReload() {
            int positiPage = -1;
            if (viewPage != null) {
                positiPage = viewPage.getCurrentItem();
            }
            final int finalPositiPage = positiPage;
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //get max from id
                    if (mRecyclerListMessage.getAdapter() != null) {
                        try {
                            ListNotificationAdapter listMessageAdapter = (ListNotificationAdapter) mRecyclerListMessage.getAdapter();
                            int form_id = listMessageAdapter.getNotificationList().get(0).getId();
                            refeshNotification(form_id, finalPositiPage, false);
                        } catch (Exception e) {
                            getDataFormServer();
                        }
                    } else {
                        getDataFormServer(DataAccessNotification.getMaxNotificationID(LaoSchoolShared.myProfile.getId()), viewPage.getCurrentItem());
                    }
                    // Refresh items
                    mSwipeRefreshLayout.setRefreshing(false);
                    Log.d(TAG, "onRefesh()");
                }
            });
        }

        private void refeshNotification(final int form_id, final int pos, boolean showloading) {
            _showProgressLoading(showloading);
            if (pos == -1) {
                getDataFormServer();
                return;
            }
            service.getNotification(form_id, new AsyncCallback<List<Message>>() {
                @Override
                public void onSuccess(final List<Message> result) {
                    try {
                        if (result != null) {
                            int resultsSize = result.size();
                            int end = 0;
                            for (int i = 0; i < resultsSize; i++) {
                                Message message = result.get(i);
                                accessNotification.addOrUpdateNotification(message);
                                end = i;
                            }
                            if (end == (resultsSize - 1)) {
                                refeshNotificationPagerInbox();
                                refeshNotificationPagerUnread();
                                if (pos == TAB_INBOX_0) {
                                    viewPage.setCurrentItem(TAB_INBOX_0);
                                } else if (pos == TAB_UNREAD_1) {
                                    viewPage.setCurrentItem(TAB_UNREAD_1);
                                }
                            }
                            _showProgressLoading(false);
                        } else {
                            showNodata();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "refeshNotification()/getNotification(" + form_id + ") Exception=" + e.getMessage());
                        showError();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "getDataFormServer()/getNotification(" + form_id + ") onFailure=" + message);
                    showError();
                }

                @Override
                public void onAuthFail(String message) {
                    LaoSchoolShared.goBackToLoginPage(context);
                }
            });
        }

        public static void refeshNotificationPagerInbox() {
            List<Message> notificationInbox = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, LaoSchoolShared.READ_1);
            NotificationList notiInBox = ((NotificationPagerAdapter) (viewPage.getAdapter())).getFragment(TAB_INBOX_0);
            notiInBox._setListNotification(notificationInbox, TAB_INBOX_0);
        }

        public static void refeshNotificationPagerUnread() {
            List<Message> notificationUnread = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, LaoSchoolShared.UNREAD_0);
            NotificationList notiUnread = ((NotificationPagerAdapter) (viewPage.getAdapter())).getFragment(TAB_UNREAD_1);
            notiUnread._setListNotification(notificationUnread, TAB_UNREAD_1);
        }

        private void _setListNotification(final List<Message> messages, final int position) {
            try {
                _showProgressLoading(false);
                if (messages != null) {
                    if (messages.size() > 0) {
                        mNoNotification.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);

                        final ListNotificationAdapter listMessageAdapter = new ListNotificationAdapter(thiz, mRecyclerListMessage, position, messages);
                        listMessageAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                            @Override
                            public void onLoadMore() {
                                int countMessageFormLocal = accessNotification.getNotificationCount((position == 0) ? 1 : 0);
                                if (messages.size() < countMessageFormLocal) {
                                    Log.d(TAG, "Load more notification !!!");
                                    messages.add(null);
                                    listMessageAdapter.notifyItemInserted(messages.size() - 1);
                                    _loadMoreData(messages, listMessageAdapter, position);
                                } else {
                                    Log.d(TAG, "No notification load !!!");
                                }

                            }
                        });
                        mRecyclerListMessage.setAdapter(listMessageAdapter);
                        _handlerSwipeReload();
                    } else {
                        mNoNotification.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                    }
                } else {
                    mNoNotification.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                }
                mNoNotification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int form_id = DataAccessNotification.getMaxNotificationID(LaoSchoolShared.myProfile.getId());
                        refeshNotification(form_id, viewPage.getCurrentItem(), true);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "NotificationList:_setListNotification():" + e.getMessage());
            }

        }

        private void _loadMoreData(final List<Message> messages, final ListNotificationAdapter notificationAdapter, final int position) {
            //Load more data for reyclerview
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "/_loadMoreData():Load More 2");

                    //Remove loading item
                    messages.remove(messages.size() - 1);
                    notificationAdapter.notifyItemRemoved(messages.size());

                    //Load data

                    List<Message> messagesForUser = new ArrayList<>();
                    if (LaoSchoolShared.myProfile != null) {
                        messagesForUser = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), messages.size() + 30, messages.size(), (position == 0 ? 1 : 0));
                        Log.d(TAG, "NotificationList:getListNotificationForUser size=" + messages.size());
                    }
                    messages.addAll(messagesForUser);


                    notificationAdapter.notifyDataSetChanged();
                    notificationAdapter.setLoaded();
                }
            }, 2000);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (LaoSchoolShared.myProfile == null) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.logoutApplication();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() -isVisibleToUser:" + isVisibleToUser);
    }

    public void reloadAffterCreate() {
        Log.d(TAG, "reloadAffterCreate()");
        _showProgressLoading(true);
        service.getNotification(new AsyncCallback<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> result) {
                try {
                    if (result != null) {
                        int resultsSize = result.size();
                        int end = 0;
                        for (int i = 0; i < resultsSize; i++) {
                            Message message = result.get(i);
                            accessNotification.addOrUpdateNotification(message);
                            end = i;
                        }
                        if (end == (resultsSize - 1)) {
                            reloadDataFormLocal();
                        }
                    } else {
                        showNodata();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "reloadAffterCreate()/getNotification() Exception=" + e.getMessage());
                    showError();

                }
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "reloadAffterCreate()/getNotification() onFailure=" + message);
                showError();
            }

            @Override
            public void onAuthFail(String message) {
                LaoSchoolShared.goBackToLoginPage(context);
            }
        });
    }

    public static void reloadDataFormLocal() {
        //int position = viewPage.getCurrentItem();
        //Log.d(TAG, "reloadDataFormLocal() position = " + position);
        if (viewPage.getAdapter() != null) {
            List<Message> notificationInbox = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, LaoSchoolShared.READ_1);
            NotificationList notiInBox = ((NotificationPagerAdapter) (viewPage.getAdapter())).getFragment(TAB_INBOX_0);
            notiInBox._setListNotification(notificationInbox, TAB_INBOX_0);

            //
            List<Message> notificationUnread = accessNotification.getListNotificationForUser(Message.MessageColumns.COLUMN_NAME_TO_USR_ID, LaoSchoolShared.myProfile.getId(), 30, 0, LaoSchoolShared.UNREAD_0);
            NotificationList notiUnread = ((NotificationPagerAdapter) (viewPage.getAdapter())).getFragment(TAB_UNREAD_1);
            notiUnread._setListNotification(notificationUnread, TAB_UNREAD_1);
        } else {
            _showProgressLoading(false);
            _initPageData();
        }
    }
}
