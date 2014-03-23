package lame.data;


import lame.schema.ArrayField;
import lame.schema.Field;
import lame.schema.IntField;
import lame.schema.RecordField;
import lame.schema.StringField;

import java.io.IOException;
import java.io.InputStream;

import static lame.data.BinaryRecordDecoder.readInt;
import static lame.data.BinaryRecordDecoder.readString;

public class BinarySchemaDecoder {

	public RecordField decode(InputStream is) throws IOException {
		Field.Type type = Field.Type.fromCode(readInt(is));
		if (type != Field.Type.RECORD) {
			throw new RuntimeException("Expected a record type but got: " + type.name());
		}

		return decodeRecord(is, readString(is));
	}

	private RecordField decodeRecord(InputStream is, String name) throws IOException {
		RecordField.Builder builder = new RecordField.Builder();
		builder.setName(name);

		int size = readInt(is);
		for (int i = 0; i < size; i++) {
			builder.addField(decodeField(is));
		}

		return builder.build();
	}

	private Field decodeField(InputStream is) throws IOException {
		Field.Type fieldType = Field.Type.fromCode(readInt(is));
		String fieldName = readString(is);

		switch (fieldType) {
			case RECORD:
				return decodeRecord(is, fieldName);
			case STRING:
				return new StringField(fieldName);
			case INT:
				return new IntField(fieldName);
			case ARRAY:
				return decodeArray(is, fieldName);
			default:
				throw new RuntimeException("Unknown field type: " + fieldType.name());
		}
	}

	private ArrayField decodeArray(InputStream is, String name) throws IOException {
		return new ArrayField(name, decodeField(is));
	}
}
