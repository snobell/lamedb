package lame.data;

import java.io.IOException;
import java.io.InputStream;

public interface RecordDecoder {
	public Record decode(InputStream is) throws IOException;
}
