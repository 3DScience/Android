package com.laoschool.screen;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.laoschool.R;
import com.laoschool.adapter.RecylerViewScreenExamResultsAdapter;
import com.laoschool.adapter.RecylerViewScreenExamResultsStudentTabAdapter;
import com.laoschool.shared.LaoSchoolShared;
import com.laoschool.view.FragmentLifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenExamResults extends Fragment implements FragmentLifecycle {


    private static final String TAG = "ScreenExamResults";
    private ScreenExamResults screenExamResults;
    private int containerId;
    private String data;
    private String currentRole;
    private Context context;

    public ScreenExamResults() {
        // Required empty public constructor
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public interface IScreenExamResults {
        void sendData(String message);

        void gotoScreenMarkScoreStudentFromExamResults(String student);
    }

    public IScreenExamResults iScreenExamResults;

    //
    RelativeLayout mFillerDescription;
    LinearLayout mFilterTerm;

    Spinner cbxTerm;
    Spinner cbxClass;
    Spinner cbxSubject;

    TextView txtTerm;
    TextView txtSubject;
    TextView txtClass;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return _defineScreenExamResultsbyRole(inflater, container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            containerId = getArguments().getInt(LaoSchoolShared.CONTAINER_ID);
            currentRole = getArguments().getString(LaoSchoolShared.ROLE);
            Log.d(getString(R.string.title_screen_exam_results), "-Container Id:" + containerId);
        }
        this.context = getActivity();
        this.screenExamResults = this;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //set display menu item
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        iScreenExamResults = (IScreenExamResults) activity;
    }

    public static Fragment instantiate(int containerId, String currentRole) {
        ScreenExamResults fragment = new ScreenExamResults();
        Bundle args = new Bundle();
        args.putInt(LaoSchoolShared.CONTAINER_ID, containerId);
        args.putString(LaoSchoolShared.ROLE, currentRole);
        fragment.setArguments(args);
        return fragment;
    }

    private View _defineScreenExamResultsbyRole(LayoutInflater inflater, ViewGroup container) {
        if (currentRole.equals(LaoSchoolShared.ROLE_TEARCHER)) {
            return _defineScreenExamResultsTeacher(inflater, container);
        } else {
            return _defineScreenExamResultsDetailForStudent(inflater, container);
        }

    }

    private View _defineScreenExamResultsDetailForStudent(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.screen_exam_results_student, container, false);
        ViewPager mViewPageScreenExamResultsStudent = (ViewPager) view.findViewById(R.id.mViewPageScreenExamResultsStudent);
        //
        //ScorePageAdapter scorePageAdapter = new ScorePageAdapter(context, Arrays.asList("Term 1", "Term 2", "Total"));

        ExamScorePagerAdapter sampleFragmentPagerAdapter = new ExamScorePagerAdapter(getFragmentManager(), Arrays.asList("Term 1", "Term 2", "Total"));
        mViewPageScreenExamResultsStudent.setAdapter(sampleFragmentPagerAdapter);

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(mViewPageScreenExamResultsStudent);

        return view;
    }

    private View _defineScreenExamResultsTeacher(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.screen_exam_results_tearcher, container, false);
        view.findViewById(R.id.btnGotoMarkScore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = LaoSchoolShared.makeFragmentTag(containerId, LaoSchoolShared.POSITION_SCREEN_MARK_SCORE_STUDENT_11);
                Log.d(getString(R.string.title_screen_exam_results), tag);
                setData("Hello " + getString(R.string.title_screen_mark_score_student));
                iScreenExamResults.sendData("Hello " + getString(R.string.title_screen_mark_score_student));
            }
        });

        //Define item
        mFillerDescription = (RelativeLayout) view.findViewById(R.id.mShowFitler);
        mFilterTerm = (LinearLayout) view.findViewById(R.id.mFitlerTerm);

        cbxTerm = (Spinner) view.findViewById(R.id.cbxTerm);
        cbxClass = (Spinner) view.findViewById(R.id.cbxClass);
        cbxSubject = (Spinner) view.findViewById(R.id.cbxSubject);

        Button btnFilterSubmit = (Button) view.findViewById(R.id.btnFilterSubmit);
        SearchView searchStudent = (SearchView) view.findViewById(R.id.mSearchExamResults);
        final LinearLayout mSearch = (LinearLayout) view.findViewById(R.id.mSearch);

        txtTerm = (TextView) view.findViewById(R.id.txtTermScreenExamResults);
        txtSubject = (TextView) view.findViewById(R.id.txtSubjectScreenExamResults);
        txtClass = (TextView) view.findViewById(R.id.txtClassScreenExamResults);

        //
        searchStudent.setIconifiedByDefault(false);
        searchStudent.setIconified(false);
        searchStudent.clearFocus();
        searchStudent.setQueryHint(getString(R.string.hint_search_exam_resutls));


        //set search plate color..
        ViewGroup search_plate = (ViewGroup) searchStudent.findViewById(R.id.search_plate);
        if (search_plate != null) {
            search_plate.setBackgroundColor(Color.parseColor("#ebebeb"));
        }

        //
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mRecylerViewExamResults);

        //


        // Creating adapter for spinner Term
        final List<String> terms = Arrays.asList(getResources().getStringArray(R.array.termTest));
        _fillDataForSpinerFilter(cbxTerm, terms);
        cbxTerm.setSelection(1);

        //Handler on item Selected
        cbxTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    List<String> classTest = Arrays.asList(getResources().getStringArray(R.array.classTest));
                    _fillDataForSpinerFilter(cbxClass, classTest);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cbxClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    List<String> classTest = Arrays.asList(getResources().getStringArray(R.array.subjectTest));
                    _fillDataForSpinerFilter(cbxSubject, classTest);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final boolean[] flagSummit = {false};
        //Handler on submit filter
        btnFilterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _term = cbxTerm.getSelectedItem().toString();
                if (!_term.equals(getString(R.string.selected))) {
                    String _class = cbxClass.getSelectedItem().toString();
                    if (!_class.equals(getString(R.string.selected))) {
                        String _subject = cbxSubject.getSelectedItem().toString();
                        if (!_subject.equals(getString(R.string.selected))) {
                            flagSummit[0] = true;
                            Toast.makeText(context, "Term:" + _term + "-Class:" + _class + "-Subject:" + _subject, Toast.LENGTH_SHORT).show();
                            txtTerm.setText(_term);
                            txtClass.setText(_class);
                            txtSubject.setText(_subject);
                            _fillDataForListResultFilter(recyclerView);
                            mSearch.setVisibility(View.VISIBLE);
                            _showFilterDescription();
                        } else {
                            flagSummit[0] = false;
                            cbxSubject.performClick();
                        }
                    } else {
                        flagSummit[0] = false;
                        cbxClass.performClick();
                    }
                } else {
                    //Show dropdown view
                    flagSummit[0] = false;
                    cbxTerm.performClick();
                }
            }
        });
        View.OnClickListener showHideFilter = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFilterTerm.isShown()) {
                    Log.d(TAG, "Show description");
                    _showFilterDescription();
                } else {
                    Log.d(TAG, "Show filter");
                    _showFilterTerm();
                }
            }
        };
        mFillerDescription.setOnClickListener(showHideFilter);
