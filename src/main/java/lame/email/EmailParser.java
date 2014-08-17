package lame.email;

import lame.data.Record;
import lame.schema.RecordField;
import lame.schema.StringField;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class EmailParser {
	private Path mailFilePath;
	private Record record;

	private String headerName;

	private static enum State {
		LOOKING_FOR_HEADER_NAME,
		LOOKING_FOR_HEADER_VALUE,
		NEW_LINE_IN_VALUE,
		LOOKING_FOR_BODY,
	}

	public EmailParser(Path mailFilePath) {
		this.record = new Record(makeSchema());
		this.mailFilePath = mailFilePath;
	}

	public void read() throws IOException {
		Charset cs = Charset.defaultCharset();

		BufferedReader reader = null;

		headerName = null;

		try {
			reader = Files.newBufferedReader(mailFilePath, cs);

			State currentState = State.LOOKING_FOR_HEADER_NAME;

			StringBuilder headerNameBuffer = new StringBuilder();
			StringBuilder headerValueBuffer = new StringBuilder();

			String line;
			while((line = reader.readLine()) != null) {
				int charsProcessed = 0;
				for (int i = 0; i < line.length(); i++) {
					char nextChar = line.charAt(i);

					if (nextChar == '\r') {
						continue;
					}

					//System.out.print(nextChar);

					switch (currentState) {
						case LOOKING_FOR_HEADER_NAME:
							currentState = processHeaderName(headerNameBuffer, headerValueBuffer, nextChar);
							break;
						case LOOKING_FOR_HEADER_VALUE:
							currentState = processHeaderValue(headerValueBuffer, nextChar);
							break;
						case LOOKING_FOR_BODY:
							processBody(headerValueBuffer, nextChar);
							break;
					}
					charsProcessed++;
				}

				//System.out.println();

				if (charsProcessed == 0) {
					emitHeaderValuePair(headerValueBuffer);
					currentState = State.LOOKING_FOR_BODY;
				} else if (currentState == State.LOOKING_FOR_HEADER_NAME) {
					System.out.println("We never found something that looks like a header so assume it's the previous header's value");
					headerValueBuffer.append(headerNameBuffer);
					headerNameBuffer.delete(0, headerNameBuffer.length());
				} else if (currentState == State.LOOKING_FOR_BODY) {
					processBody(headerValueBuffer, '\n');
				} else {
					currentState = State.LOOKING_FOR_HEADER_NAME;
				}
			}

			record.put("Body", headerValueBuffer.toString());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private State processHeaderName(StringBuilder headerNameBuffer, StringBuilder headerValueBuffer, char nextChar) {
		if (nextChar == ':') {
			// If there was a previous header:value pair now is the time to emit it
			if (headerName != null) {
				emitHeaderValuePair(headerValueBuffer);
			}

			headerName = headerNameBuffer.toString();
			headerNameBuffer.delete(0, headerNameBuffer.length());

			return State.LOOKING_FOR_HEADER_VALUE;
		}

		headerNameBuffer.append(nextChar);
		return State.LOOKING_FOR_HEADER_NAME;
	}

	private State processHeaderValue(StringBuilder headerValueBuffer, char nextChar) {
		headerValueBuffer.append(nextChar);
		return State.LOOKING_FOR_HEADER_VALUE;
	}

	private void processBody(StringBuilder buffer, char nextChar) {
		buffer.append(nextChar);
	}

	private void emitHeaderValuePair(StringBuilder headerValueBuffer) {
		String headerValue = headerValueBuffer.toString();
		record.put(headerName, headerValue);
		headerValueBuffer.delete(0, headerValueBuffer.length());
	}

	private RecordField makeSchema() {
		return new RecordField.Builder()
				.setName("Email")
				.addField(new StringField("Message-ID"))
				.addField(new StringField("Date"))
				.addField(new StringField("From"))
				.addField(new StringField("To"))
				.addField(new StringField("Cc"))
				.addField(new StringField("Bcc"))
				.addField(new StringField("Subject"))
				.addField(new StringField("Mime-Version"))
				.addField(new StringField("Content-Type"))
				.addField(new StringField("charset"))
				.addField(new StringField("Content-Transfer-Encoding"))
				.addField(new StringField("X-From"))
				.addField(new StringField("X-To"))
				.addField(new StringField("X-cc"))
				.addField(new StringField("X-bcc"))
				.addField(new StringField("X-Folder"))
				.addField(new StringField("X-Origin"))
				.addField(new StringField("X-FileName"))
				.addField(new StringField("Body"))
				.build();
	}

	public Record getRecord() {
		return record;
	}
}
