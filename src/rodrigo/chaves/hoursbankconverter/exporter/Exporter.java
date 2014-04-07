package rodrigo.chaves.hoursbankconverter.exporter;

import java.io.File;
import java.util.List;

public interface Exporter<T> {

	public abstract File export(List<T> checkpoints) throws Exception;
	
}
