// Created on Oct 24, 2005
package test.servidor.catalogo;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author lleggieri
 * @version $Id: CatalogoTestSuite.java,v 1.1 2005/11/17 20:28:32 sqquima Exp $
 */
public class CatalogoTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test para test.servidor.catalogo");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(CatalogoTestCase.class));
		//$JUnit-END$
		return suite;
	}
}
