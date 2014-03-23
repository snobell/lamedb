package lame.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BlockCodec {

	public int encode(byte[] block, OutputStream output) throws IOException;

	public InputStream decode(InputStream input, int blockLength) throws IOException;
}
