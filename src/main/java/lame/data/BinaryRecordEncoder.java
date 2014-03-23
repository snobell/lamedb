package lame.data;

import lame.schema.ArrayField;
import lame.schema.Field;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class BinaryRecordEncoder implements RecordEncoder {

	@Override
	public void encode(Record record, OutputStream os) throws IOException {
		os.write(encodeInt(Field.Type.RECORD.getCode()));

		encodeRecord(record, os);
	}

	private void encodeRecord(Record record, OutputStream os) throws IOException {
		for (Field field: record.getSchema()) {
			Object data = record.get(field.getName());
			encodeObject(field, data, os);
		}
	}

	private void encodeObject(Field field, Object data, OutputStream os) throws IOException {
		os.write(encodeInt(field.getType().getCode()));

		switch(field.getType()) {
			case RECORD:
				Record recordData = (Record) data;
				encodeRecord(recordData, os);
				break;
			case STRING:
				String stringData = (String) data;
				encodeString(stringData, os);
				break;
			case INT:
				int intData = (Integer) data;
				os.write(encodeInt(intData));
				break;
			case ARRAY:
				encodeArray((ArrayField) field, (List) data, os);
				break;
			default:
				throw new RuntimeException("Unhandled field type: " + field.getType().name());
		}
	}

	private void encodeArray(ArrayField arrayField, List data, OutputStream os) throws IOException {
		os.write(encodeInt(data.size()));

		for(Object o: data) {
			encodeObject(arrayField.getElementType(), o, os);
		}
	}

	public static void encodeString(String data, OutputStream os) throws IOException {
		try {
			byte[] stringBytes = data.getBytes("UTF-8");
			os.write(encodeInt(data.length()));
			os.write(stringBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] encodeInt(int intValue) {
		return ByteBuffer.allocate(4).putInt(intValue).array();
	}

	public static byte[] encodeLong(long longValue) {
		return ByteBuffer.allocate(8).putLong(longValue).array();
	}
}
