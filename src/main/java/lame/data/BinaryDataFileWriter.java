package lame.data;

import lame.schema.RecordField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class BinaryDataFileWriter {
	private static final int MAX_BLOCK_SIZE = 2048;

	private final OutputStream os;
	private RecordEncoder recordEncoder;
	private ByteArrayOutputStream currentBlock;
	private long recordsInCurrentBlock;
	private byte[] syncMarker;

	public BinaryDataFileWriter(RecordField schema, OutputStream os) throws IOException {
		this.os = os;

		syncMarker = generateSyncMarker();
		writeHeader(schema, syncMarker);

		recordEncoder = new BinaryRecordEncoder();

		currentBlock = new ByteArrayOutputStream();
		recordsInCurrentBlock = 0;
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

		os.close();
	}

	private void writeHeader(RecordField schema, byte[] syncMarker) throws IOException {
		BinarySchemaEncoder schemaEncoder = new BinarySchemaEncoder();
		schemaEncoder.encode(schema, os);

		os.write(syncMarker);
	}

	private void writeBlock() throws IOException {
		os.write(BinaryRecordEncoder.encodeLong(recordsInCurrentBlock));
		os.write(BinaryRecordEncoder.encodeInt(currentBlock.size()));

		currentBlock.writeTo(os);
		currentBlock.reset();

		os.write(syncMarker);

		recordsInCurrentBlock = 0;
	}

	private byte[] generateSyncMarker() {
		byte[] syncMarker = new byte[16];
		Random random = new Random();
		random.nextBytes(syncMarker);
		return syncMarker;
	}
}
