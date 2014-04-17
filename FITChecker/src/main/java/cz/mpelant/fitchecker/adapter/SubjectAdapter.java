package cz.mpelant.fitchecker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.model.Subject;

/**
 * SubjectAdapter.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.adapter
 * @since 4/17/2014
 */
public class SubjectAdapter extends CursorAdapter {

    public SubjectAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_subject, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Subject item = new Subject(cursor);
        TextView name = ButterKnife.findById(view, R.id.subject);
        ImageView changed = ButterKnife.findById(view, R.id.changed);

        name.setText(item.getName());
        changed.setVisibility(item.isRead() ? View.INVISIBLE : View.VISIBLE);
        view.setTag(item);
    }
}