//        txtTerm.setOnClickListener(showHideFilter);
//        txtClass.setOnClickListener(showHideFilter);
//        txtSubject.setOnClickListener(showHideFilter);
//        mFilterTerm.setOnClickListener(showHideFilter);

        return view;
    }

    public void _showFilterDescription() {
        //
        TranslateAnimation hideFilter = new TranslateAnimation(0, 0, 0, mFilterTerm.getHeight());
        hideFilter.setDuration(200);
        hideFilter.setFillAfter(true);
        TranslateAnimation showFilterDescription = new TranslateAnimation(0, 0, mFillerDescription.getHeight(), 0);
        showFilterDescription.setDuration(200);
        showFilterDescription.setFillAfter(true);

        //mFilterTerm.startAnimation(hideFilter);
        mFilterTerm.setVisibility(View.GONE);

        //mFillerDescription.startAnimation(showFilterDescription);
        mFillerDescription.setVisibility(View.VISIBLE);
        ///
        //
        txtTerm.setVisibility(View.VISIBLE);
        txtClass.setVisibility(View.VISIBLE);
        txtSubject.setVisibility(View.VISIBLE);

        cbxClass.setVisibility(View.GONE);
        cbxSubject.setVisibility(View.GONE);
        cbxTerm.setVisibility(View.GONE);
    }

    public void _showFilterTerm() {

        ///
//        TranslateAnimation showFilterDescription = new TranslateAnimation(0, 0, 0, mFillerDescription.getHeight());
//        showFilterDescription.setDuration(200);
//        showFilterDescription.setFillAfter(true);
//        mFillerDescription.startAnimation(showFilterDescription);


        //
//        TranslateAnimation hideFilter = new TranslateAnimation(0, 0, 0, mFilterTerm.getHeight());
//        hideFilter.setDuration(200);
//        hideFilter.setFillAfter(true);
//        mFilterTerm.startAnimation(hideFilter);
        mFilterTerm.setVisibility(View.VISIBLE);
        mFillerDescription.setVisibility(View.GONE);

        txtTerm.setVisibility(View.GONE);
        txtClass.setVisibility(View.GONE);
        txtSubject.setVisibility(View.GONE);

        cbxClass.setVisibility(View.VISIBLE);
        cbxSubject.setVisibility(View.VISIBLE);
        cbxTerm.setVisibility(View.VISIBLE);
    }

    private void _fillDataForListResultFilter(RecyclerView recyclerView) {
        //init adapter
        final List<String> strings = new ArrayList<String>();
        strings.add(context.getString(R.string.row_sub_header));
        strings.addAll(Arrays.asList(getResources().getStringArray(R.array.listTeacher)));
        RecylerViewScreenExamResultsAdapter adapter = new RecylerViewScreenExamResultsAdapter(this, strings);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);

        //set adapter
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void _fillDataForSpinerFilter(Spinner cbx, List<String> classTest) {
        ArrayAdapter<String> dataAdapterclassTest = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, classTest);
        // Drop down layout style - list view with radio button
        dataAdapterclassTest.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        cbx.setAdapter(dataAdapterclassTest);
    }


    public class ExamScorePagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private List<String> tabTitles;

        public ExamScorePagerAdapter(FragmentManager fm, List<String> tabTitles) {
            super(fm);
            this.tabTitles = tabTitles;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return ScoreFragment.newInstance(position + 1);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles.get(position);
        }
    }

    public static class ScoreFragment extends Fragment {
        public static final String ARG_PAGE = "ARG_PAGE";

        private int mPage;
        ScoreFragment fragment;
        private Context context;

        public static ScoreFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt(ARG_PAGE, page);
            ScoreFragment fragment = new ScoreFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPage = getArguments().getInt(ARG_PAGE);
            this.fragment = this;
            this.context = getActivity();
        }

        // Inflate the fragment layout we defined above for this fragment
        // Set the associated text for the title
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.view_exam_resluts_student_tab, container, false);
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerViewExamResultsStudentTab);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1);
            recyclerView.setLayoutManager(gridLayoutManager);

            RecylerViewScreenExamResultsStudentTabAdapter studentTabAdapter = new RecylerViewScreenExamResultsStudentTabAdapter(fragment, Arrays.asList(getResources().getStringArray(R.array.subjects)));
            if (mPage == 0) {

            } else if (mPage == 1) {
                studentTabAdapter = new RecylerViewScreenExamResultsStudentTabAdapter(fragment, Arrays.asList(getResources().getStringArray(R.array.subjects1)));
            } else if (mPage == 2) {
                studentTabAdapter = new RecylerViewScreenExamResultsStudentTabAdapter(fragment, Arrays.asList(getResources().getStringArray(R.array.subjects2)));
            }
            recyclerView.setAdapter(studentTabAdapter);

            return view;
        }
    }

}
