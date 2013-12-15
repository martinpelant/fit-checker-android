package com.actionbarsherlock;

/**
 *
 * BuildConfig.java
 * fix for proguard error
 * https://github.com/JakeWharton/ActionBarSherlock/issues/1001
 *
 * @author eMan s.r.o.
 * @project Imes
 * @package com.actionbarsherlock
 * @since 10/26/13
 */
public class BuildConfig {
    public static final boolean DEBUG = cz.mpelant.fitchecker.BuildConfig.DEBUG;
}
