package lame.data;


import lame.schema.ArrayField;
import lame.schema.Field;
import lame.schema.RecordField;

import static lame.data.BinaryRecordEncoder.encodeInt;
import static lame.data.BinaryRecordEncoder.encodeString;

import java.io.IOException;
import java.io.OutputStream;

public class BinarySchemaEncoder {

	public void encode(RecordField schema, OutputStream os) throws IOException {
		encodeField(schema, os);
	}

	private void encodeRecord(RecordField recordField, OutputStream os) throws IOException {
		os.write(encodeInt(recordField.size()));

		for (Field field: recordField) {
			encodeField(field, os);
		}
	}

	private void encodeField(Field field, OutputStream os) throws IOException {
		os.write(encodeInt(field.getType().getCode()));
		encodeString(field.getName(), os);

		switch(field.getType()) {
			case RECORD:
				encodeRecord((RecordField) field, os);
				break;
			case ARRAY:
				encodeArray((ArrayField) field, os);
				break;
		}
	}

	private void encodeArray(ArrayField arrayField, OutputStream os) throws IOException {
		encodeField(arrayField.getElementType(), os);
	}
}
