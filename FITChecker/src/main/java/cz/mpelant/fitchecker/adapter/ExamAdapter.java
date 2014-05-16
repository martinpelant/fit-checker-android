package cz.mpelant.fitchecker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.model.Exam;
import cz.mpelant.fitchecker.model.Subject;

/**
 * SubjectAdapter.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.adapter
 * @since 4/17/2014
 */
public class ExamAdapter extends CursorAdapter {

    public ExamAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_exam, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Exam exam = new Exam(cursor);
        TextView date = ButterKnife.findById(view, R.id.txtDate);
        TextView room = ButterKnife.findById(view, R.id.txtRoom);
        TextView state = ButterKnife.findById(view, R.id.txtState);

        ImageView attending = ButterKnife.findById(view, R.id.imgAttending);
        attending.setVisibility(exam.isRegistered() ? View.VISIBLE : View.GONE);
        date.setText(exam.getFormattedDate());
        String roomText = exam.getRoom();
        if (TextUtils.isEmpty(roomText)) {
            roomText = context.getString(R.string.notSpecified);
        }
        room.setText(context.getString(R.string.room) + ": " + roomText);
        state.setText(exam.getOccupied() + "/" + exam.getCapacity());
        view.setTag(exam);

        if (exam.getOccupied() >= exam.getCapacity() && exam.getCapacity() != 0) {
            state.setTextColor(Color.RED);
        } else {
            if (exam.getCapacity() == 0) {
                state.setTextColor(Color.GRAY);
            } else {
                state.setTextColor(Color.parseColor("#76d91c"));
            }
        }

        long now = System.currentTimeMillis();
        if (now > exam.getLongDate()) {
            view.setBackgroundResource(R.drawable.bgr_shade);
        } else {
            view.setBackgroundDrawable(null);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
