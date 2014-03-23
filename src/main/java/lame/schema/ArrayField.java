package lame.schema;

public class ArrayField extends Field {
	private final Field elementType;

	public ArrayField(String name, Field elementType) {
		super(name, Type.ARRAY);
		this.elementType = elementType;
	}

	public Field getElementType() {
		return elementType;
	}
}
