package cz.mpelant.fitchecker.service;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Request for some update on subject(s)
 * Created by David Bilik[david.bilik@ackee.cz] on 16. 5. 2014.
 */
public class SubjectRequest {
    protected static final String TAG = SubjectRequest.class.getName();
    private static final String URI = "uri";
    private static final String NOTIF = "notif";
    @NonNull
    public Uri mUri;
    public boolean showNotifications;


    public SubjectRequest(@NonNull Uri uri) {
        this(uri, false);
    }

    public SubjectRequest(@NonNull Uri uri, boolean showNotifications) {
        mUri = uri;
        this.showNotifications = showNotifications;
    }

    SubjectRequest(Intent intent) {
        showNotifications = intent.getBooleanExtra(NOTIF, false);
        mUri = intent.getParcelableExtra(URI);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubjectRequest that = (SubjectRequest) o;
        return showNotifications == that.showNotifications && mUri.equals(that.mUri);

    }

    @Override
    public int hashCode() {
        int result = mUri.hashCode();
        result = 31 * result + (showNotifications ? 1 : 0);
        return result;
    }


    void applyToIntent(Intent intent) {
        intent.putExtra(URI, mUri);
        intent.putExtra(NOTIF, showNotifications);
    }


}
