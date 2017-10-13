package com.amatecny.android.icantrackyou.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.amatecny.android.icantrackyou.mvp.presenter.BaseMvpPresenter;
import com.amatecny.android.icantrackyou.mvp.presenter.MvpPresenter;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * MVP based implementation of android.support.v4.app.Fragment.
 * It handles creating and releasing of presenter as well as provides context for non-context based classes
 *
 * @see BaseMvpPresenter for basic presenter implementation
 * Created by amatecny on 31/01/2017.
 */
public abstract class MvpFragment<P extends MvpPresenter> extends DaggerFragment implements MvpView {

    @Inject
    protected P presenter;
    //for rxBinding purposes
    protected CompositeDisposable activeDisposables = new CompositeDisposable();

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onViewCreated( View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );

        presenter.viewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.viewResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.viewPaused();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.viewDestroyed();
        disposeAll();
    }

    private void disposeAll() {
        activeDisposables.clear();
    }

    /**
     * Add a {@link Disposable} that can be disposed at a later time to a collection of disposables.
     */
    protected void storeDisposable( Disposable disposable ) {
        activeDisposables.add( disposable );
    }

    /**
     * Seamlessly modifies the upstream observable to store {@link Disposable} after onSubscribe.
     *
     * @return upstream {@link Observable} which will store it's disposable for their later auto disposal
     */
    protected <T> ObservableTransformer<T, T> storeDisposable() {
        return upstream -> upstream.doOnSubscribe( this::storeDisposable );
    }
}
