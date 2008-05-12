// Created on Oct 24, 2005
package test.servidor.lock;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author lleggieri
 * @version $Id$
 */
public class LockTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test para test.servidor.lock");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(LockManagerTestCase.class));
		//$JUnit-END$
		return suite;
	}
}
