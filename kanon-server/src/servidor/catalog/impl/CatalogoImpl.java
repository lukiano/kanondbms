/**
 * 
 */
package servidor.catalog.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import servidor.buffer.BufferManager;
import servidor.buffer.FabricaBufferManager;
import servidor.catalog.Catalogo;
import servidor.catalog.Valor;
import servidor.catalog.tipo.Conversor;
import servidor.catalog.tipo.Tipo;
import servidor.indice.hash.FabricaHashManager;
import servidor.tabla.Campo;
import servidor.tabla.Columna;
import servidor.tabla.FabricaTabla;
import servidor.tabla.Pagina;
import servidor.tabla.Registro;
import servidor.tabla.Tabla;
import servidor.tabla.impl.ColumnaImpl;
import servidor.tabla.impl.FabricaTablaNormal;
import servidor.tabla.impl.FabricaTablaSistema;
import servidor.tabla.impl.TablaDecoradoraSoloLectura;
import servidor.util.Iterador;



/*
 * Tabla de Tablas:
 * 
 * NombreTabla | IdTabla
 * ------------+--------
 *             |
 *             |
 * 
 * 
 * 
 * 
 * Tabla de Columnas:
 * 
 * NombreTabla | NombreColumna | PosicionColumna | TipoColumna | LongitudCampo
 * ------------+---------------+-----------------+-------------+--------------
 *             |               |                 |             |
 *             |               |                 |             |
 *
 * 
 *
 * 
 * Tabla de Paginas:
 * 
 * NombreTabla | CantidadDePaginas
 * ------------+------------------
 *             |
 *             |
 * 
 */

/**
 * @author lleggieri
 *
 */
public class CatalogoImpl implements Catalogo {
    
    /**
     * Instancia del Buffer Manager usada para obtener los diferentes bloques.
     */
    private BufferManager bufferManager;
    
    /**
     * Fabrica de las implementaciones de las tablas de usuario.
     */
    private FabricaTabla fabricaTabla = new FabricaTablaNormal();
    
    /**
     * Fabrica de las implementaciones de las tablas del sistema.
     */
    private FabricaTabla fabricaTablaSistema = new FabricaTablaSistema();

    /**
     * Mapa con las columnas de las distintas tablas del sistema.
     */
    private static final Map<String, Columna[]> ColumnaTablasSistema = new HashMap<String, Columna[]>();
    
    /**
     * Mapa con los identificadores numericos de las tablas del sistema.
     */
    private static final Map<String, Integer> NumeroTablasSistema = new HashMap<String, Integer>();
    
    // se llenan los mapas al levantarse la clase.
    static {
    	ColumnaTablasSistema.put(NOMBRE_TABLA_DE_TABLAS, 
    			new Columna[] { 
    				new ColumnaImpl("NombreTabla", LONGITUD_CAMPO_NOMBRE_TABLA, 0, Tipo.CHAR), 
    				new ColumnaImpl("IdTabla", LONGITUD_INT, 1, Tipo.NUMERIC)
    			});
    	
    	ColumnaTablasSistema.put(NOMBRE_TABLA_DE_PAGINAS,
    			new Columna[] {  
					new ColumnaImpl("NombreTabla", LONGITUD_CAMPO_NOMBRE_TABLA, 0, Tipo.CHAR),
					new ColumnaImpl("CantidadDePaginas", LONGITUD_INT, 1, Tipo.NUMERIC)
				});

    	ColumnaTablasSistema.put(NOMBRE_TABLA_DE_COLUMNAS,
    			new Columna[] { 
    				new ColumnaImpl("NombreTabla", LONGITUD_CAMPO_NOMBRE_TABLA, 0, Tipo.CHAR),
    				new ColumnaImpl("NombreColumna", LONGITUD_CAMPO_NOMBRE_COLUMNA, 1, Tipo.CHAR),
    				new ColumnaImpl("PosicionColumna", LONGITUD_INT, 2, Tipo.NUMERIC),
    				new ColumnaImpl("TipoColumna", LONGITUD_CAMPO_TIPO_COLUMNA, 3, Tipo.CHAR),
    				new ColumnaImpl("LongitudCampo", LONGITUD_INT, 4, Tipo.NUMERIC) 
    			});
    	NumeroTablasSistema.put(NOMBRE_TABLA_DE_TABLAS, Integer.MAX_VALUE);
    	NumeroTablasSistema.put(NOMBRE_TABLA_DE_COLUMNAS, Integer.MAX_VALUE-1);
    	NumeroTablasSistema.put(NOMBRE_TABLA_DE_PAGINAS, Integer.MAX_VALUE-2);
    }
    
