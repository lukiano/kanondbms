// Created on Oct 24, 2005
package test.servidor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author lleggieri
 * @version $Id$
 */
public class ServidorTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test para test.servidor");
		//$JUnit-BEGIN$
		suite.addTest(test.servidor.fisico.FisicoTestSuite.suite());
		//$JUnit-END$
		return suite;
	}
}
