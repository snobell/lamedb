package lame.test.data

import lame.data.BinaryRecordDecoder
import lame.data.BinaryRecordEncoder
import lame.data.Record
import lame.schema.ArrayField
import lame.schema.IntField
import lame.schema.RecordField
import lame.schema.StringField

class BinaryRecordEncoderDecoderTest extends GroovyTestCase {
	void testEncoderDecoder() {
		RecordField schema = new RecordField.Builder()
				.setName("person")
				.addField(new StringField("givenName"))
				.addField(new StringField("surname"))
				.addField(new IntField("age"))
				.addField(new ArrayField("hobbies", new StringField("hobby")))
				.build();

		Record record = new Record(schema);

		record.put("givenName", "Chris")
		record.put("surname", "Scobell")
		record.put("age", 27)
		record.put("hobbies", ["Fishing", "Cooking", "Skydiving", "Knitting"])

		BinaryRecordEncoder encoder = new BinaryRecordEncoder()

		ByteArrayOutputStream os = new ByteArrayOutputStream()
		encoder.encode(record, os)

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())

		BinaryRecordDecoder decoder = new BinaryRecordDecoder(schema)
		Record decodedRecord = decoder.decode(is)

		assert decodedRecord == record
	}

	void testBinaryFormat() {
		RecordField schema = new RecordField.Builder()
				.setName("person")
				.addField(new StringField("surname"))
				.addField(new IntField("age"))
				.addField(new ArrayField("hobbies", new StringField("hobby")))
				.build();

		Record record = new Record(schema);

		record.put("surname", "Scobell")
		record.put("age", 27)
		record.put("hobbies", ["Fishing", "Cooking", "Skydiving", "Knitting"])

		BinaryRecordEncoder encoder = new BinaryRecordEncoder()

		ByteArrayOutputStream os = new ByteArrayOutputStream()
		encoder.encode(record, os)

		assert os.toByteArray() == [
			0, 0, 0, 1, // Record code
			0, 0, 0, 2, // String code
			0, 0, 0, 7, // Lengh of string (Scobell)
			83, 99, 111, 98, 101, 108, 108, // Bytes of string (Scobell)
			0, 0, 0, 3, // Int code
			0, 0, 0, 27, // Value of int
			0, 0, 0, 4, // Array Code
			0, 0, 0, 4, // Length of Array
			0, 0, 0, 2, // Array type code (string)
			0, 0, 0, 7, // Length of first array element string (Fishing)
			70, 105, 115, 104, 105, 110, 103, // Bytes of string (Fishing)
			0, 0, 0, 2, // Array type code (string)
			0, 0, 0, 7, // Length of 2nd array element string (Cooking)
			67, 111, 111, 107, 105, 110, 103,  // Bytes of string (Cooking)
			0, 0, 0, 2, // Array type code (string)
			0, 0, 0, 9, // Length of 3rd array element string (Skydiving)
			83, 107, 121, 100, 105, 118, 105, 110, 103, // Bytes of skydiving
			0, 0, 0, 2, // Array type code (string)
			0, 0, 0, 8, // Length of 4th element of array (Knitting)
			75, 110, 105, 116, 116, 105, 110, 103 // Bytes of Kitting
		] as byte[]
	}
}
