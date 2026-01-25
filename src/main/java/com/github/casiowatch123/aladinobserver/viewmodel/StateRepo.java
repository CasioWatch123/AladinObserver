package com.github.casiowatch123.aladinobserver.viewmodel;

import com.github.casiowatch123.aladinobserver.viewmodel.impl.Observable;

public final class StateRepo {
    private final Observable<Boolean> notifierEnabled = new Observable<>(true);
    
    public Observable<Boolean> notifierEnabled() {
        return notifierEnabled;
    }
    public void enableNotification() {
        notifierEnabled.set(true);
    }
    public void disableNotification() {
        notifierEnabled.set(false);
    }
    public StateRepo() {}
}
