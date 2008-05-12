/**
 * 
 */
package servidor.fisico.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import servidor.buffer.Bloque;
import servidor.buffer.Bloque.ID;
import servidor.fisico.DiskSpaceManager;

/**
 * Implementacion del Disk Manager.
 * @see File
 * @see ID2Nombre
 */
public final class DiskSpaceManagerImpl implements DiskSpaceManager {

    private static final String FILE_COULD_NOT_CREATED = "A new page could not be created. The file could not be created. ";
    
    /**
     * El directorio donde se guardaran los archivos que representan los bloques.
     */
    private File directorioRaiz;
    
    /**
     * Helper que convierte el identificador del bloque a un nombre de archivo.
     */
    private ID2Nombre helper;
    
    /**
     * Constructor de la clase. 
     * @param directorioRaiz el directorio donde se guardaran los archivos que representan los bloques.
     * Si no existe el directorio, se crea.
     */
    public DiskSpaceManagerImpl(File directorioRaiz) {
        this.directorioRaiz = directorioRaiz;
        this.directorioRaiz.mkdirs();
        this.helper = new ID2NombreConPunto();
    }

    /**
     * @see servidor.fisico.DiskSpaceManager#leerBloque(servidor.buffer.Bloque.ID)
     */
    public Bloque leerBloque(ID id) {
        File file = new File(this.directorioRaiz, this.helper.dameNombre(id)); 
        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                try {
                	// se crea un bloque y se llena con el contenido del archivo.
                    return new BloqueImpl(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
       	//throw new RuntimeException("Inconsistencia en los datos. No se encontro el archivo " + file);
       	return null;
    }

    /**
     * @see servidor.fisico.DiskSpaceManager#nuevoBloque(servidor.buffer.Bloque.ID)
     */
    public Bloque nuevoBloque(ID id) {
        File file = new File(this.directorioRaiz, this.helper.dameNombre(id));
        if (file.exists()) {
            return this.leerBloque(id);
        }
    	try {
    		if (!file.createNewFile()) {
    			throw new RuntimeException(FILE_COULD_NOT_CREATED + file);
    		}
    	} catch (IOException e) {
    		throw new RuntimeException(FILE_COULD_NOT_CREATED + file, e);
    	}
        return new BloqueImpl();
    }

    /**
     * @see servidor.fisico.DiskSpaceManager#guardarBloque(servidor.buffer.Bloque.ID, servidor.buffer.Bloque)
     */
    public void guardarBloque(ID id, Bloque bloque) {
        File file = new File(this.directorioRaiz, this.helper.dameNombre(id));
        try {
            OutputStream outputStream = new FileOutputStream(file);
            try {
            	outputStream.write(bloque.dameDatos());
            } finally {
                outputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bloque.desMarcar();
    }

    /**
     * @see servidor.fisico.DiskSpaceManager#borrarBloque(servidor.buffer.Bloque.ID)
     */
    public void borrarBloque(ID id) {
        File file = new File(this.directorioRaiz, this.helper.dameNombre(id)); 
        if (file.exists()) {
            if (!file.delete()) {
                //throw new RuntimeException("No se pudo borrar la pagina " + file);
            }
        }
    }

}
