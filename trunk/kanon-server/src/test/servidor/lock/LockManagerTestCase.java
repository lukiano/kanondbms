/**
 * 
 */
package test.servidor.lock;

import servidor.Id;
import servidor.lock.FabricaLockManager;
import servidor.lock.LockManager;
import junit.framework.TestCase;

/**
 * @author lleggieri
 *
 */
public class LockManagerTestCase extends TestCase {

    /**
     * 
     */
    public LockManagerTestCase() {
        super();
    }

    /**
     * @param name
     */
    public LockManagerTestCase(String name) {
        super(name);
    }

    public void testManager() throws Exception {
        final LockManager lockManager = FabricaLockManager.dameInstancia();
        final Id id1 = new StringID("id1");
        
        lockManager.bloquear(id1, false);
        Thread thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, false);
                lockManager.desbloquear(id1);
                System.out.println("thread 1 ok");
            }
        };
        thread.start();
        
        thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, true);
                lockManager.desbloquear(id1);
                System.out.println("thread 2 ok");
            }
        };
        thread.start();
        synchronized (this) { this.wait(1000);}
        lockManager.desbloquear(id1);
        System.out.println("main1 ok");
        synchronized (this) { this.wait(1000);}
    }
    
    public void testManager2() throws Exception {
        final LockManager lockManager = FabricaLockManager.dameInstancia();
        final Id id1 = new StringID("id1");
        
        lockManager.bloquear(id1, true);
        Thread thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, false);
                lockManager.desbloquear(id1);
                System.out.println("thread 3 ok");
            }
        };
        thread.start();
        
        thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, true);
                lockManager.desbloquear(id1);
                System.out.println("thread 4 ok");
            }
        };
        thread.start();
        synchronized (this) { this.wait(1000);}
        lockManager.desbloquear(id1);
        System.out.println("main2 ok");
        synchronized (this) { this.wait(1000);}
    }
 
    public void testManager3() throws Exception {
        final LockManager lockManager = FabricaLockManager.dameInstancia();
        final Id id1 = new StringID("id1");
        
        lockManager.bloquear(id1, false);
        Thread thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, false);
                lockManager.desbloquear(id1);
                System.out.println("thread 5 ok");
            }
        };
        thread.start();
        
        thread = new Thread() {
            @Override
			public void run() {
                lockManager.bloquear(id1, true);
                lockManager.desbloquear(id1);
                System.out.println("thread 6 ok");
            }
        };
        thread.start();
        synchronized (this) { this.wait(500);}
        lockManager.desbloquear(id1);
        synchronized (this) { this.wait(500);}
        lockManager.bloquear(id1, true);
        System.out.println("main3 ok");
        synchronized (this) { this.wait(1000);}
    }
    /**
     * Clase interna para representar ID usada en el caso de test.
     * @author lleggieri
     *
     */
    private static class StringID implements Id {
        
        /**
         * el id representado por un String.
         */
        private String string;
     
        /**
         * Constructor de la clase.
         * @param string el ID de esta instancia.
         */
        public StringID(String string) {
            if (string == null) {
                throw new NullPointerException("El String de un StringID no puede ser nulo.");
            }
            this.string = string;
        }
        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
		public String toString() {
            return this.string;
        }
        
    }
    
}
