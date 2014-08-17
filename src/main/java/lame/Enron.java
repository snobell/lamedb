package lame;

import lame.email.EmailParser;
import lame.schema.RecordField;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Enron {
	public static void main(String[] args) throws IOException {


//		EmailParser p = new EmailParser(Paths.get("/Users/chris/Downloads/enron_mail_20110402/maildir/bass-e/inbox/7."));
//		p.read();
//		System.out.println(p.getRecord());
//		System.exit(0);

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isDirectory(entry);
			}
		};

		DirectoryStream<Path> employeeMail = Files.newDirectoryStream(Paths.get(args[0]), filter);


		for (Path child: employeeMail) {
			String employee = child.getFileName().toString();

			DirectoryStream<Path> mail = Files.newDirectoryStream(child, filter);
			for (Path mailDirectory: mail) {

				DirectoryStream<Path> mailFiles = Files.newDirectoryStream(mailDirectory);
				for (Path mailFile: mailFiles) {
					if (Files.isRegularFile(mailFile)) {
						System.out.println("\nEMAIL FILE IS        " + employee + " " + mailDirectory.getFileName() + " " + mailFile.getFileName());
						EmailParser parser = new EmailParser(mailFile);
						parser.read();

						System.out.println(parser.getRecord());
					}
				}
			}
		}


	}




}
