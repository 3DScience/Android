package com.laoschool.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.laoschool.R;
import com.laoschool.entities.ExamResult;
import com.laoschool.shared.LaoSchoolShared;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Hue on 5/9/2016.
 */
public class ScoreStudentSemesterAdapter extends RecyclerView.Adapter<ScoreStudentSemesterAdapter.ScoreStudentSemesterAdapterViewHolder> {
    private static final String TAG = ScoreStudentSemesterAdapter.class.getSimpleName();
    Context context;
    Map<Integer, ArrayList<ExamResult>> scores;
    List<Integer> months = new ArrayList<>();

    public ScoreStudentSemesterAdapter(Context context, Map<Integer, ArrayList<ExamResult>> scores) {
        this.context = context;
        this.scores = scores;
        for (Integer key : scores.keySet()) {
            Log.d(TAG, " -month:" + key);
            months.add(key);
        }

        Log.d(TAG, " -exam size:" + scores.size());
    }


    @Override
    public ScoreStudentSemesterAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_score_by_month, parent, false); //Inflating the layout
        ScoreStudentSemesterAdapterViewHolder scoreStudentSemesterAdapterViewHolder = new ScoreStudentSemesterAdapterViewHolder(view, viewType);
        return scoreStudentSemesterAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(ScoreStudentSemesterAdapterViewHolder holder, int position) {
        View view = holder.view;
        try {
            int month = months.get(position);
            String monthStr = _getMonthString(month);
//            if (month < 100) {
//                monthStr =;
//            } else if (month == 100) {
//                monthStr = "Final";
//            }
            List<ExamResult> scoreList = scores.get(month);
            if (scoreList.size() > 0) {
                final ExamResult examResult = scoreList.get(scoreList.size() - 1);
                String score = examResult.getSresult();
                if (!score.trim().isEmpty()) {
                    ((TextView) (view.findViewById(R.id.lbScoreMonth))).setText(monthStr);
                    ((TextView) (view.findViewById(R.id.lbScore))).setText(score);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            _showDetailsExamResults(examResult);

                        }
                    });
                } else {
                    Log.e(TAG, "onBindViewHolder() - score is empty");
                    view.setVisibility(View.GONE);
                    //notifyItemRemoved(position);
                }
            } else {
                Log.e(TAG, "onBindViewHolder() - month:" + month + " list exam is empty");
                view.setVisibility(View.GONE);
//                ((TextView) (view.findViewById(R.id.lbScoreMonth))).setText("");
//                ((TextView) (view.findViewById(R.id.lbScore))).setText("");
                // notifyItemRemoved(position);
            }
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder() - exception=" + e.getMessage());
        }
    }

    private void _showDetailsExamResults(ExamResult examResult) {
        AlertDialog.Builder bDetails = new AlertDialog.Builder(context);
        View examResultDetails = View.inflate(context, R.layout.view_exam_results_details, null);
        ((TextView) examResultDetails.findViewById(R.id.lbExamSubject)).setText(String.valueOf(examResult.getSubjectName()));
        if (examResult.getExam_type() == 2)
            ((TextView) examResultDetails.findViewById(R.id.lbExamDate)).setText(examResult.getTermName());
        else {
            ((TextView) examResultDetails.findViewById(R.id.lbExamDate)).setText(String.valueOf(examResult.getExam_month() + "/" + examResult.getExam_year()));
        }
        String score = examResult.getSresult();
        ((TextView) examResultDetails.findViewById(R.id.lbExamScore)).setText(String.valueOf(score));
        ((TextView) examResultDetails.findViewById(R.id.lbExamTecherName)).setText(String.valueOf(examResult.getTeacherName()));
        ((TextView) examResultDetails.findViewById(R.id.lbExamDateUpdateScore)).setText(" - " + LaoSchoolShared.formatDate(examResult.getExam_dt(), 2));
        ((TextView) examResultDetails.findViewById(R.id.lbExamNotice)).setText(String.valueOf(examResult.getNotice()));
        bDetails.setCustomTitle(examResultDetails);
        final Dialog dialog = bDetails.create();
        ((TextView) examResultDetails.findViewById(R.id.lbExamClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private String _getMonthString(int month) {
        if (month == 100)
            return "Final";
        DateFormat inputFormatter1 = new SimpleDateFormat("MMM", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.set(2016, month - 1, 10);
        String monthParse = inputFormatter1.format(cal.getTime());
        Log.d(TAG, " _getMonthString() - monnt:" + month + ",month parse:" + monthParse);
        return monthParse;
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    public class ScoreStudentSemesterAdapterViewHolder extends RecyclerView.ViewHolder {
        View view;
        int viewType;

        public ScoreStudentSemesterAdapterViewHolder(View itemView, int viewType) {
            super(itemView);
            this.view = itemView;
            this.viewType = viewType;
        }
    }
}