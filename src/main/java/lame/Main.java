package lame;

import lame.data.BinaryDataFileReader;
import lame.data.Record;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
	public static void main(String[] args) throws IOException, ParseException {
		CommandLineParser parser = new BasicParser();

		Options options = new Options();
		options.addOption(
			OptionBuilder.withArgName("file")
			             .hasArg()
			             .withDescription("Path to a data file")
			             .create("file"));
		options.addOption( "s", "schema", false, "display the schema of a datafile only" );
		options.addOption( "h", "help", false, "display this help text" );

		CommandLine line = parser.parse( options, args );

		// Display help.
		if(line.hasOption( "help") || !line.hasOption("file")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("tool", options);
			System.exit(0);
		}

		BinaryDataFileReader reader = getReader(line.getOptionValue("file"));

		if (line.hasOption("schema")) {
			displaySchema(reader);
		} else {
			displayData(reader);
		}
	}

	private static BinaryDataFileReader getReader(String fileName) throws IOException {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(fileName));
			return new BinaryDataFileReader(is);
		} catch (FileNotFoundException e) {
			System.err.println("Error: file not found \"" + fileName + "\"");
			System.exit(1);
		}

		return null;
	}

	private static void displaySchema(BinaryDataFileReader reader) throws IOException {
		System.out.println(reader.getSchema());
	}

	private static void displayData(BinaryDataFileReader reader) {
		for (Record r: reader) {
			System.out.println(r);
		}
	}
}