    /**
     * Constructor por omision de la clase.
     */
    public CatalogoImpl() {
    }

    /**
     * @see servidor.catalog.Catalogo#dameTabla(java.lang.String)
     */
    public Tabla dameTabla(String nombreTabla) {
    	Tabla tabla = this.obtenerTabla(nombreTabla);
    	if (ColumnaTablasSistema.containsKey(nombreTabla)) {
    		// se desea una tabla del sistema
    		tabla = new TablaDecoradoraSoloLectura(tabla);
    	}
    	return tabla;
    }
    
    /**
     * Metodo interno para obtener la implementacion de una tabla y trabajar con la misma.
     */
    public Tabla obtenerTabla(String nombreTabla) {
        Conversor conversorATexto = Conversor.conversorATexto();
    	if (ColumnaTablasSistema.containsKey(nombreTabla)) {
    		// se obtiene la tabla del sistema de manera 'hardcodeada'
            // (por algo hay que empezar)
    		return this.crearImplementacionTabla(
                    nombreTabla,
                    NumeroTablasSistema.get(nombreTabla),
                    ColumnaTablasSistema.get(nombreTabla),
                    true);
    	}
    	// no se está pidiendo una tabla del sistema
    	Tabla tabla = this.obtenerTabla(NOMBRE_TABLA_DE_TABLAS);
    	
        // se itera por la Tabla de Tablas para ver si existe la tabla deseada.
    	
        // Iterador<Registro.ID> iterador = tabla.registros(); // ahora se usan los indices.
    	int columna = 0;
    	Columna[] columnas = tabla.columnas();
    	Campo campoColumna = columnas[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	Iterador<Registro.ID> iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, 
					Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro proximoRegistro = tabla.registro(idProximo);
                Object nombreTablaCrudo = proximoRegistro.valor(0); // nombre de la tabla
                Object idTablaCrudo = proximoRegistro.valor(1); // id de la tabla
                tabla.liberarRegistro(idProximo);
                if (nombreTabla.equals(conversorATexto.convertir(tabla.columnas()[0].campo(), nombreTablaCrudo))) {
                	int numeroTabla = Integer.valueOf((String)conversorATexto.convertir(tabla.columnas()[1].campo(), idTablaCrudo));
                    return this.crearImplementacionTabla(
                            nombreTabla,
                            numeroTabla,
                            this.columnasDeTabla(nombreTabla),
                            false);
                }
            }
        } finally {
            iterador.cerrar();
        }
        // la tabla no existe
      	return null;
    }
    
    /**
     * @see servidor.catalog.Catalogo#tablaDelSistema(java.lang.String)
     */
    public boolean tablaDelSistema(String nombreTabla) {
    	return Catalogo.NOMBRE_TABLA_DE_TABLAS.equals(nombreTabla) ||
    		Catalogo.NOMBRE_TABLA_DE_PAGINAS.equals(nombreTabla) ||
    		Catalogo.NOMBRE_TABLA_DE_COLUMNAS.equals(nombreTabla);
    }

    /**
     * Metodo para crear la implementacion de una tabla.
     * Delega el pedido a alguna de las fabricas de tabla correspondientes.
     * @param nombreTabla el nombre de la tabla.
     * @param idTabla el identificador numerico de la tabla.
     * @param columnas las columnas de la tabla.
     * @param tablaDelSistema true si es una tabla del sistema.
     * @return una implementacion de la tabla, segun alguna de las fabricas disponibles.
     */
    private Tabla crearImplementacionTabla(String nombreTabla, int idTabla, Columna[] columnas, boolean tablaDelSistema) {
    	// Esto seria como un Design Pattern AbstractFactory.
    	Tabla tabla;
    	if (tablaDelSistema) { // del sistema
    		tabla = this.fabricaTablaSistema.dameTabla(idTabla, nombreTabla, columnas);
    	} else {
    		tabla = this.fabricaTabla.dameTabla(idTabla, nombreTabla, columnas);
    	}
        // tabla = new InspectorTabla(tabla); // descomentar para agregar un inspector a las tablas.
    	return tabla;
    }

    /**
     * @see servidor.catalog.Catalogo#borrarTabla(java.lang.String)
     */
    public void borrarTabla(String nombreTabla) {
        
        /* pasos a seguir (en orden):
         * 
         *  - borrar la entrada que referencia a la tabla de la "Tabla de Tablas".
         *  - borrar las entradas que referencian a las columnas de la tabla de la "Tabla de Columnas".
         *  - borrar la entrada que referencia a la tabla de la "Tabla de Paginas".
         *  - borrar todas las paginas de la tabla usando el BufferManager.
         */

        Tabla tabla = this.obtenerTabla(nombreTabla);
        if (tabla == null) {
        	throw new RuntimeException("No table exists with name '" + nombreTabla + "'.");
        }

        Conversor conversorATexto = Conversor.conversorATexto();
        tabla = this.obtenerTabla(NOMBRE_TABLA_DE_TABLAS);
        // se itera por la Tabla de Tabla para borrar la tabla a borrar (valga la redundancia).
        // Iterador<Registro.ID> iterador = tabla.registros(); // Ahora se usan los indices
    	int columna = 0;
    	Campo campoColumna = ColumnaTablasSistema.get(NOMBRE_TABLA_DE_TABLAS)[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	Iterador<Registro.ID> iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));

    	int numeroTabla = 0;
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro proximoRegistro = tabla.registro(idProximo);
                Object nombreTablaCrudo = proximoRegistro.valor(0); // nombre de la tabla
                Object idTablaCrudo = proximoRegistro.valor(1); // id de la tabla
                tabla.liberarRegistro(idProximo);
                if (nombreTabla.equals(conversorATexto.convertir(tabla.columnas()[0].campo(), nombreTablaCrudo))) {
                	numeroTabla = Integer.valueOf((String)conversorATexto.convertir(tabla.columnas()[1].campo(), idTablaCrudo));
                	tabla.borrarRegistro(idProximo);
                	break;
                }
            }
        } finally {
            iterador.cerrar();
        }
        
        tabla = this.obtenerTabla(NOMBRE_TABLA_DE_COLUMNAS);
        // se itera por la Tabla de Columnas para borrar la tabla a borrar (valga la redundancia).
    	campoColumna = ColumnaTablasSistema.get(NOMBRE_TABLA_DE_COLUMNAS)[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro proximoRegistro = tabla.registro(idProximo);
                Object columna0 = proximoRegistro.valor(0); // nombre de la tabla
                if (nombreTabla.equals(conversorATexto.convertir(tabla.columnas()[0].campo(), columna0))) {
                	tabla.borrarRegistro(idProximo);
                }
            }
        } finally {
            iterador.cerrar();
        }

        int cantidadDePaginas = 0;
        tabla = this.obtenerTabla(NOMBRE_TABLA_DE_PAGINAS);
        // se itera por la Tabla de Paginas para borrar la tabla a borrar (valga la redundancia).
    	campoColumna = ColumnaTablasSistema.get(NOMBRE_TABLA_DE_PAGINAS)[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro proximoRegistro = tabla.registro(idProximo);
                Object columna0 = proximoRegistro.valor(0); // nombre de la tabla
                if (nombreTabla.equals(conversorATexto.convertir(tabla.columnas()[0].campo(), columna0))) {
                	Object columna1 = proximoRegistro.valor(1);
                	String cantPaginas = (String) conversorATexto.convertir(tabla.columnas()[1].campo(), columna1);
                	cantidadDePaginas = Integer.parseInt(cantPaginas) + 1;
                	tabla.borrarRegistro(idProximo);
                	break;
                }
            }
        } finally {
            iterador.cerrar();
        }
        
        // se borran las paginas propiamente dichas de la tabla
        Tabla.ID idTabla = Tabla.ID.nuevoID(nombreTabla, numeroTabla);
        BufferManager bufferManager = this.getBufferManager();
        for (int i = 0; i < cantidadDePaginas; i++) {
            bufferManager.borrarBloque(Pagina.ID.nuevoID(idTabla, i));
        }
        this.getBufferManager().guardarBloquesModificados();
    }

    /**
     * @see servidor.catalog.Catalogo#crearTabla(java.lang.String, servidor.tabla.Columna[])
     */
    public synchronized void crearTabla(String nombreTabla,
            Columna... columnas) {
        if (this.obtenerTabla(nombreTabla) != null) {
            throw new RuntimeException("A table with name '" + nombreTabla + "' already exists.");
        }
        this.insertarTabla(nombreTabla, columnas);
        this.getBufferManager().guardarBloquesModificados();
    }
    
    

    /**
     * Metodo que inserta una entrada de una nueva tabla en el catalogo.
     * (Tabla de tablas y tabla de columnas).
     * @param nombreTabla el nombre de la tabla nueva.
     * @param columnas las columnas de la tabla.
     */
    private void insertarTabla(String nombreTabla, Columna[] columnas) {
        Conversor conversor = Conversor.conversorDeTexto();
        String numeroTabla = this.dameNumeroTablaLibre();
        {
            Tabla tablaTabla = this.obtenerTabla(NOMBRE_TABLA_DE_TABLAS);
            Object nombreTablaCrudo = conversor.convertir(tablaTabla.columnas()[0].campo(), nombreTabla);
            Object numeroTablaCrudo = conversor.convertir(tablaTabla.columnas()[1].campo(), numeroTabla);
            Columna[] columnasTablaTablas = tablaTabla.columnas();
            Collection<Valor> valores = new ArrayList<Valor>(columnasTablaTablas.length);
            valores.add(Valor.nuevoValor(0, columnasTablaTablas[0].campo(), nombreTablaCrudo));
            valores.add(Valor.nuevoValor(1, columnasTablaTablas[1].campo(), numeroTablaCrudo));
			tablaTabla.insertarRegistro(valores);
        }
        {
            Tabla columnasTabla = this.obtenerTabla(NOMBRE_TABLA_DE_COLUMNAS);
            Columna[] columnasTablaColumnas = columnasTabla.columnas();
            for (int i = 0; i < columnas.length; i++) {
            	Object[] columna = new Object[columnasTablaColumnas.length];
                columna[0] = conversor.convertir(columnasTablaColumnas[0].campo(), nombreTabla);
                columna[1] = conversor.convertir(columnasTablaColumnas[1].campo(), columnas[i].nombre());
                columna[2] = conversor.convertir(columnasTablaColumnas[2].campo(), Integer.toString(i));
                columna[3] = conversor.convertir(columnasTablaColumnas[3].campo(), columnas[i].campo().tipo().name());
                columna[4] = conversor.convertir(columnasTablaColumnas[4].campo(), Integer.toString(columnas[i].campo().longitud()));

                Collection<Valor> valores = new ArrayList<Valor>(columnasTablaColumnas.length);
                for (int j = 0; j < columnasTablaColumnas.length; j++) {
                	valores.add(Valor.nuevoValor(j, columnasTablaColumnas[j].campo(), columna[j]));
                }
                columnasTabla.insertarRegistro(valores);
            }
        }
    }

	/**
	 * Itera por la tabla de tablas hasta encontrar el identificador mayor, y devuelve ese + 1.
	 * @return Un identificador numerico en desuso para la tabla.
	 */
	private String dameNumeroTablaLibre() {
		int numeroTablaMaximo = 0;
		Conversor conversor = Conversor.conversorATexto();
		Tabla tabla = this.obtenerTabla(NOMBRE_TABLA_DE_TABLAS);
		Campo numeroTablaCampo = tabla.columnas()[1].campo();
		Iterador<Registro.ID> iterador = tabla.registros();
		try {
			while (iterador.hayProximo()) {
				Registro.ID idRegistro = iterador.proximo();
				Registro registro = tabla.registro(idRegistro);
				try {
					Object numeroTablaCrudo = registro.valor(1);
					String numeroTablaString = (String) conversor.convertir(numeroTablaCampo, numeroTablaCrudo);
					int numeroTabla = Integer.valueOf(numeroTablaString);
					if (numeroTabla >= numeroTablaMaximo) {
						numeroTablaMaximo = numeroTabla + 1;
					}
				} finally {
					tabla.liberarRegistro(idRegistro);
				}
			};
		} finally {
			iterador.cerrar();
		}
		return String.valueOf(numeroTablaMaximo);
	}

	/**
	 * @return la instancia de Buffer Manager.
	 */
	private synchronized BufferManager getBufferManager() {
		if (bufferManager == null) {
			bufferManager = FabricaBufferManager.dameInstancia();
		}
		return bufferManager;
	}

	/**
	 * @see servidor.catalog.Catalogo#columnasDeTabla(java.lang.String)
	 */
	public Columna[] columnasDeTabla(String nombreTabla) {
    	if (this.tablaDelSistema(nombreTabla)) {
    		// se obtienen las columnas de la tabla del sistema de manera 'hardcodeada'
            // (por algo hay que empezar)
    		return ColumnaTablasSistema.get(nombreTabla);
    	}
    	
    	if (!this.existeTabla(nombreTabla)) {
    		throw new RuntimeException("No table exists with name '" + nombreTabla + "'.");
    	}

		// no se piden columnas de una tabla del sistema
        Conversor conversorATexto = Conversor.conversorATexto();
        List<Columna> resultado = new ArrayList<Columna>(512);
        // hack
        for (int i = 0; i < 512; i++) {
        	resultado.add(null);
        }
        int maxPos = 0;
    	// Se obtiene e itera la tabla de columnas y se devuelve el resultado
        Tabla tabla = this.obtenerTabla(Catalogo.NOMBRE_TABLA_DE_COLUMNAS);
        
        // Iterador<Registro.ID> iterador = tabla.registros();
    	int columna = 0;
    	Columna[] columnas = tabla.columnas();
    	Campo campoColumna = columnas[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	Iterador<Registro.ID> iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));

    	// Se lee la tabla de columnas y se arman las Implementaciones Columna correspondientes.
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro registro = tabla.registro(idProximo);
                try {
                    String nombreTablaEnFila = 
                        (String) conversorATexto.convertir(tabla.columnas()[0].campo(), registro.valor(0));
                    if (nombreTablaEnFila.equals(nombreTabla)) {
                        String nombreColumna = 
                            (String) conversorATexto.convertir(tabla.columnas()[1].campo(), registro.valor(1));
                        String posicionColumnaString = 
                            (String) conversorATexto.convertir(tabla.columnas()[2].campo(), registro.valor(2));
                        String tipoColumnaString = 
                            (String) conversorATexto.convertir(tabla.columnas()[3].campo(), registro.valor(3));
                        String longitudCampoColumnaString = 
                            (String) conversorATexto.convertir(tabla.columnas()[4].campo(), registro.valor(4));
                        Tipo tipoColumna = Tipo.valueOf(tipoColumnaString);
                        Integer posicionColumna = Integer.valueOf(posicionColumnaString);
                        Integer longitudCampoColumna = Integer.valueOf(longitudCampoColumnaString);
                        Columna columnaActual = new ColumnaImpl(
                                nombreColumna, 
                                longitudCampoColumna, 
                                posicionColumna, 
                                tipoColumna);
                        resultado.set(posicionColumna, columnaActual);
                        if (posicionColumna > maxPos) {
                        	maxPos = posicionColumna;
                        }
                    }
                } finally {
                	tabla.liberarRegistro(idProximo);
                }
            }
        } finally {
            iterador.cerrar();
        }
        resultado = resultado.subList(0, maxPos + 1);
        return resultado.toArray(new Columna[resultado.size()]);
	}

	/**
	 * @see servidor.catalog.Catalogo#actualizarTablaPaginas(java.lang.String, int)
	 */
	public void actualizarTablaPaginas(String nombreTabla, int nroPagina) {
		
		if (NOMBRE_TABLA_DE_PAGINAS.equals(nombreTabla)) {
			// no se actualizan las paginas de la tabla de paginas
			return;
		}

    	if (!this.existeTabla(nombreTabla)) {
    		throw new RuntimeException("No table exists with name '" + nombreTabla + "'.");
    	}

    	// Se obtiene e itera la tabla de paginas y se actualiza el valor si ya existía una entrada
        Tabla tablaPaginas = this.obtenerTabla(NOMBRE_TABLA_DE_PAGINAS);
        
        // Iterador<Registro.ID> iterador = tablaPaginas.registros();
    	int columnaNombreTabla = 0;
    	int columnaCantidadPaginas = 1;
    	Columna[] columnasPaginas = tablaPaginas.columnas();
    	Campo campoColumnaNombreTabla = columnasPaginas[columnaNombreTabla].campo();
    	Campo campoColumnaCantidadPaginas = columnasPaginas[columnaCantidadPaginas].campo();

		Conversor conversorDeTexto = Conversor.conversorDeTexto();
    	Object nombreTablaCodificado = conversorDeTexto.convertir(campoColumnaNombreTabla, nombreTabla);
    	Object cantidadPaginasCodificado = conversorDeTexto.convertir(campoColumnaCantidadPaginas, String.valueOf(nroPagina));
    	
    	// pido el indice de la columna de los nombres con el nombre de la tabla (deberia haber a lo sumo un registro en ese indice)
    	Iterador<Registro.ID> iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tablaPaginas.id(), columnaNombreTabla, nombreTablaCodificado);

        try {
            while (iterador.hayProximo()) {
            	// ya existe la entrada
                Registro.ID idProximo = iterador.proximo();
               	tablaPaginas.actualizarRegistro(idProximo,
               			Collections.singleton(
               					Valor.nuevoValor(columnaCantidadPaginas, 
               									campoColumnaCantidadPaginas, 
               									cantidadPaginasCodificado)));
               	this.getBufferManager().guardarBloquesModificados();
                return;
            }
        } finally {
            iterador.cerrar();
        }
		// no existe la entrada => se crea
        Collection<Valor> valores = new ArrayList<Valor>();
        valores.add(Valor.nuevoValor(0, campoColumnaNombreTabla, nombreTablaCodificado));
        valores.add(Valor.nuevoValor(1, campoColumnaCantidadPaginas, cantidadPaginasCodificado));
        tablaPaginas.insertarRegistro(valores);
        this.getBufferManager().guardarBloquesModificados();
	}
    
    public boolean existeTabla( String nombreTabla ) {
    	Conversor conversorATexto = Conversor.conversorATexto();
    	if (ColumnaTablasSistema.containsKey(nombreTabla)) {
            // las tablas del sistema existen
    		return true;
    	}
    	// no se está pidiendo una tabla del sistema
    	Tabla tabla = this.obtenerTabla(NOMBRE_TABLA_DE_TABLAS);
    	
        // se itera por la Tabla de Tablas para ver si existe la tabla deseada.
    	int columna = 0;
    	Columna[] columnas = tabla.columnas();
    	Campo campoColumna = columnas[columna].campo();
    	// pido el indice de la columna de los nombres con el nombre de la tabla
    	Iterador<Registro.ID> iterador = FabricaHashManager.dameInstancia() 
			.dameRegistros(tabla.id(), columna, 
					Conversor.conversorDeTexto().convertir(campoColumna, nombreTabla));
        try {
            while (iterador.hayProximo()) {
                Registro.ID idProximo = iterador.proximo();
                Registro proximoRegistro = tabla.registro(idProximo);
                Object nombreTablaCrudo = proximoRegistro.valor(0); // nombre de la tabla
                tabla.liberarRegistro(idProximo);
                String nombreTablaString = (String) conversorATexto.convertir(tabla.columnas()[0].campo(), nombreTablaCrudo); 
                if (nombreTabla.equals(nombreTablaString)) {
                	return true;
                }
            }
        } finally {
            iterador.cerrar();
        }
        // la tabla no existe (no hay una entrada en la Tabla de Tablas) 
      	return false;    
    }

    /**
     * @see servidor.catalog.Catalogo#dameColumnaEnPosicion(java.lang.String, int)
     */
    public Columna dameColumnaEnPosicion(String nombreTabla, int position) {
    	if (!this.existeTabla(nombreTabla)) {
    		throw new RuntimeException("No table exists with name '" + nombreTabla + "'.");
    	}
    	
        Columna res = null;
        //Se crea un conversor de los datos de los registros a texto.
        Conversor conversorATexto = Conversor.conversorATexto();
        //obtengo la tabla
        Tabla tabla = this.obtenerTabla(NOMBRE_TABLA_DE_COLUMNAS);
        // se itera por la Tabla de Columnas para encontrar la posicion de la columna deseada. 
        Iterador<Registro.ID> iterador = tabla.registros();
        
        try {
            while (iterador.hayProximo())
            {
                Registro.ID idProximo       = iterador.proximo();
                Registro proximoRegistro    = tabla.registro(idProximo);
                try {
                    String nombreTablaEnRegistro = (String) conversorATexto.convertir(tabla.columnas()[0].campo(), proximoRegistro.valor(1));
                    String posicionEnRegistro = (String) conversorATexto.convertir(tabla.columnas()[2].campo(), proximoRegistro.valor(3));
                    Integer posicionCol = Integer.valueOf(posicionEnRegistro);
                    //si es el nombre de la tabla q estoy buscando...
                    if ( nombreTablaEnRegistro.equals(nombreTabla) && posicionCol.equals(position) ) {
                        //obtengo los datos de la columna
                        String nombreColumna                = (String) conversorATexto.convertir(tabla.columnas()[1].campo(), proximoRegistro.valor(2));
                        String posicionColumnaString        = (String) conversorATexto.convertir(tabla.columnas()[2].campo(), proximoRegistro.valor(3));
                        String tipoColumnaString            = (String) conversorATexto.convertir(tabla.columnas()[3].campo(), proximoRegistro.valor(4));
                        String longitudCampoColumnaString   = (String) conversorATexto.convertir(tabla.columnas()[4].campo(), proximoRegistro.valor(5));
                        Tipo   tipoColumna                  = Tipo.valueOf(tipoColumnaString); 
                        Integer posicionColumna             = Integer.valueOf(posicionColumnaString);
                        Integer longitudCampoColumna        = Integer.valueOf(longitudCampoColumnaString);
                        
                        //creo la columna a devolver en dicha posicion
                         res = new ColumnaImpl(  nombreColumna, longitudCampoColumna, posicionColumna,  tipoColumna); 
                    }
                } finally {
                	tabla.liberarRegistro(idProximo);
                }
                                
            }
        }   
        finally {
            iterador.cerrar();
        }
        return res;
    }
   
}
