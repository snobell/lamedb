package lame.test.schema

import lame.utils.StringUtils

class StringUtilsTest extends GroovyTestCase {
	void testEmptyList() {
		assert StringUtils.join([], ',') == ''
	}

	void testSingleElementList() {
		assert StringUtils.join(["flower"], ',') == 'flower'
	}

	void testTwoElementList() {
		assert StringUtils.join(["flower", "water"], ", ") == "flower, water"
	}

	void testMultipleElementList() {
		assert StringUtils.join(["flower", "water", "sugar"], "--") == "flower--water--sugar"
	}

	void testListWithNullItem() {
		assert StringUtils.join(["flower", null, "tree"], ', ') == "flower, null, tree"
	}

	void testWithIterable() {
		assert StringUtils.join(["one", "two", "three", "three"] as Set, ", ") == "one, two, three"
	}
}