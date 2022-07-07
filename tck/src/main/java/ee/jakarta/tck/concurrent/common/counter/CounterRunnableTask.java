/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.common.counter;

import java.time.Duration;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestUtil;

public class CounterRunnableTask implements Runnable {

	private String countSingletionJndi = "";
	private long sleepTime = 0;

	public CounterRunnableTask(String countSingletionJndi) {
		this.countSingletionJndi = countSingletionJndi;
	}

	public CounterRunnableTask(String countSingletionJndi, long sleepTime) {
		this.countSingletionJndi = countSingletionJndi;
		this.sleepTime = sleepTime;
	}

	public void run() {
		try {
			if (sleepTime > 0) {
				TestUtil.sleep(Duration.ofMillis(sleepTime));
			}

			InitialContext context = new InitialContext();
			CounterInterface counter = (CounterInterface) context.lookup(countSingletionJndi);
			counter.inc();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
