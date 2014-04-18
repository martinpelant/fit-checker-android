package cz.mpelant.fitchecker.fragment.dialog;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * BaseDialogFragment.java
 *
 * @author eMan s.r.o.
 * @project chlist-an
 * @package cz.eman.chlist.fragment.dialog
 * @since 4/10/2014
 */
public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        }catch (IllegalStateException e){
            e.printStackTrace();
        }


    }
}
