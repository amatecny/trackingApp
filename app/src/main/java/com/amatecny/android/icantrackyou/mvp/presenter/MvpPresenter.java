package com.amatecny.android.icantrackyou.mvp.presenter;


import com.amatecny.android.icantrackyou.mvp.view.MvpView;

/**
 * Presenter to be used with {@link MvpView}.
 * <p>
 * Created by amatecny on 17/01/2017
 */
public interface MvpPresenter<V extends MvpView> {

    V getView();

    void setView( V view );

    /**
     * Called when view hierarchy is ready
     */
    void viewCreated();

    /**
     * Called when view hierarchy is no longer present, no calls to view should be done after this and prior {@link #viewCreated()}
     */
    void viewDestroyed();

    /**
     * Called when view is visible to user, usable for e.g. broadcasts
     */
    void viewResumed();

    /**
     * Called when view is not fully visible to user, usable for e.g. broadcasts
     */
    void viewPaused();

}
