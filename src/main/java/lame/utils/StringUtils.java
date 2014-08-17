package lame.utils;

import java.util.Iterator;

public class StringUtils {

	public static String join(Iterable<?> list, String separator) {
		Iterator<?> iterator = list.iterator();
		if (!iterator.hasNext()) {
			return "";
		}

		StringBuilder s = new StringBuilder();
		s.append(iterator.next());

		while (iterator.hasNext()) {
			s.append(separator);
			s.append(iterator.next());
		}

		return s.toString();
	}
}
