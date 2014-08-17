package lame.schema;

public class Field {
	public static enum Type {
		RECORD(1),
		STRING(2),
		INT(3),
		ARRAY(4),
		;

		private final int code;

		private Type(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static Type fromCode(int code) {
			for (Type type: values()) {
				if (type.code == code) {
					return type;
				}
			}

			throw new RuntimeException("Code does not correspond to a Type: " + code);
		}
	}

	public static class InvalidFieldException extends RuntimeException {
		public InvalidFieldException(String message) {
			super(message);
		}
	}

	final private String name;
	private final Type type;

	public Field(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public Object getDefaultValue() {
		return null;
	}

	public Type getType() {
		return type;
	}
}
