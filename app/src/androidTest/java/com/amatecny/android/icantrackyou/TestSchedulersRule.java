package com.amatecny.android.icantrackyou;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

/**
 * Makes all the RxJava schedulers execute on the test scheduler.
 * <p>
 * Created by amatecny on 09/07/2017, (c) 2017 Blackboard Inc.
 */

public class TestSchedulersRule implements TestRule {

    private final TestScheduler testScheduler = new TestScheduler();

    public TestScheduler getTestScheduler() {
        return testScheduler;
    }

    @Override
    public Statement apply( final Statement base, Description description ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                RxJavaPlugins.setIoSchedulerHandler( scheduler -> testScheduler);
                RxJavaPlugins.setComputationSchedulerHandler( scheduler -> testScheduler );
                RxJavaPlugins.setNewThreadSchedulerHandler( scheduler -> testScheduler );
                try {
                    base.evaluate();
                } finally {
                    RxJavaPlugins.reset();
                }
            }
        };
    }
}
