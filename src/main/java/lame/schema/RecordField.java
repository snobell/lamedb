package lame.schema;

import lame.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordField extends Field implements Iterable<Field> {
	private final Map<String, Field> fields;

	private RecordField(String name, Map<String, Field> fields) {
		super(name, Type.RECORD);
		this.fields = fields;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(getName());
		s.append("-schema(");

		List<String> fieldsToString = new ArrayList<String>(fields.size());
		for (Field field: fields.values()) {
			fieldsToString.add(field.toString());
		}

		s.append(StringUtils.join(fieldsToString, ", "));
		s.append(")");
		return s.toString();
	}

	public Set<String> getFieldSet() {
		return fields.keySet();
	}

	@Override
	public Iterator<Field> iterator() {
		return fields.values().iterator();
	}

	public int size() {
		return fields.size();
	}

	public static class Builder {
		String name = null;
		final Map<String, Field> fields = new LinkedHashMap<String, Field>();

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder addField(Field field) {
			fields.put(field.getName(), field);
			return this;
		}

		public RecordField build() {
			validate();
			return new RecordField(name, fields);
		}

		private void validate() {
			if (name == null || fields.size() == 0) {
				throw new RuntimeException("Invalid RecordField");
			}
		}
	}


}
