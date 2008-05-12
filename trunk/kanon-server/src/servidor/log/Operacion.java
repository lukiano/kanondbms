package servidor.log;

public enum Operacion {
	
	INSERT, 
    
    UPDATE, 
    
    DELETE,
    
    INSERT_INDEX,
    
    DELETE_INDEX,
    
    CLR_INSERT,
    
    CLR_UPDATE,
    
    CLR_DELETE,
    
    CLR_INSERT_INDEX,
    
    CLR_DELETE_INDEX,
    
    DUMMY_CLR,
    
    BEGIN,
    
    PREPARE,
    
    COMMIT,
    
    ROLLBACK,
    
    END,
    
    CHILD_COMMITTED,
    
    BEGIN_CHECKPOINT,
    
    END_CHECKPOINT;

}
