package lame.test.data

import lame.data.BinarySchemaDecoder
import lame.data.BinarySchemaEncoder
import lame.schema.ArrayField
import lame.schema.IntField
import lame.schema.RecordField
import lame.schema.StringField

class BinarySchemaEncoderDecoderTest extends GroovyTestCase {

	public void testEncoderCanBeDecodedByDecoder() {
		RecordField addressField = new RecordField.Builder()
				.setName("address")
				.addField(new StringField("street"))
				.addField(new StringField("suburb"))
				.addField(new StringField("state"))
				.addField(new StringField("postcode"))
				.build();

		RecordField phoneNumberField = new RecordField.Builder()
				.setName("phoneNumber")
				.addField(new StringField("areaCode"))
				.addField(new StringField("number"))
				.build();

		RecordField schema = new RecordField.Builder()
				.setName("person")
				.addField(new StringField("givenName"))
				.addField(new StringField("surname"))
				.addField(new IntField("age"))
				.addField(addressField)
				.addField(new ArrayField("hobbies", new StringField("hobby")))
				.addField(new ArrayField("phoneNumbers", phoneNumberField))
				.build();

		BinarySchemaEncoder schemaEncoder = new BinarySchemaEncoder();
		ByteArrayOutputStream sos = new ByteArrayOutputStream();
		schemaEncoder.encode(schema, sos);

		BinarySchemaDecoder schemaDecoder = new BinarySchemaDecoder();
		ByteArrayInputStream sis = new ByteArrayInputStream(sos.toByteArray());

		RecordField decodedSchema = schemaDecoder.decode(sis);

		assert decodedSchema.name == "person"
		assert decodedSchema.toString() == schema.toString()
	}

	public void testByteFormat() {
		RecordField schema = new RecordField.Builder()
				.setName("phoneNumber")
				.addField(new StringField("areaCode"))
				.addField(new ArrayField("hobbies", new StringField("hobby")))
				.build();

		BinarySchemaEncoder schemaEncoder = new BinarySchemaEncoder();
		ByteArrayOutputStream sos = new ByteArrayOutputStream();
		schemaEncoder.encode(schema, sos);

		assert sos.toByteArray() == [
			0, 0, 0, 1,  // Record type
			0, 0, 0, 11, // Length of field name (phoneNumber)
			112, 104, 111, 110, 101, 78, 117, 109, 98, 101, 114, // Bytes of field name (phoneNumber)
			0, 0, 0, 2,  // Number of fields in record
			0, 0, 0, 2,  // Field type (String)
			0 ,0, 0, 8,  // Length of field name (areaCode)
			97, 114, 101, 97, 67, 111, 100, 101, // Bytes of field name (areaCode)
			0, 0, 0, 4,  // Field type (Array)
			0 ,0, 0, 7,  // Length of field name (hobbies)
			104, 111, 98, 98, 105, 101, 115, // Bytes of field name (hobbies)
			0, 0, 0, 2,  // Field type (String)
			0 ,0, 0, 5,  // Length of field name (hobby)
			104, 111, 98, 98, 121// Bytes of field name (hobby)
		] as byte[]
	}
}
