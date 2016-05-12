package com.laoschool.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.laoschool.R;
import com.laoschool.entities.ExamResult;
import com.laoschool.listener.OnLoadMoreListener;
import com.laoschool.screen.ScreenExamResults;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Hue on 3/11/2016.
 */
public class ExamResultsStudentSemesterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ExamResultsStudentSemesterAdapter.class.getSimpleName();
    private Fragment screen;
    private Context context;
    private List<ExamResult> examResults;
    private List<String> subjects = new ArrayList<>();
    private List<Integer> subjectIds = new ArrayList<>();

    private int TYPE_SUB_HEADER = 0;
    private int TYPE_TITLE = 1;
    private int TYPE_LINE = 2;
    Map<Integer, Map<Integer, ArrayList<String>>> listMap;

    public ExamResultsStudentSemesterAdapter(Fragment screen, List<ExamResult> examResults) {
        this.screen = screen;
        this.context = screen.getActivity();
        if (examResults != null) {
            this.examResults = examResults;
            HashMap<Integer, String> hashSubject = new LinkedHashMap<Integer, String>();
            listMap = new HashMap<>();
            for (ExamResult examResult : examResults) {
                hashSubject.put(examResult.getSubject_id(), examResult.getSubject());
            }
            Map<Integer, String> treeSubject = new TreeMap<>(hashSubject);
            for (Integer subId : treeSubject.keySet()) {
                //defile subject list
                subjects.add(treeSubject.get(subId));
                subjectIds.add(subId);


                Map<Integer, ArrayList<String>> scoresByMonthList = new HashMap<>();
                ArrayList<String> end_semester = new ArrayList<>();
                for (int i = 0; i < examResults.size(); i++) {
                    ExamResult examResult = examResults.get(i);
                    int exam_month = examResult.getExam_month();
                    int exam_type = examResult.getExam_type();
                    Log.d(TAG, " - exam_month:" + exam_month + ", exam_type:" + exam_type);
                    String score = String.valueOf(examResult.getIresult());
                    if (examResult.getSubject_id() == subId) {

                        ArrayList tempList = null;
                        if (scoresByMonthList.containsKey(exam_month)) {
                            tempList = scoresByMonthList.get(exam_month);
                            if (tempList == null)
                                tempList = new ArrayList();
                            if (exam_type == 1)
                                tempList.add(score);
                            else if (exam_type == 2) {
                                end_semester.add(score);
                            }
                        } else {
                            tempList = new ArrayList();
                            if (exam_type == 1)
                                tempList.add(score);
                            else if (exam_type == 2) {
                                end_semester.add(score);
                            }
                        }
                        scoresByMonthList.put(exam_month, tempList);
                    }
                }
                scoresByMonthList.put(100, end_semester);

                listMap.put(subId, scoresByMonthList);

            }
        } else {
            this.examResults = new ArrayList<>();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_LINE)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_line, parent, false); //Inflating the layout
        else if (viewType == TYPE_TITLE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_exam_results_student, parent, false); //Inflating the layout
        } else if (viewType == TYPE_SUB_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_only_title, parent, false); //Inflating the layout
        }
        ExamResultsStudentSemesterAdapterViewHolder viewHolder = new ExamResultsStudentSemesterAdapterViewHolder(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof ExamResultsStudentSemesterAdapterViewHolder) {
                ExamResultsStudentSemesterAdapterViewHolder semesterHolder = (ExamResultsStudentSemesterAdapterViewHolder) holder;
                View view = semesterHolder.view;
                if (semesterHolder.viewType == TYPE_TITLE) {
                    final String title = subjects.get(position);
                    Map<Integer, ArrayList<String>> scores = listMap.get(subjectIds.get(position));
                    Map<Integer, ArrayList<String>> treeScores = new TreeMap<>(scores);
//                //Define and set data
                    TextView txtSubjectScreenResultsStudent = (TextView) view.findViewById(R.id.txtSubjectScreenResultsStudent);
                    RecyclerView mListScoreBySemester = (RecyclerView) view.findViewById(R.id.mListScoreBySemester);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    mListScoreBySemester.setLayoutManager(linearLayoutManager);

                    txtSubjectScreenResultsStudent.setText(title);

                    ScoreStudentSemesterAdapter scoreStudentSemesterAdapter = new ScoreStudentSemesterAdapter(context, treeScores);
                    mListScoreBySemester.setAdapter(scoreStudentSemesterAdapter);
                } else {

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder() - exception messages:" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    @Override
    public int getItemViewType(int position) {
//        String item = strings.get(position);
//        if (item.equals(context.getString(R.string.row_sub_header)))
//            return TYPE_SUB_HEADER;
//        else if (!item.equals(context.getString(R.string.row_line)))
        return TYPE_TITLE;
//        else
//            return TYPE_LINE;

    }

    public class ExamResultsStudentSemesterAdapterViewHolder extends RecyclerView.ViewHolder {
        View view;
        int viewType;

        public ExamResultsStudentSemesterAdapterViewHolder(View itemView, int viewType) {
            super(itemView);
            this.view = itemView;
            this.viewType = viewType;
        }
    }
}

