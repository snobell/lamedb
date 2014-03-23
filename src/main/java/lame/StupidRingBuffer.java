package lame;

public class StupidRingBuffer {
	private int[] bytes;
	private int start;
	private int end;
	private int size;

	public StupidRingBuffer(int capacity) {
		bytes = new int[capacity];
		start = 0;
		end = 0;
		size = 0;
	}

	public void add(int value) {
		bytes[end] = value;

		if (end == start && size == bytes.length) {
			start = (start + 1) % bytes.length;
		}

		end = (end + 1) % bytes.length;

		if (size < bytes.length) {
			size++;
		}
	}

	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}

		int offset = (start + index) % bytes.length;
		return bytes[offset];
	}

	public int size() {
		return size;
	}

	public void dropFirst() {
		if (size == 0) {
			throw new IndexOutOfBoundsException();
		}

		// x x x
		// End = 0
		// Start = 0

		start = (start + 1) % bytes.length;
		size--;
	}
}
