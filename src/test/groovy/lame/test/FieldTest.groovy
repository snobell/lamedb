package lame.test

import lame.schema.ArrayField
import lame.schema.Field
import lame.schema.IntField
import lame.schema.StringField

class FieldTest extends GroovyTestCase {
	void testTypeFromCode() {
		assert Field.Type.fromCode(1) == Field.Type.RECORD
		assert Field.Type.fromCode(2) == Field.Type.STRING
		assert Field.Type.fromCode(3) == Field.Type.INT
		assert Field.Type.fromCode(4) == Field.Type.ARRAY
	}

	void testTypeFromInvalidCodeShouldFail() {
		shouldFail() {
			Field.Type.fromCode(-1)
		}
	}

	void testIntField() {
		IntField field = new IntField("score")

		assert field.type == Field.Type.INT
		assert field.name == "score"
		assert field.defaultValue == 0
	}

	void testStringField() {
		StringField field = new StringField("name")

		assert field.type == Field.Type.STRING
		assert field.name == "name"
	}

	void testArrayField() {
		IntField score = new IntField("score")
		ArrayField field = new ArrayField("scores", score)

		assert field.type == Field.Type.ARRAY
		assert field.elementType == score
		assert field.name == "scores"
	}
}
