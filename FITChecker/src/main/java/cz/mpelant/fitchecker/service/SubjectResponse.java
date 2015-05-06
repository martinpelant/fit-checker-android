package cz.mpelant.fitchecker.service;

import cz.mpelant.fitchecker.model.Subject;

import java.util.HashSet;
import java.util.Set;

/**
 * Response of some subject update operation
 * Created by David Bilik[david.bilik@ackee.cz] on 16. 5. 2014.
 */
public class SubjectResponse {
    protected static final String TAG = SubjectResponse.class.getName();
    private Set<Subject> changedSubjects = new HashSet<>();
    public boolean errorOccured;

    public void setSubjectChanged(Subject subject) {
        getChangedSubjects().add(subject);
    }

    public boolean isChangesDetected() {
        return getChangedSubjects().size() > 0;
    }

    public void setErrorsOccured() {
        errorOccured = true;
    }

    public Set<Subject> getChangedSubjects() {
        return changedSubjects;
    }
}

