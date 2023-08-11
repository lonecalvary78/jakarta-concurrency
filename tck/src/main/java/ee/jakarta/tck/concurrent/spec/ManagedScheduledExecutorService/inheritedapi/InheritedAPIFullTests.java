/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Full;
import jakarta.ejb.EJB;

@Full
public class InheritedAPIFullTests {

    @Deployment(name = "InheritedAPITests")
    public static EnterpriseArchive createDeployment() {
        JavaArchive inheritedJAR = ShrinkWrap.create(JavaArchive.class, "inheritedapi.jar")
                .addClasses(InheritedAPIFullTests.class, CounterEJBProvider.class, TestEjb.class,
                        TestEjbInterface.class)
                .addPackages(true, PACKAGE.TASKS.getPackageName(), PACKAGE.COUNTER.getPackageName(),
                        PACKAGE.FIXED_COUNTER.getPackageName())
                .addAsServiceProvider(EJBJNDIProvider.class, CounterEJBProvider.FullProvider.class);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "inheritedapi.ear")
                .addAsModules(inheritedJAR);

        return ear;
    }

    @EJB
    private TestEjbInterface testEjb;

    /*
     * @testName: testApiSubmit
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.1
     *
     * @test_Strategy:
     */
    @Test
    public void testApiSubmit() {
        testEjb.testApiSubmit();
    }

    /*
     * @testName: testApiExecute
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.2
     *
     * @test_Strategy:
     */
    @Test
    public void testApiExecute() {
        testEjb.testApiExecute();
    }

    /*
     * @testName: testApiInvokeAll
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.3
     *
     * @test_Strategy:
     */
    @Test
    public void testApiInvokeAll() {
        testEjb.testApiInvokeAll();
    }

    /*
     * @testName: testApiInvokeAny
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.4
     *
     * @test_Strategy:
     */
    @Test
    public void testApiInvokeAny() {
        testEjb.testApiInvokeAny();
    }

    /*
     * @testName: testApiSchedule
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.5
     *
     * @test_Strategy:
     */
    @Test
    public void testApiSchedule() {
        testEjb.testApiSchedule();
    }

    /*
     * @testName: testApiScheduleAtFixedRate
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.6
     *
     * @test_Strategy:
     */
    @Test
    public void testApiScheduleAtFixedRate() {
        testEjb.testApiScheduleAtFixedRate();
    }

    /*
     * @testName: testApiScheduleWithFixedDelay
     *
     * @assertion_ids: CONCURRENCY:SPEC:44.7
     *
     * @test_Strategy:
     */
    @Test
    public void testApiScheduleWithFixedDelay() {
        testEjb.testApiScheduleWithFixedDelay();
    }

}
