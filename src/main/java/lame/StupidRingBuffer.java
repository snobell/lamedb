package lame;

public class StupidRingBuffer {
	private int[] bytes;
	private int start;
	private int size;

	public StupidRingBuffer(int capacity) {
		bytes = new int[capacity];
		start = 0;
		size = 0;
	}

	public void add(int value) {
		bytes[start] = value;

		start = (start + 1) % bytes.length;

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

		start = (start + 1) % bytes.length;
		size--;
	}
}
