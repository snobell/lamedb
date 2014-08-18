package lame.data;

import lame.utils.StupidRingBuffer;
import lame.schema.RecordField;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BinaryDataFileReader implements Iterable<Record> {
	private final InputStream input;
	private final BlockCodec blockCodec;
	private final RecordField schema;
	private RecordDecoder recordDecoder;
	private int[] syncMarker;

	private long currentBlockRecordCount;

	private long recordsSeenSoFar;

	private long blocksRead;

	private InputStream currentBlock;

	public BinaryDataFileReader(InputStream input) throws IOException {
		this.input = input;

		BinarySchemaDecoder schemaDecoder = new BinarySchemaDecoder();
		schema = schemaDecoder.decode(input);

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

	public void skip(long offset) throws IOException {
		input.skip(offset);

		int nextByte = input.read();
		StupidRingBuffer inputBuffer = new StupidRingBuffer(16);

		while (nextByte != -1) {
			inputBuffer.add(nextByte);

			findOffsetOfNextSyncMarker(inputBuffer);

			if (inputBuffer.size() == syncMarker.length) {
				break;
			}

			nextByte = input.read();
		}
	}

	public void findOffsetOfNextSyncMarker(StupidRingBuffer inputBuffer) {
		int syncMarkerPos = 0;
		for (int i = 0; i < inputBuffer.size(); i++) {
			if (syncMarker[syncMarkerPos] == inputBuffer.get(i)) {
				syncMarkerPos++;
			} else {
				inputBuffer.dropFirst();
				findOffsetOfNextSyncMarker(inputBuffer);
			}
		}
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

	public RecordField getSchema() {
		return schema;
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

	private int[] readSyncMarker(InputStream is) throws IOException {
		int[] syncMarker = new int[16];
		for (int i = 0; i < syncMarker.length; i++) {
			int nextValue = is.read();
			if (nextValue == -1) {
				throw new RuntimeException("Could not read sync marker");
			}
			syncMarker[i] = nextValue;
		}

		return syncMarker;
	}

	private void validateSyncMarker() throws IOException {
		int[] blockSyncMarker = readSyncMarker(input);
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
