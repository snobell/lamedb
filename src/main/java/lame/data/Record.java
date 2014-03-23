package lame.data;

import lame.StringUtils;
import lame.schema.Field;
import lame.schema.RecordField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Record implements Map<String, Object> {
	private Map<String, Object> data;

	private final RecordField schema;

	public Record(RecordField schema) {
		this.data = new HashMap<String, Object>(schema.size());

		for (Field field: schema) {
			data.put(field.getName(), field.getDefaultValue());
		}
		this.schema = schema;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object field) {
		return data.containsKey(field);
	}

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public Object get(Object field) {
		return data.get(field);
	}

	@Override
	public Object put(String field, Object value) {
		if (!schema.getFieldSet().contains(field)) {
			throw new RuntimeException("field '" + field + "' is not in the Record's schema");
		}
		return data.put(field, value);
	}

	@Override
	public Object remove(Object o) {
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ?> map) {
		if (!map.keySet().equals(data.keySet())) {
			throw new RuntimeException("Map does not match record schema");
		}

		data.putAll(map);
	}

	@Override
	public void clear() {
		throw new RuntimeException("I'm sorry Dave, I can't let you do that");
	}

	@Override
	public Set<String> keySet() {
		return schema.getFieldSet();
	}

	@Override
	public Collection<Object> values() {
		return data.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return data.entrySet();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(schema.getName());
		s.append("(");

		List<String> fieldValues = new ArrayList<String>(data.size());
		for (Field field: schema) {
			fieldValues.add(field.getName() + "=" + data.get(field.getName()));
		}

		s.append(StringUtils.join(fieldValues, ", "));
		s.append(")");
		return s.toString();
	}

	public RecordField getSchema() {
		return schema;
	}
}
