package lame.data;

import lame.schema.RecordField;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BinaryDataFileReader implements Iterable<Record> {
	private final InputStream input;
	private final BlockCodec blockCodec;
	private RecordDecoder recordDecoder;
	private byte[] syncMarker;

	private long currentBlockRecordCount;

	private long recordsSeenSoFar;

	private long blocksRead;

	private InputStream currentBlock;

	public BinaryDataFileReader(InputStream input) throws IOException {
		this.input = input;

		BinarySchemaDecoder schemaDecoder = new BinarySchemaDecoder();
		RecordField schema = schemaDecoder.decode(input);

		recordDecoder = new BinaryRecordDecoder(schema);

		blockCodec = readBlockCodec(input);
		syncMarker = readSyncMarker(input);

		blocksRead = 0;
		nextBlock();
	}

	public Record read() throws IOException {
		if (recordsSeenSoFar == currentBlockRecordCount) {
			nextBlock();
		} else if (recordsSeenSoFar > currentBlockRecordCount) {
			throw new RuntimeException("Read more records than there should have been!");
		}

		Record record = recordDecoder.decode(currentBlock);
		if (record != null) {
			recordsSeenSoFar++;
		}

		return record;
	}

	public long getBlocksRead() {
		return blocksRead;
	}

	@Override
	public Iterator<Record> iterator() {
		try {
			return new Iterator<Record>() {
				private Record nextRecord = read();

				@Override
				public boolean hasNext() {
					return nextRecord != null;
				}

				@Override
				public Record next() {
					Record record = nextRecord;
					try {
						nextRecord = read();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return record;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void nextBlock() throws IOException {
		Long nextBlockRecordCount = readBlockRecordCount(input);
		if (nextBlockRecordCount != null) {
			currentBlockRecordCount = nextBlockRecordCount;

			int blockSize = readBlockSize(input);
			currentBlock = blockCodec.decode(input, blockSize);

			validateSyncMarker();

			blocksRead++;
		} else {
			currentBlockRecordCount = 0;
		}

		recordsSeenSoFar = 0;
	}

	private BlockCodec readBlockCodec(InputStream is) throws IOException {
		String codecClassName = BinaryRecordDecoder.readString(is);

		try {
			Class codecClass = Class.forName(codecClassName);
			return (BlockCodec) codecClass.newInstance();

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not find block codec class: " + codecClassName, e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Error instantiating block codec: " + codecClassName, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error instantiating block codec: " + e.getMessage(), e);
		}
	}

	private byte[] readSyncMarker(InputStream is) throws IOException {
		byte[] syncMarker = new byte[16];
		int bytesRead = is.read(syncMarker, 0, 16);

		if (bytesRead != syncMarker.length) {
			throw new RuntimeException("Could not read sync marker");
		}

		return syncMarker;
	}

	private void validateSyncMarker() throws IOException {
		byte[] blockSyncMarker = readSyncMarker(input);
		for (int i = 0; i < syncMarker.length; i++) {
			if (blockSyncMarker[i] != syncMarker[i]) {
				throw new RuntimeException("Invalid block sync marker");
			}
		}
	}

	private Long readBlockRecordCount(InputStream is) throws IOException {
		return BinaryRecordDecoder.tryToReadLong(is);
	}

	private int readBlockSize(InputStream is) throws IOException{
		return BinaryRecordDecoder.readInt(is);
	}
}
