package lame.test

import lame.StupidRingBuffer

class StupidRingBufferTest extends GroovyTestCase {
	StupidRingBuffer buffer
	void setUp() {
		buffer = new StupidRingBuffer(3)
	}

	void testAddAndRetrieveUpToCapacity() {
		buffer.add(1)
		buffer.add(2)
		buffer.add(3)

		assert buffer.size() == 3
		assert buffer.get(0) == 1
		assert buffer.get(1) == 2
		assert buffer.get(2) == 3
	}

	void testAddAndRetrieveOverCapacity() {
		buffer.add(1)
		buffer.add(2)
		buffer.add(3)
		buffer.add(4)

		assert buffer.size() == 3
		assert buffer.get(0) == 2
		assert buffer.get(1) == 3
		assert buffer.get(2) == 4
	}

	void testSize() {
		assert buffer.size() == 0

		buffer.add(1)

		assert buffer.size() == 1
	}

	void testRetrieveElementOutOfBoundsInEmptyBuffer() {
		shouldFail (IndexOutOfBoundsException) {
			buffer.get(0)
		}
	}

	void testRetrieveElementOutOfBounds() {
		buffer.add(1)
		buffer.add(2)

		shouldFail (IndexOutOfBoundsException) {
			buffer.get(2)
		}

		shouldFail (IndexOutOfBoundsException) {
			buffer.get(-1)
		}
	}

	void testDropFirstElement() {
		buffer.add(1)
		buffer.add(2)
		buffer.add(3)

		buffer.dropFirst()

		assert buffer.size() == 2
		assert buffer.get(0) == 2
		assert buffer.get(1) == 3
	}

	void testDropFirstElementOfEmptyBufferShouldFail() {
		shouldFail(IndexOutOfBoundsException) {
			buffer.dropFirst()
		}
	}
}
