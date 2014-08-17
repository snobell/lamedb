package lame.test.schema

import lame.schema.Field
import lame.schema.RecordField
import lame.schema.StringField

class RecordFieldTest extends GroovyTestCase {
	void testRecordFieldBuilder() {
		RecordField addressField = new RecordField.Builder()
				.setName("address")
				.addField(new StringField("street"))
				.addField(new StringField("suburb"))
				.addField(new StringField("state"))
				.addField(new StringField("postcode"))
				.build();

		assert addressField.name == "address"
		assert addressField.type == Field.Type.RECORD
		assert addressField.collect { it.name } == ["street", "suburb", "state", "postcode"]
		assert addressField.size() == 4
	}

	void testNestedRecordFields() {
		RecordField phoneNumberField = new RecordField.Builder()
				.setName("phoneNumber")
				.addField(new StringField("areaCode"))
				.addField(new StringField("number"))
				.build();

		RecordField schema = new RecordField.Builder()
				.setName("person")
				.addField(new StringField("givenName"))
				.addField(new StringField("surname"))
				.addField(phoneNumberField)
				.build();

		boolean foundPhoneNumber = false
		schema.each { Field f ->
			if (f.name == "phoneNumber") {
				assert f.collect { it.name } == ["areaCode", "number"]
				foundPhoneNumber = true
			}
		}

		assert foundPhoneNumber
	}

	void testFieldSet() {
		RecordField addressField = new RecordField.Builder()
				.setName("address")
				.addField(new StringField("street"))
				.addField(new StringField("suburb"))
				.addField(new StringField("state"))
				.addField(new StringField("postcode"))
				.build();

		assert addressField.fieldSet == ["street", "suburb", "state", "postcode"] as Set
	}

	void testRecordFieldIsInvalidIfMissingName() {
		shouldFail(Field.InvalidFieldException) {
			new RecordField.Builder()
					.addField(new StringField("areaCode"))
					.addField(new StringField("number"))
					.build();
		}
	}

	void testRecordFieldIsInvalidIfMissingFields() {
		shouldFail(Field.InvalidFieldException) {
			new RecordField.Builder()
					.setName("phoneNumber")
					.build();
		}
	}
}
