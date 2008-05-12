// Created on Oct 24, 2005
package test.servidor.buffer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author lleggieri
 * @version $Id: BufferTestSuite.java,v 1.1 2005/10/25 02:33:48 sqquima Exp $
 */
public class BufferTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test para test.servidor.buffer");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(PoliticaTestCase.class));
		//$JUnit-END$
		return suite;
	}
}
