package lame.data;

import lame.schema.ArrayField;
import lame.schema.Field;
import lame.schema.RecordField;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryRecordDecoder implements RecordDecoder {
	private final RecordField schema;

	public BinaryRecordDecoder(RecordField schema) {
		this.schema = schema;
	}

	@Override
	public Record decode(InputStream is) throws IOException {
		// Do we have another record to read?
		Integer typeCode = tryToReadInt(is);
		if (typeCode == null) {
			return null;
		}

		Field.Type recordType = Field.Type.fromCode(typeCode);
		if (recordType != Field.Type.RECORD) {
			throw new RuntimeException("Expecting a Record but got a: " + recordType.name());
		}

		return decodeRecord(is, schema);
	}

	public Record decodeRecord(InputStream is, RecordField schema) throws IOException {
		Record record = new Record(schema);

		for (Field field: schema) {
			record.put(field.getName(), decodeObject(field, is));
		}

		return record;
	}

	private Object decodeObject(Field field, InputStream is) throws IOException {
		Field.Type actualType = Field.Type.fromCode(readInt(is));
		if (field.getType() != actualType) {
			throw new RuntimeException("Expecting a '" + field.getType() + "' but got a '" + actualType + "'");
		}

		switch(field.getType()) {
			case RECORD:
				return decodeRecord(is, (RecordField) field);
			case STRING:
				return readString(is);
			case INT:
				return readInt(is);
			case ARRAY:
				return decodeArray((ArrayField) field, is);
			default:
				throw new RuntimeException("Unhandled field type: " + field.getType().name());
		}
	}

	private List<Object> decodeArray(ArrayField arrayField, InputStream is) throws IOException {
		int size = readInt(is);
		List<Object> data = new ArrayList<Object>(size);

		for (int i = 0; i < size; i++) {
			data.add(decodeObject(arrayField.getElementType(), is));
		}

		return data;
	}

	public static String readString(InputStream is) throws IOException {
		int size = readInt(is);
		byte[] stringBytes = new byte[size];
		int bytesRead = is.read(stringBytes, 0, size);

		if (bytesRead == -1) {
			throw new EOFException();
		}

		return new String(stringBytes, "UTF-8");
	}

	public static int readInt(InputStream is) throws IOException {
		return readBytes(is, 4).getInt();
	}

	public static Integer tryToReadInt(InputStream is) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		int bytesRead = is.read(buffer.array(), 0, 4);

		if (bytesRead == -1) {
			return null;
		}

		return buffer.getInt();
	}

	public static long readLong(InputStream is) throws IOException {
		return readBytes(is, 8).getLong();
	}

	public static Long tryToReadLong(InputStream is) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		int bytesRead = is.read(buffer.array(), 0, 8);

		if (bytesRead == -1) {
			return null;
		}

		return buffer.getLong();
	}

	private static ByteBuffer readBytes(InputStream is, int bytesToRead) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);
		int bytesRead = is.read(buffer.array(), 0, bytesToRead);

		if (bytesRead == -1) {
			throw new EOFException();
		}
		return buffer;
	}
}
