
package cz.mpelant.fitchecker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import cz.mpelant.fitchecker.R;

public class BaseFragmentActivity extends AppCompatActivity {
    public static final String TAG = "BaseFragmentActivity";

    private static final String EXTRA_FRAGMENT_NAME = "fragment";
    private static final String EXTRA_ARGUMENTS = "arguments";
    public static final int CONTENT_VIEW_ID = R.id.baseActivityContent;

    /**
     * gets intent for starting new activity with fragment defined by fragment name and passes extras to the starting intent
     *
     * @param ctx
     * @param fragmentName fragment to instantiate
     * @param args         to pass to the instantiated fragment
     */
    public static Intent generateIntent(Context ctx, String fragmentName, Bundle args) {
        return new Intent(ctx, BaseFragmentActivity.class).putExtra(EXTRA_FRAGMENT_NAME, fragmentName).putExtra(EXTRA_ARGUMENTS, args);
    }

    /**
     * gets intent for starting new activity with fragment defined by fragment name and passes extras to the starting intent
     *
     * @param ctx
     * @param fragmentName fragment to instantiate
     * @param args         to pass to the instantiated fragment
     */
    public static Intent generateIntent(Context ctx, String fragmentName, Bundle args, Class<?> activityClass) {
        return new Intent(ctx, activityClass).putExtra(EXTRA_FRAGMENT_NAME, fragmentName).putExtra(EXTRA_ARGUMENTS, args);
    }

    public static void startActivity(Context ctx, String fragmentName) {
        Intent intent = new Intent(ctx, BaseFragmentActivity.class).putExtra(EXTRA_FRAGMENT_NAME, fragmentName);
        ctx.startActivity(intent);
    }

    /**
     * Start specific activity and open fragment defined by name
     *
     * @param ctx
     * @param fragmentName
     * @param activityClass
     */
    public static void startActivity(Context ctx, String fragmentName, Class<?> activityClass) {
        Intent intent = new Intent(ctx, activityClass).putExtra(EXTRA_FRAGMENT_NAME, fragmentName);
        ctx.startActivity(intent);
    }

    /**
     * Start specific activity and open fragment defined by name
     *
     * @param ctx
     * @param fragmentName
     * @param activityClass
     */
    public static void startActivity(Context ctx, String fragmentName, Class<?> activityClass, Bundle args) {
        Intent intent = new Intent(ctx, activityClass).putExtra(EXTRA_FRAGMENT_NAME, fragmentName).putExtra(EXTRA_ARGUMENTS, args);
        ctx.startActivity(intent);
    }

    /**
     * starts new activity with fragment defined by fragment name and passes extras to the starting intent
     *
     * @param ctx
     * @param fragmentName fragment to instantiate
     * @param args         to pass to the instantiated fragment
     */
    public static void startActivity(Context ctx, String fragmentName, Bundle args) {
        ctx.startActivity(generateIntent(ctx, fragmentName, args));
    }

    /**
     * starts new activity with fragment defined by fragment name and passes extras to the starting intent
     *
     * @param ctx
     * @param fragmentName fragment to instantiate
     * @param extras       to copy to the new intent
     */
    public static void startActivity(Context ctx, String fragmentName, Intent extras) {
        Intent intent = new Intent(ctx, BaseFragmentActivity.class).putExtra(EXTRA_FRAGMENT_NAME, fragmentName).putExtras(extras);
        ctx.startActivity(intent);
    }

    private FrameLayout mContentView;

    public View getContentView() {
        return mContentView;
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(CONTENT_VIEW_ID);
    }


    /**
     * returns the name of the fragment to be instantiated
     *
     * @return
     */

    protected String getFragmentName() {
        return getIntent().getStringExtra(EXTRA_FRAGMENT_NAME);
    }

    /**
     * instantiates the fragment
     *
     * @return
     */
    protected Fragment instantiateFragment(String fragmentName) {
        return Fragment.instantiate(this, fragmentName);
    }

    protected FrameLayout onCreateContentView() {
        return new FrameLayout(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setElevation(0);
        mContentView = onCreateContentView();
        // we need to set SERVER_ID to this container
        mContentView.setId(CONTENT_VIEW_ID);
        setContentViewInternal(mContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        String fragmentName = getFragmentName();
        if (fragmentName == null) {
            finish();
            return;
        }

        Bundle args = getIntent().getBundleExtra(EXTRA_ARGUMENTS);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentName);
        if ((fragment == null) && (savedInstanceState == null)) {
            fragment = instantiateFragment(fragmentName);
            if (args != null) {
                fragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction().add(CONTENT_VIEW_ID, fragment, fragment.getClass().getName()).commit();
        }

    }


    /**
     * replace fragment with a new fragment, add it to the back stack and use fragment name as a
     * transaction tag
     *
     * @param fragment for container to be replaced with
     */
    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, fragment.getClass().getName(), true);
    }

    /**
     * replaces fragment with a new fragment and uses fragment name as a
     * transaction tag
     *
     * @param fragment for container to be replaced with
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        replaceFragment(fragment, fragment.getClass().getName(), addToBackStack);
    }

    public void clearFragmentBackStack() {
        try {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @param fragment       fragment for container to be replaced with
     * @param name           of the transaction, null if not needed
     * @param addToBackStack
     */
    public void replaceFragment(Fragment fragment, String name, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(CONTENT_VIEW_ID, fragment, fragment.getClass().getName());
        if (addToBackStack) {
            transaction.addToBackStack(name);
        }

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commitAllowingStateLoss();
    }

    protected void setContentViewInternal(View view, ViewGroup.LayoutParams params) {
        setContentView(view, params);
    }

}
