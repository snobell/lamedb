package lame.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

public class DeflateBlockCodec implements BlockCodec {
	private byte[] buffer;

	public DeflateBlockCodec() {
		buffer = new byte[BinaryDataFileWriter.MAX_BLOCK_SIZE];
	}

	public int encode(byte[] block, OutputStream output) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(block);
		deflater.finish();

		int totalCompressedBytes = 0;

		while(!deflater.finished()) {
			int compressedBytes = deflater.deflate(buffer);
			output.write(buffer, 0, compressedBytes);
			totalCompressedBytes += compressedBytes;
		}
		deflater.end();

		return totalCompressedBytes;
	}

	@Override
	public InputStream decode(InputStream input, int blockLength) throws IOException {
		byte[] nextBlock = new byte[blockLength];

		int bytesRead = input.read(nextBlock, 0, blockLength);

		if (bytesRead != blockLength) {
			throw new RuntimeException("Error reading block");
		}

		return new InflaterInputStream(new ByteArrayInputStream(nextBlock));
	}
}
