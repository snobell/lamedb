package lame.test.data

import lame.data.Record
import lame.schema.RecordField
import lame.schema.StringField

class RecordTest extends GroovyTestCase {
	RecordField schema
	Record record

	void setUp() {
		schema = new RecordField.Builder()
				.setName("person")
				.addField(new StringField("name"))
				.addField(new StringField("address"))
				.build()

		record = new Record(schema)
	}
	void testSetFieldsInRecord() {
		record.name = "Chris"
		record.address = "The Moon"

		assert record.name == "Chris"
		assert record.address == "The Moon"
	}

	void testKeySetShouldMatchFieldsInSchema() {
		assert record.getSchema() == schema
		assert record.keySet() == schema.fieldSet
	}

	void testValues() {
		record.name = "Chris"
		record.address = "The Moon"
		assert record.values() as Set == ['Chris', "The Moon"] as Set
	}

	void testAttemptingToSetFieldNotInSchema() {
		shouldFail(Record.FieldNotInRecord) {
			record.notAValidField = "eggs"
		}
	}

	void testAttemptingPutAllFromNonMatchingMap() {
		shouldFail() {
			record.putAll([does: "not match"])
		}
	}
}
