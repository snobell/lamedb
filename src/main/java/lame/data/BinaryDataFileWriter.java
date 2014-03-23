package lame.data;

import lame.schema.RecordField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class BinaryDataFileWriter {
	static final int MAX_BLOCK_SIZE = 16384;

	private final OutputStream output;
	private RecordEncoder recordEncoder;
	private BlockCodec blockCodec;

	private ByteArrayOutputStream currentBlock;
	private long recordsInCurrentBlock;
	private byte[] syncMarker;

	public BinaryDataFileWriter(RecordField schema, OutputStream output) throws IOException {
		this(schema, output, new DeflateBlockCodec());
	}

	public BinaryDataFileWriter(RecordField schema, OutputStream output, BlockCodec blockCodec) throws IOException {
		this.output = output;
		this.recordEncoder = new BinaryRecordEncoder();
		this.blockCodec = blockCodec;

		currentBlock = new ByteArrayOutputStream();
		recordsInCurrentBlock = 0;

		syncMarker = generateSyncMarker();
		writeHeader(schema, syncMarker, blockCodec.getClass());
	}

	public void write(Record record) throws IOException {
		recordEncoder.encode(record, currentBlock);
		recordsInCurrentBlock++;

		if (currentBlock.size() > MAX_BLOCK_SIZE) {
			writeBlock();
		}
	}

	public void close() throws IOException {
		if (currentBlock.size() > 0) {
			writeBlock();
		}

		output.close();
	}

	private void writeHeader(RecordField schema, byte[] syncMarker, Class blockCodec) throws IOException {
		BinarySchemaEncoder schemaEncoder = new BinarySchemaEncoder();
		schemaEncoder.encode(schema, output);

		BinaryRecordEncoder.encodeString(blockCodec.getCanonicalName(), output);

		output.write(syncMarker);
	}

	private void writeBlock() throws IOException {
		ByteArrayOutputStream encodedBlock = new ByteArrayOutputStream(currentBlock.size());
		int encodedBlockSize = blockCodec.encode(currentBlock.toByteArray(), encodedBlock);

		output.write(BinaryRecordEncoder.encodeLong(recordsInCurrentBlock));
		output.write(BinaryRecordEncoder.encodeInt(encodedBlockSize));
		encodedBlock.writeTo(output);
		output.write(syncMarker);

		currentBlock.reset();
		recordsInCurrentBlock = 0;
	}

	private byte[] generateSyncMarker() {
		byte[] syncMarker = new byte[16];
		Random random = new Random();
		random.nextBytes(syncMarker);
		return syncMarker;
	}
}
