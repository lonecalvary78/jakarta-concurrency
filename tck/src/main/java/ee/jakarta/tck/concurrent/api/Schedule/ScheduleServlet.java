/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.concurrent.api.Schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/ScheduleServlet")
public class ScheduleServlet extends TestServlet {
    private static final TestLogger log = TestLogger.get(ScheduleServlet.class);
    private static final long serialVersionUID = 1L;
    private static final long TIMEOUT_S = TestConstants.waitTimeout.toSeconds();

    // -----------------------------------------------------------------------
    // Static coordination state for testScheduleMethodCron
    // Written by ScheduleBean.scheduledEvery3Seconds()
    // -----------------------------------------------------------------------

    /** Reaches zero after 5 firings of the cron-scheduled method. */
    static final CountDownLatch cronRan5TimesLatch = new CountDownLatch(5);

    /** Total invocations of the cron-scheduled method. */
    static final AtomicInteger cronRunCount = new AtomicInteger(0);

    // -----------------------------------------------------------------------
    // Static coordination state for testScheduleMethodEvery6Seconds
    // Written by ScheduleBean.scheduledEvery6Seconds()
    // -----------------------------------------------------------------------

    /** Reaches zero after two firings of the every-6-seconds scheduled method. */
    static final CountDownLatch every6SecondsRanTwiceLatch = new CountDownLatch(2);

    /** Total invocations of the every-6-seconds scheduled method. */
    static final AtomicInteger every6SecondsRunCount = new AtomicInteger(0);

    // -----------------------------------------------------------------------
    // Static coordination state for testScheduleMethodNotConcurrentWithSelf
    // Written by ScheduleBean.scheduledSlowMethod()
    // -----------------------------------------------------------------------

    /**
     * Held open while the test verifies no overlap; released by the test
     * when the slow method is allowed to finish.
     */
    static final CountDownLatch slowMethodFinishLatch = new CountDownLatch(1);

    /** Peak number of concurrent executions recorded inside the slow method. */
    static final AtomicInteger slowMethodMaxConcurrent = new AtomicInteger(0);

    /** Released once the slow scheduled method has started. */
    static final CountDownLatch slowMethodStartedLatch = new CountDownLatch(1);

    // -----------------------------------------------------------------------
    // Static coordination state for testScheduleMethodStopsOnException
    // Written by ScheduleBean.scheduledMethodThatThrows()
    // -----------------------------------------------------------------------

    /** A value is added each time the throwing scheduled method runs. */
    static final LinkedBlockingQueue<Boolean> throwingMethodResultQueue =
            new LinkedBlockingQueue<>();

    // -----------------------------------------------------------------------
    // Static coordination state for testScheduleMethodWithZone
    // Written by ScheduleBean.scheduledEvery5SecondsPacific()
    // -----------------------------------------------------------------------

    /** Reaches zero after 3 firings of the zone-aware scheduled method. */
    static final CountDownLatch zonedRanThriceLatch = new CountDownLatch(3);

    /** Total invocations of the zone-aware scheduled method. */
    static final AtomicInteger zonedRunCount = new AtomicInteger(0);

    // -----------------------------------------------------------------------
    // Test methods
    // -----------------------------------------------------------------------

    /**
     * Verify that a {@code @Schedule} annotation using a cron expression
     * fires the method repeatedly when placed directly on a CDI bean method.
     *
     * <p>The cron expression fires every 3 seconds.
     * The test waits until five invocations have completed, then confirms the
     * counter is at least 5.</p>
     */
    public void testScheduleMethodCron() throws Exception {
        assertEquals(true,
                     cronRan5TimesLatch.await(TIMEOUT_S, TimeUnit.SECONDS));
        assertEquals(true,
                     cronRunCount.get() >= 5);
    }

    /**
     * Verify that a method annotated directly with {@code @Schedule} runs
     * automatically and repeatedly without any explicit invocation.
     *
     * <p>The method uses a seconds-list schedule every 6 seconds. The test
     * waits until the latch has counted down twice, then confirms the run
     * counter is at least 2, proving the method fired more than once.</p>
     */
    public void testScheduleMethodEvery6Seconds() throws Exception {
        assertEquals(true,
                     every6SecondsRanTwiceLatch.await(TIMEOUT_S * 2,
                                                      TimeUnit.SECONDS));
        assertEquals(true,
                     every6SecondsRunCount.get() >= 2);
    }

    /**
     * Verify that a scheduled method never runs concurrently with itself.
     *
     * <p>The spec states that executions missed due to overlap are always
     * skipped. The method uses the cron expression that fires every 4 seconds
     * starting at second 1 (i.e. at seconds 1, 5, 9, 13, ...). The test
     * holds the first invocation open long enough for at least one additional
     * trigger to fire and be skipped, then releases it and checks that the
     * peak concurrent-execution count never exceeded 1.</p>
     */
    public void testScheduleMethodNotConcurrentWithSelf() throws Exception {
        // Wait until the first invocation has started
        assertEquals(true,
                     slowMethodStartedLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        // Hold for long enough that at least two more triggers (at 4-second
        // intervals) would fire while the method is still running.
        TimeUnit.SECONDS.sleep(9);

        // Release the held invocation
        slowMethodFinishLatch.countDown();

        // If concurrency was ever >1 the spec was violated
        assertEquals(1, slowMethodMaxConcurrent.get());
    }

    /**
     * Verify that a scheduled method that throws a {@link RuntimeException}
     * was at least invoked (observable side-effect before the throw).
     *
     * <p>Per the spec, scheduling stops for the method once it throws.
     * The test confirms the method ran at most once by waiting on the queue
     * that the method populates before throwing.</p>
     */
    public void testScheduleMethodStopsOnException() throws Exception {
        assertEquals(Boolean.TRUE,
                     throwingMethodResultQueue.poll(TIMEOUT_S, TimeUnit.SECONDS));
        assertEquals(null,
                     throwingMethodResultQueue.poll(8, TimeUnit.SECONDS));
    }

    /**
     * Verify that a {@code @Schedule} annotation with an explicit
     * {@link jakarta.enterprise.concurrent.Schedule#zone()} attribute is
     * honoured and the method still fires repeatedly.
     *
     * <p>The method fires every 5 seconds in the America/Los_Angeles zone.
     * The test confirms at least 3 invocations complete within the timeout,
     * which proves the container accepted and applied the zone-aware schedule.</p>
     */
    public void testScheduleMethodWithZone() throws Exception {
        assertEquals(true,
                     zonedRanThriceLatch.await(TIMEOUT_S * 3, TimeUnit.SECONDS));
        assertEquals(true,
                     zonedRunCount.get() >= 3);
    }
}
