package rodrigo.chaves.hoursbankconverter.reader;

import java.util.List;

public interface Reader<T> {

	List<T> read(java.io.Reader reader, String separator) throws Exception;
}
