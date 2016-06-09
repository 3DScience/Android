package com.laoschool.screen;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.laoschool.LaoSchoolSingleton;
import com.laoschool.R;
import com.laoschool.adapter.InputExamResultsAdapter;
import com.laoschool.entities.ExamResult;
import com.laoschool.entities.Master;
import com.laoschool.model.AsyncCallback;
import com.laoschool.shared.LaoSchoolShared;
import com.laoschool.view.FragmentLifecycle;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenInputExamResultsStudent extends Fragment implements FragmentLifecycle {


    private static final String TAG = ScreenInputExamResultsStudent.class.getSimpleName();
    private Context context;
    private int containerId;
    private TextView lbTermName;

    private View mSelectedSubject;
    private TextView lbSubjectSeleted;

    private Dialog dialogSelectedSubject;
    private View mToolBox;
    private ProgressDialog progressDialog;
    private List<String> exam_months;
    private List<Long> exam_dates;
    private TextView lbInputDate;
    private Dialog dialogSelectedInputTypeDate;
    private Long selectedDateInputExamResult;
    private RecyclerView listExamByStudent;
    InputExamResultsAdapter resultsforClassbySubjectAdapter;
    private TextView lbInputSubmit;
    private View btnCancelInputExamResult;
    private View btnSubmitInputExamResult;
    private int selectedSubjectId;
    Map<Integer, ExamResult> groupStudentMap;
    private View mContainer;
    private TextView lbClassName;
    private SearchView mSearchStudentName;

    interface IScreenInputExamResults {
        void cancelInputExamResults();
    }

    public IScreenInputExamResults iScreenInputExamResults;

    public ScreenInputExamResultsStudent() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = getActivity();
        if (getArguments() != null) {
            containerId = getArguments().getInt(LaoSchoolShared.CONTAINER_ID);
            Log.d(TAG, "-Container Id:" + containerId);
        }
        progressDialog = new ProgressDialog(context);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.screen_input_exam_results_student, container, false);
        HomeActivity activity = (HomeActivity) getActivity();
        activity.hideBottomBar();

        mContainer = view.findViewById(R.id.mContainer);


        lbTermName = (TextView) view.findViewById(R.id.lbClassAndTermName);
        lbClassName = (TextView) view.findViewById(R.id.lbClassName);

        mSelectedSubject = view.findViewById(R.id.mSelectedSubject);
        lbSubjectSeleted = (TextView) mSelectedSubject.findViewById(R.id.lbSubjectSeleted);

        //
        mSearchStudentName = (SearchView) view.findViewById(R.id.mSearchStudentName);
        SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) mSearchStudentName.findViewById(R.id.search_src_text);
        autoCompleteTextView.setTextColor(getResources().getColor(android.R.color.white));
        //Custom search
        // Hide icon search in searchView and set clear text icon
        ImageView search_close_btn = (ImageView) mSearchStudentName.findViewById(R.id.search_close_btn);
        if (search_close_btn != null) {
            search_close_btn.setImageDrawable(LaoSchoolShared.getDraweble(context, R.drawable.ic_close_black_24dp));
        }
        ImageView magImage = (ImageView) mSearchStudentName.findViewById(R.id.search_mag_icon);
        if (magImage != null) {
            magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            magImage.setVisibility(View.GONE);
        }
        //define toolbox
        mToolBox = view.findViewById(R.id.mToolBox);
        lbInputDate = (TextView) mToolBox.findViewById(R.id.lbInputDate);

        listExamByStudent = (RecyclerView) view.findViewById(R.id.listExamByStudent);
        listExamByStudent.setHasFixedSize(true);

        listExamByStudent.setLayoutManager(new LinearLayoutManager(context));

        lbInputSubmit = (TextView) view.findViewById(R.id.lbInputSubmit);

        btnCancelInputExamResult = view.findViewById(R.id.btnCancelInputExamResult);
        btnSubmitInputExamResult = view.findViewById(R.id.btnSubmitInputExamResult);

        submitInputExamResults();
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_screen_mark_score_student, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_done_mark_score:
                iScreenInputExamResults.cancelInputExamResults();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
        try {
            String className = LaoSchoolShared.myProfile.getEclass().getTitle();
            String termName = String.valueOf("Term " + LaoSchoolShared.myProfile.getEclass().getTerm());
            String year = String.valueOf(LaoSchoolShared.myProfile.getEclass().getYears());
            String tag = LaoSchoolShared.makeFragmentTag(containerId, LaoSchoolShared.POSITION_SCREEN_EXAM_RESULTS_2);
            ScreenExamResults screenExamResults = (ScreenExamResults) getFragmentManager().findFragmentByTag(tag);
            fillDataSubject(screenExamResults.listSubject, screenExamResults.selectedSubjectId);
            Log.d(TAG, "-screenExamResults.selectedSubjectId:" + screenExamResults.selectedSubjectId);
            lbTermName.setText(termName + " / " + year);
            lbClassName.setText(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillDataSubject(List<Master> listSubject, int selectedSubjectId) {
        final List<String> subjectNames = new ArrayList<>();
        Map<Integer, String> subs = new HashMap<>();
        for (Master master : listSubject) {
            subjectNames.add(master.getSval());
            subs.put(master.getId(), master.getSval());
        }
        //First load
        this.selectedSubjectId = selectedSubjectId;
        lbSubjectSeleted.setText(subs.get(selectedSubjectId));
        getExamResultsbySubjectId(true, selectedSubjectId);

        dialogSelectedSubject = makeDialogSelectdSubject(listSubject, subjectNames);
        mSelectedSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFocus();
                dialogSelectedSubject.show();
            }
        });
    }


    private Dialog makeDialogSelectdSubject(final List<Master> result, final List<String> subjectNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_selected_type_input_exam_results);
        View header = View.inflate(context, R.layout.custom_hearder_dialog, null);
        ImageView imgIcon = ((ImageView) header.findViewById(R.id.imgIcon));
        Drawable drawable = LaoSchoolShared.getDraweble(context, R.drawable.ic_library_books_black_24dp);
        int color = Color.parseColor("#ffffff");
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        imgIcon.setImageDrawable(drawable);

        ((TextView) header.findViewById(R.id.txbTitleDialog)).setText(R.string.selected_subject);


        builder.setCustomTitle(header);
        final ListAdapter subjectListAdapter = new ArrayAdapter<String>(context, R.layout.row_selected_subject, subjectNames);

        builder.setAdapter(subjectListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                String subjectName = subjectNames.get(i);
                int subjectId = result.get(i).getId();
                lbSubjectSeleted.setText(subjectName);
                getExamResultsbySubjectId(true, subjectId);
                selectedSubjectId = subjectId;
            }
        });
        final AlertDialog dialog = builder.create();
        ((ImageView) header.findViewById(R.id.imgCloseDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    private void submitInputExamResults() {
        View.OnClickListener actionSubmitInput = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaoSchoolShared.hideSoftKeyboard(getActivity());
                if (getActivity().getCurrentFocus() != null) {
                    getActivity().getCurrentFocus().clearFocus();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.title_msg_comfirm_submit_input_exam_results);
                builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (resultsforClassbySubjectAdapter.getInputExamResults().size() > 0) {
                            Log.d(TAG, "-input type exam size:" + resultsforClassbySubjectAdapter.getInputExamResults().size());
                            List<ExamResult> examResults = resultsforClassbySubjectAdapter.getInputExamResults();
                            inputExamResults(examResults);
                        } else {

                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        };

        View.OnClickListener actionCancelInput = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaoSchoolShared.hideSoftKeyboard(getActivity());
                //Go back
                cancelInputExamResults();
            }
        };

        btnSubmitInputExamResult.setOnClickListener(actionSubmitInput);
        btnCancelInputExamResult.setOnClickListener(actionCancelInput);

    }

    private void inputExamResults(final List<ExamResult> examResults) {
        int monthEx = 0;
        int yearEx = 0;
        int teacherId = LaoSchoolShared.myProfile.getId();
        if (selectedDateInputExamResult != null) {
            monthEx = LaoSchoolShared.getMonth(selectedDateInputExamResult);
            yearEx = 2016;
        }
        Log.d(TAG, "inputExamResults() -month:" + monthEx + ",year:" + yearEx);
        progressDialog.show();
        if (monthEx > 0 && yearEx > 0) {
            final int size = examResults.size();
            for (int i = 0; i < size; i++) {
                final ExamResult examResult = examResults.get(i);
                examResult.setExam_month(monthEx);
                examResult.setTeacher_id(teacherId);
                examResult.setExam_year(yearEx);
                Log.d(TAG, "inputExamResults() -exam:" + examResult.toString());
                callInputExamResults(size, i, examResult);
            }
        } else {
            Log.d(TAG, "inputExamResults() -parse date error.");
        }

    }

    private void callInputExamResults(final int size, final int index, ExamResult examResult) {
        Log.d(TAG, "callInputExamResults().onSuccess() -input: " + examResult.toJson());
        if (index == (size - 1)) {
            Log.d(TAG, "callInputExamResults().onSuccess() -input " + size + " ok");
            finishInputExamResults();
            progressDialog.dismiss();
        }
//        LaoSchoolSingleton.getInstance().getDataAccessService().inputExamResults(examResult, new AsyncCallback<ExamResult>() {
//            @Override
//            public void onSuccess(ExamResult result) {
//                Log.d(TAG, "callInputExamResults().onSuccess() -result: " + result.toJson());
//                if (index == (size - 1)) {
//                    Log.d(TAG, "callInputExamResults().onSuccess() -input " + size + " ok");
//                    finishInputExamResults();
//                }
//
//            }
//
//            @Override
//            public void onFailure(String message) {
//                Log.d(TAG, "callInputExamResults().onFailure() -message" + message);
//                progressDialog.dismiss();
//            }
//
//            @Override
//            public void onAuthFail(String message) {
//                Log.d(TAG, "callInputExamResults().onAuthFail() -message" + message);
//                progressDialog.dismiss();
//            }
//        });
    }

    private void finishInputExamResults() {
        resultsforClassbySubjectAdapter.clearData();
        getExamResultsbySubjectId(false, selectedSubjectId);
        Toast.makeText(context, R.string.msg_input_exam_results_successfully, Toast.LENGTH_SHORT).show();

    }

    private void cancelInputExamResults() {
        iScreenInputExamResults.cancelInputExamResults();
    }

    private void getExamResultsbySubjectId(boolean showProgress, final int subjectId) {
        Log.d(TAG, "getExamResultsbySubject()");
        if (showProgress) {
            progressDialog.show();
        }
        final ProgressDialog finalProgressDialog = progressDialog;
        LaoSchoolSingleton.getInstance().getDataAccessService().getExamResultsforMark(LaoSchoolShared.myProfile.getEclass().getId(), -1, subjectId, new AsyncCallback<List<ExamResult>>() {
            @Override
            public void onSuccess(List<ExamResult> result) {
                if (result != null) {
                    //Group data
                    groupStudentMap = groupExamResultbyStudentId(result);
                    Map<Long, String> groupMonth = groupMonth(result);

                    fillDataDateInputExamResults(groupMonth);
                    fillDataExamStudent(groupStudentMap);

                } else {
                    Log.d(TAG, "getExamResultsbySubject().onSuccess() message:NUll");
                }
                if (finalProgressDialog != null)
                    finalProgressDialog.dismiss();
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "getExamResultsbySubject().onFailure() message:" + message);
                if (finalProgressDialog != null)
                    finalProgressDialog.dismiss();
            }

            @Override
            public void onAuthFail(String message) {
                Log.e(TAG, "getExamResultsbySubject().onAuthFail() message:" + message);
                if (finalProgressDialog != null)
                    finalProgressDialog.dismiss();
                LaoSchoolShared.goBackToLoginPage(context);

            }
        });
    }

    private void fillDataDateInputExamResults(Map<Long, String> groupMonth) {
        exam_months = new ArrayList<>();
        exam_dates = new ArrayList<>();
        for (Long month : (new TreeMap<Long, String>(groupMonth)).keySet()) {
            String monthStr = groupMonth.get(month);
            exam_months.add(monthStr);
            exam_dates.add(month);
        }
        if (exam_months.size() > 1) {
            // String monthStr = groupMonth.get(exam_dates.get(exam_dates.size() - 1));
//            exam_months.remove(exam_months.size() - 1);
//            exam_months.add("Test final");
        }
        if (exam_months.size() > 0) {
            //first
            lbInputDate.setText(exam_months.get(0));
            lbInputSubmit.setText(exam_months.get(0));
            selectedDateInputExamResult = exam_dates.get(0);

            //
            dialogSelectedInputTypeDate = makeDialogSelectdInputExamType(exam_months, exam_dates);
            mToolBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearFocus();
                    dialogSelectedInputTypeDate.show();
                }
            });
        }
    }

    private void clearFocus() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
        LaoSchoolShared.hideSoftKeyboard(getActivity());
    }

    private Dialog makeDialogSelectdInputExamType(final List<String> exam_months, final List<Long> exam_dates) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setTitle(R.string.title_selected_type_input_exam_results);
        View header = View.inflate(context, R.layout.custom_hearder_dialog, null);
        ImageView imgIcon = ((ImageView) header.findViewById(R.id.imgIcon));
        imgIcon.setImageDrawable(LaoSchoolShared.getDraweble(context, R.drawable.ic_input_white_24dp));

        ((TextView) header.findViewById(R.id.txbTitleDialog)).setText("Seleted date");

        builder.setCustomTitle(header);
        final ListAdapter subjectListAdapter = new ArrayAdapter<String>(context, R.layout.row_selected_subject, exam_months);

        builder.setAdapter(subjectListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                String selected = exam_months.get(i);
                selectedDateInputExamResult = exam_dates.get(i);
                Log.d(TAG, "-selected month:" + selectedDateInputExamResult);
                lbInputDate.setText(selected);
                lbInputSubmit.setText(selected);
                fillDataExamStudent(groupStudentMap, selectedDateInputExamResult);


            }
        });
        final AlertDialog dialog = builder.create();
        ((ImageView) header.findViewById(R.id.imgCloseDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    private void fillDataExamStudent(Map<Integer, ExamResult> groupStudentMap, Long selectedDateInputExamResult) {
        resultsforClassbySubjectAdapter = new InputExamResultsAdapter(context, groupStudentMap, selectedDateInputExamResult);
        listExamByStudent.setAdapter(resultsforClassbySubjectAdapter);
    }

    private void fillDataExamStudent(Map<Integer, ExamResult> groupStudentMap) {
        resultsforClassbySubjectAdapter = new InputExamResultsAdapter(context, groupStudentMap, selectedDateInputExamResult);
        listExamByStudent.setAdapter(resultsforClassbySubjectAdapter);


    }

    private Map<Long, String> groupMonth(List<ExamResult> result) {
        Map<Long, String> groupMonth = new HashMap<Long, String>();
        for (ExamResult examResult : result) {
            if (examResult.getExam_type() <= 2) {
                int exam_month = examResult.getExam_month();
                groupMonth.put(Long.valueOf(exam_month), getMonthString(exam_month));
            }
        }
        return groupMonth;
    }

    private String getMonthString(int month) {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.UK);
        String monthParse = dateFormatSymbols.getMonths()[month - 1];
        return monthParse;
    }

    private Map<Integer, ExamResult> groupExamResultbyStudentId(List<ExamResult> result) {
        Map<Integer, ExamResult> groupStudentMap = new HashMap<Integer, ExamResult>();
        for (ExamResult examResult : result) {
            if (examResult.getExam_type() <= 2) {
                Integer studentId = examResult.getStudent_id();
                groupStudentMap.put(studentId, examResult);
            }
        }
        return groupStudentMap;
    }


    public static Fragment instantiate(int containerId, String currentRole) {
        ScreenInputExamResultsStudent fragment = new ScreenInputExamResultsStudent();
        Bundle args = new Bundle();
        args.putInt(LaoSchoolShared.CONTAINER_ID, containerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iScreenInputExamResults = (IScreenInputExamResults) context;
    }

}
