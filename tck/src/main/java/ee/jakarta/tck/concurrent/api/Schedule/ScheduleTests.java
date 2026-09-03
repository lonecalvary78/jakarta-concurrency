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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@RunAsClient
public class ScheduleTests extends TestClient {

    @ArquillianResource(ScheduleServlet.class)
    private URL baseURL;

    @Deployment(name = "ScheduleTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "ScheduleTests_web.war")
                .addPackages(false, ScheduleTests.class.getPackage());
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "ScheduleServlet";
    }

    // Numbers in assertion ids are line numbers in the Schedule Javadoc source

    @Assertion(id = "JAVADOC:40", strategy = """
            Tests that the Schedule.cron() attribute is supported when
            @Schedule is placed directly on a CDI managed bean method.
            The cron expression "*/3 * * * * *" fires the method every
            3 seconds and the test confirms multiple invocations occur.
            """)
    public void testScheduleMethodCron() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:40", strategy = """
            Tests that a method annotated directly with @Schedule on a CDI
            managed bean is invoked automatically and repeatedly by the
            container without any explicit caller invocation.
            The schedule uses a seconds-field list (every 6 seconds).
            """)
    public void testScheduleMethodEvery6Seconds() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:56", strategy = """
            Tests that a scheduled method never runs concurrently with itself.
            The spec requires that executions missed due to overlap are always
            skipped. The schedule uses cron "1/4 * * * * *" (fires at seconds
            1, 5, 9, 13, ...). The test holds the first invocation open
            across multiple trigger times and verifies that the peak
            concurrent execution count never exceeds 1.
            """)
    public void testScheduleMethodNotConcurrentWithSelf() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:60", strategy = """
            Tests that a @Schedule method that throws a RuntimeException is
            observed to have run at least once. Per the spec, scheduling
            continues until an invocation raises an exception or error;
            the test verifies the method was invoked before the throw and
            confirms that scheduled execution does not continue indefinitely
            after the exception.
            """)
    public void testScheduleMethodStopsOnException() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:47", strategy = """
            Tests that a @Schedule annotation with an explicit zone attribute
            (America/Los_Angeles) is accepted and the method still fires
            repeatedly. The schedule fires every 5 seconds.
            """)
    public void testScheduleMethodWithZone() throws Exception {
        runTest(baseURL, testname);
    }
}
