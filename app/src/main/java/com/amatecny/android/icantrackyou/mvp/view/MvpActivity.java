package com.amatecny.android.icantrackyou.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.amatecny.android.icantrackyou.mvp.presenter.MvpPresenter;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Base Mvp Activity implementation
 * <p>
 * Lifecycle methods {@link #onResume()}, {@link #onPause()}, {@link #onDestroy()} are called automatically on presenter,
 * but {@link MvpPresenter#viewCreated()} must be called from implementing class once all initialization is done in {@link #onCreate(Bundle)}
 *
 * @see MvpPresenter#viewCreated()
 * @see MvpPresenter#viewResumed()
 * @see MvpPresenter#viewPaused()
 * @see MvpPresenter#viewDestroyed()
 * <p>
 * Created by amatecny on 23-Mar-17.
 */
public abstract class MvpActivity<T extends MvpPresenter> extends DaggerAppCompatActivity implements MvpView {

    @Inject
    protected T presenter;

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.viewResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.viewPaused();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.viewDestroyed();
    }

    @NonNull
    public abstract T getPresenter();
}
