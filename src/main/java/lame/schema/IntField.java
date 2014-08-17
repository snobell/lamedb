package lame.schema;

public class IntField extends Field {
	public IntField(String name) {
		super(name, Type.INT);
	}

	@Override
	public Object getDefaultValue() {
		return 0;
	}
}
