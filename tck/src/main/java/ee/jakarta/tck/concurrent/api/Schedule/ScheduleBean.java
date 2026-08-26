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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A CDI managed bean with methods annotated directly with {@link Schedule},
 * each using a different schedule configuration to provide broad coverage.
 *
 * <p>The five scheduled methods use:
 * <ul>
 *   <li>{@link #scheduledEvery6Seconds()} – seconds field list, no zone</li>
 *   <li>{@link #scheduledEvery5SecondsPacific()} – seconds field list, with an
 *       explicit time zone</li>
 *   <li>{@link #scheduledEvery3Seconds()} – cron expression (every 3 seconds)</li>
 *   <li>{@link #scheduledMethodThatThrows()} - seconds field list, no zone</li>
 *   <li>{@link #scheduledSlowMethod()} – cron expression firing every 4 seconds
 *       starting at second 1 (i.e. seconds 1, 5, 9, 13, ...)</li>
 * </ul>
 * </p>
 *
 * <p>Coordination state (latches, counters) lives as {@code static} fields on
 * {@link ScheduleServlet}, so there is no need for non-private fields here.</p>
 */
@ApplicationScoped
public class ScheduleBean {

    // Active-invocation counter for the slow method; used only within this
    // bean to compute the peak concurrency recorded in ScheduleServlet.
    private final AtomicInteger slowMethodActive = new AtomicInteger(0);

    /**
     * Fires every 3 seconds via a cron expression. Tests verify that
     * {@link Schedule#cron()} is supported on a plain CDI bean method.
     */
    @Schedule(cron = "*/3 * * * * *")
    public void scheduledEvery3Seconds() {
        ScheduleServlet.cronRunCount.incrementAndGet();
        ScheduleServlet.cronRan5TimesLatch.countDown();
    }

    /**
     * Fires every 5 seconds using the America/Los_Angeles zone. Tests verify
     * that a {@link Schedule} with an explicit {@link Schedule#zone()} is
     * honoured and the method still executes repeatedly.
     */
    @Schedule(seconds = { 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55 },
              minutes = {}, // minutes not restricted
              hours = {}, // hours not restricted
              zone = "America/Los_Angeles")
    public void scheduledEvery5SecondsPacific() {
        ScheduleServlet.zonedRunCount.incrementAndGet();
        ScheduleServlet.zonedRanThriceLatch.countDown();
    }

    /**
     * Fires every 6 seconds (at seconds 0, 6, 12, 18, 24, 30, 36, 42, 48, 54).
     * Tests observe {@link ScheduleServlet#ranTwiceLatch} to confirm that the
     * method is invoked multiple times automatically.
     */
    @Schedule(seconds = { 0, 6, 12, 18, 24, 30, 36, 42, 48, 54 },
              minutes = {}, // minutes not restricted
              hours = {} // hours not restricted
    )
    public void scheduledEvery6Seconds() {
        ScheduleServlet.every6SecondsRunCount.incrementAndGet();
        ScheduleServlet.every6SecondsRanTwiceLatch.countDown();
    }

    /**
     * Fires every 3 seconds and always throws a {@link RuntimeException}.
     * The spec states that scheduling stops when an invocation raises an
     * exception, so this method should not continue to be scheduled after
     * the first throw. The test confirms the method ran at least once.
     */
    @Schedule(seconds = { 0, 3, 6, 9, 12, 15, 18, 21, 24, 27,
                          30, 33, 36, 39, 42, 45, 48, 51, 54, 57 },
              minutes = {}, // minutes not restricted
              hours = {} // hours not restricted
    )
    public void scheduledMethodThatThrows() {
        ScheduleServlet.throwingMethodResultQueue.add(Boolean.TRUE);
        throw new RuntimeException(
                "intentional exception from scheduledMethodThatThrows");
    }

    /**
     * Fires at seconds 1, 5, 9, 13, ... (every 4 seconds starting at second 1).
     * Intentionally holds for a long time so that subsequent triggers overlap
     * and must be skipped per the spec. The test checks that the peak
     * concurrency count recorded in {@link ScheduleServlet#slowMethodMaxConcurrent}
     * never exceeds 1.
     */
    @Schedule(cron = "1/4 * * * * *")
    public void scheduledSlowMethod() throws InterruptedException {
        int active = slowMethodActive.incrementAndGet();
        int maxActive = ScheduleServlet.slowMethodMaxConcurrent.get();
        while (active > maxActive
               && // checkstyle requires this on a new line
               !ScheduleServlet.slowMethodMaxConcurrent.compareAndSet(maxActive,
                                                                      active)) {
            maxActive = ScheduleServlet.slowMethodMaxConcurrent.get();
        }
        ScheduleServlet.slowMethodStartedLatch.countDown();
        try {
            // Block until the test releases us, or for up to 2 minutes as a
            // safety valve so the suite is never permanently blocked.
            ScheduleServlet.slowMethodFinishLatch.await(2, TimeUnit.MINUTES);
        } finally {
            slowMethodActive.decrementAndGet();
        }
    }
}
