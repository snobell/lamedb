package lame.test.data

import lame.data.BinaryDataFileReader
import lame.data.BinaryDataFileWriter
import lame.data.Record
import lame.schema.ArrayField
import lame.schema.IntField
import lame.schema.RecordField
import lame.schema.StringField

class BinaryRecordEncoderDecoderTest extends GroovyTestCase {
	public void testDecoderCanDecodeDataFromEncoder() {
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

		Record addressRecord = new Record(addressField);
		addressRecord.put("street", "123 Fake St");
		addressRecord.put("suburb", "Melbourne");
		addressRecord.put("state", "VIC");
		addressRecord.put("postcode", "3000");

		Record record = new Record(schema);

		record.put("givenName", "Chris");
		record.put("surname", "Scobell");
		record.put("age", 27);
		record.put("address", addressRecord);
		record.put("hobbies", ["Fishing", "Cooking", "Skydiving", "Knitting"]);

		Record phoneNumber1 = new Record(phoneNumberField);
		phoneNumber1.put("areaCode", "+61");
		phoneNumber1.put("number", "62924035");

		Record phoneNumber2 = new Record(phoneNumberField);
		phoneNumber2.put("areaCode", "+61");
		phoneNumber2.put("number", "49560930");

		record.put("phoneNumbers", [phoneNumber1, phoneNumber2]);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BinaryDataFileWriter writer = new BinaryDataFileWriter(schema, os);

		writer.write(record)
		writer.close()

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		BinaryDataFileReader reader = new BinaryDataFileReader(is);

		Record decodedRecord = reader.read()

		assert decodedRecord == record
	}
}
