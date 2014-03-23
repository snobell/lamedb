package lame.data;

import java.io.IOException;
import java.io.OutputStream;

public interface RecordEncoder {
	public void encode(Record record, OutputStream os) throws IOException;
}
