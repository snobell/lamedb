package lame.test.example;

import lame.data.BinaryDataFileReader;
import lame.data.BinaryDataFileWriter;
import lame.data.BinarySchemaDecoder;
import lame.data.BinarySchemaEncoder;
import lame.data.Record;
import lame.schema.ArrayField;
import lame.schema.IntField;
import lame.schema.RecordField;
import lame.schema.StringField;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class StressTest {
	public static void main(String[] args) throws IOException {
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

		System.out.println(schema.toString());

		BinarySchemaEncoder schemaEncoder = new BinarySchemaEncoder();
		ByteArrayOutputStream sos = new ByteArrayOutputStream();
		schemaEncoder.encode(schema, sos);

		BinarySchemaDecoder schemaDecoder = new BinarySchemaDecoder();
		ByteArrayInputStream sis = new ByteArrayInputStream(sos.toByteArray());

		RecordField decodedSchema = schemaDecoder.decode(sis);
		System.out.println(decodedSchema.toString());


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
		record.put("hobbies", Arrays.asList("Fishing", "Cooking", "Skydiving", "Knitting"));

		Record phoneNumber1 = new Record(phoneNumberField);
		phoneNumber1.put("areaCode", "+61");
		phoneNumber1.put("number", "62924035");

		Record phoneNumber2 = new Record(phoneNumberField);
		phoneNumber2.put("areaCode", "+61");
		phoneNumber2.put("number", "49560930");

		record.put("phoneNumbers", Arrays.asList(phoneNumber1, phoneNumber2));


		OutputStream os = new BufferedOutputStream(new FileOutputStream("test.out"));

		BinaryDataFileWriter writer = new BinaryDataFileWriter(schema, os);

		System.out.println("Writing 999999 Records");
		for (int i = 0; i < 999999; i++) {
			writer.write(record);
		}

		writer.close();
		System.out.println("Finished Writing 999999 Records");

		InputStream is = new BufferedInputStream(new FileInputStream("test.out"));
		BinaryDataFileReader reader = new BinaryDataFileReader(is);

		reader.skip(200);

		System.out.println("Decoded Records:");

		for (Record decodedRecord: reader) {
			// Consume all the records
		}

		System.out.println("Read " + reader.getBlocksRead() + " blocks.");
	}
}