package cz.mpelant.fitchecker;

import android.app.Application;
import com.squareup.otto.Bus;
import cz.mpelant.fitchecker.utils.MainThreadBus;

/**
 * App.java
 *
 * @project FITChecker
 * @package cz.mpelant.fitchecker
 * @since 4/17/2014
 */
public class App extends Application {
    private static App instance;
    private MainThreadBus mBus;

    @Override
    public void onCreate() {
        instance=this;
        mBus = new MainThreadBus();
        super.onCreate();
    }

    public static App getInstance() {
        return instance;
    }

    public MainThreadBus getBus(){
        return mBus;
    }



}
