package lame.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IdentityBlockCodec implements BlockCodec {

	@Override
	public int encode(byte[] block, OutputStream output) throws IOException {
		output.write(block);
		return block.length;
	}

	@Override
	public InputStream decode(InputStream input, int blockLength) throws IOException {
		byte[] block = new byte[blockLength];
		int bytesRead = input.read(block, 0, blockLength);

		if (bytesRead != blockLength) {
			throw new RuntimeException("Error reading block");
		}

		return new ByteArrayInputStream(block);
	}
}
