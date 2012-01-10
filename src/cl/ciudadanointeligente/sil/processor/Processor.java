package cl.ciudadanointeligente.sil.processor;

import org.hibernate.Session;

public interface Processor<SilModelClass,CiudadanoInteligenteModelClass> {
	public CiudadanoInteligenteModelClass process(SilModelClass source, Session session) throws Throwable;
}
