package cz.mpelant.fitchecker.utils;

import android.os.Handler;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * MainThreadBus.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.utils
 * @since 4/17/2014
 */
public class MainThreadBus extends Bus {
    private Handler mHandler;

    public MainThreadBus() {
        init();
    }

    public MainThreadBus(String identifier) {
        super(identifier);
        init();
    }

    public MainThreadBus(ThreadEnforcer enforcer) {
        super(enforcer);
        init();
    }

    public MainThreadBus(ThreadEnforcer enforcer, String identifier) {
        super(enforcer, identifier);
        init();
    }

    private void init() {
        mHandler = new Handler();
    }


    public void postOnMainThread(final Object event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                post(event);
            }
        });

    }
}
