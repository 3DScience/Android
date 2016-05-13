package com.laoschool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.laoschool.R;
import com.laoschool.entities.Attendance;
import com.laoschool.screen.ScreenAttended;

import java.util.List;

/**
 * Created by Tran An on 5/12/2016.
 */
public class ListAttendancesAdapter extends RecyclerView.Adapter<ListAttendancesAdapter.ViewHolder> {

    private List<ScreenAttended.GroupAttendance> mDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAttendancesAdapter(List<ScreenAttended.GroupAttendance> myDataset, Context context) {
        mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListAttendancesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                   .inflate(R.layout.row_attendance, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        View v = holder.mView;
        final ImageView btnDropDown = (ImageView) v.findViewById(R.id.btnDropdown);
        TextView txbAttDt = (TextView) v.findViewById(R.id.txbAttDt);
        TextView txvAbsent = (TextView) v.findViewById(R.id.txvAbsent);
        TextView txbAbsent = (TextView) v.findViewById(R.id.txbAbsent);
        TextView txvExcused = (TextView) v.findViewById(R.id.txvExcused);
        TextView txbExcused = (TextView) v.findViewById(R.id.txbExcused);
        RelativeLayout tableResult = (RelativeLayout) v.findViewById(R.id.tableResult);
        RelativeLayout groupAttendancesView = (RelativeLayout) v.findViewById(R.id.groupAttendancesView);
        final LinearLayout sessionView = (LinearLayout) v.findViewById(R.id.sessionView);

        int color = Color.parseColor("#808080"); //The color u want
        btnDropDown.setColorFilter(color);

        final ScreenAttended.GroupAttendance groupAttendance = mDataset.get(position);

        txbAttDt.setText(groupAttendance.getAtt_dt());
        if(groupAttendance.getAttendances().size() == 1) {
            btnDropDown.setImageResource(R.drawable.ic_info_black_24dp);
            txbAbsent.setText("Full days");
            if(groupAttendance.getAttendances().get(0).getExcused() == 1)
                txbExcused.setText("Yes");
            else
                txbExcused.setText("No");
            txbAbsent.setTextSize(12);
            txbExcused.setTextSize(12);
        } else {
            btnDropDown.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            txbAbsent.setText(groupAttendance.getAttendances().size()+ "");
            int totalExcused = 0;
            for(Attendance attendance: groupAttendance.getAttendances()) {
                if(attendance.getExcused() == 1)
                    totalExcused++;
            }
            txbExcused.setText(totalExcused+ "");
        }

        if(groupAttendance.getAttendances().size() > 1) {
            for(Attendance attendance: groupAttendance.getAttendances()) {
                LinearLayout relativeLayout = new LinearLayout(context);
                relativeLayout.setBackgroundColor(Color.parseColor("#E0E0E0"));
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 120);
                relativeLayout.setLayoutParams(layoutParams);

                TextView sessionName = new TextView(context);
                sessionName.setTextColor(Color.parseColor("#2962FF"));
                sessionName.setPadding(40, 40, 20, 0);
                TextView splash = new TextView(context);
                splash.setText(" / ");
                TextView reason = new TextView(context);
                reason.setPadding(20, 0, 0, 0);

                sessionName.setText(attendance.getSession() + " - " + attendance.getSubject());
                if(attendance.getExcused() == 1)
                    reason.setText(attendance.getNotice());
                else {
                    reason.setText("No reason");
                    reason.setTextColor(Color.parseColor("#D32F2F"));
                }

                relativeLayout.addView(sessionName);
                relativeLayout.addView(splash);
                relativeLayout.addView(reason);

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                linearLayout.setBackgroundColor(Color.parseColor("#9E9E9E"));

                sessionView.addView(relativeLayout);
                sessionView.addView(linearLayout);
            }
        }

        groupAttendancesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(groupAttendance.getAttendances().size() == 1) {

                } else {
                    if(sessionView.getVisibility() == View.GONE) {
                        sessionView.setVisibility(View.VISIBLE);
                        btnDropDown.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    }
                    else {
                        sessionView.setVisibility(View.GONE);
                        btnDropDown.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    }
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
