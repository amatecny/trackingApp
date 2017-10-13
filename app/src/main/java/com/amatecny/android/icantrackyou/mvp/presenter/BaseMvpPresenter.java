package com.amatecny.android.icantrackyou.mvp.presenter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.amatecny.android.icantrackyou.mvp.view.MvpView;

import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * This class represents a basic Presenter which provides APIs to store disposables, which are
 * automatically disposed in viewDestroyed(). Used for both Activity and Fragment views.
 * <p>
 * It is necessary to call viewDestroyed() from
 * {@link android.app.Activity#onDestroy()} or {@link Fragment#onDestroyView()}
 * <p>
 * Created by amatecny on 18/01/2017.
 */
public abstract class BaseMvpPresenter<V extends MvpView> implements MvpPresenter<V> {

    /**
     * NB: Strong reference is fine , as gc automatically checks for cyclical dependencies,
     * so unless there is another reference to our fragment/activity both fragment and presenter can be garbage collected
     * <p>
     * Child classes are supposed to access this variable directly to minimize the amount of
     * boilerplate code and improve code readability
     */
    protected V view;
    CompositeDisposable activeDisposables;


    public BaseMvpPresenter( @NonNull V view ) {
        this.view = view;
        activeDisposables = new CompositeDisposable();
    }

    @Override
    public V getView() {
        return view;
    }

    @Override
    public void setView( @NonNull V view ) {
        this.view = view;
    }

    @Override
    public void viewCreated() {
    }

    @Override
    public void viewDestroyed() {
        //when the view is done, halt all the running stuff in presenter, we won't need it anymore
        disposeAll();
    }

    @Override
    public void viewResumed() {
    }

    @Override
    public void viewPaused() {
    }

    /**
     * Add a {@link Disposable} that can be disposed at a later time to a collection of disposables.
     */
    protected void storeDisposable( Disposable disposable ) {
        activeDisposables.add( disposable );
    }

    /**
     * Unregister all subscriptions even before the event chain is finished
     */
    protected void disposeAll() {
        activeDisposables.clear();
    }

    /**
     * Seamlessly modifies the upstream observable to store {@link Disposable} after onSubscribe.
     *
     * @return upstream {@link Observable} which will store it's disposable for their later auto disposal
     */
    protected <T> ObservableTransformer<T, T> storeDisposable() {
        return upstream -> upstream.doOnSubscribe( this::storeDisposable );
    }

    /**
     * Seamlessly modifies the upstream observable to store {@link Completable} after onSubscribe.
     *
     * @return upstream {@link Completable} which will store it's disposable for their later auto disposal
     */
    protected CompletableTransformer storeCompletableDisposable() {
        return upstream -> upstream.doOnSubscribe( this::storeDisposable );
    }

    /**
     * Seamlessly modifies the upstream observable to store {@link Maybe} after onSubscribe.
     *
     * @return upstream {@link Maybe} which will store it's disposable for their later auto disposal
     */
    protected <T> MaybeTransformer<T, T> storeMaybeDisposable() {
        return upstream -> upstream.doOnSubscribe( this::storeDisposable );
    }

    /**
     * Seamlessly modifies the upstream observable to store {@link Single} after onSubscribe.
     *
     * @return upstream {@link Single} which will store it's disposable for their later auto disposal
     */
    protected <T> SingleTransformer<T, T> storeSingleDisposable() {
        return upstream -> upstream.doOnSubscribe( this::storeDisposable );
    }
}
