# LameDB

LameDB isn't really a database it's more of a record based binary data serialization library.

LameDB stores its data in "DataFiles" which consist of a header followed by an arbitrary number of "blocks". Each
block contains a set of binary encoded records. The blocks are by default compressed with deflate.

## DataFile Header
The header of a DataFile contains the schema of the records in the file (encoded in binary), followed by a string
naming the codec that was used to encode the blocks, and ends with a 16 byte random "sync marker".

LameDB DataFiles are split-able by virtue of the sync marker: the sync marker is output after every block and allows
the start of the next block to be found after a random seek anywhere in the DataFile.

## Record Schemas
The structure of the records is defined using schema objects. At the moment LameDB supports the following "Field" types
in the schema format:
* RecordField --- Basically a container for other fields
* StringField --- A Utf8 String
* IntField --- An Int64
* ArrayField --- Allows any Schema Field to be the type of the array.

## Record
Records decoded from a DataFile are put into "Record" instances - the Record class implements the Map interface to
allow the fields from the record to be accessed. Record instances are bound to a RecordField schema which enforces
(at runtime) that only fields defined in the schema can be accessed or modified.

## Encoders
The seralization process supports pluggable encoders/decoders for Records, Blocks and Schemas.
By default the "BinaryDataFileWriter" and "BinaryDataFileReader" will use the "BinaryRecordEncoder/Decoder" and the
"BinarySchemaEncoder/Decoder". While the default block codec used is "DeflateBlockCodec".

The BinaryRecordEncoder encodes the fields of a record in the order that they appear in the schema. It does not
output the name of any field, so the encoded data needs to be interpreted alongside the schema decoded from the
DataFile header to figure out what field is what.

Types are encoded as follows:
* String
** 4 byte type code (2 for String)
** 4 byte string length
** The bytes of the string encoded in UTF8
* Int
** 4 byte type code (3 for Int)
** 4 bytes of Int (in the current byte order!)
*

## Commandline Tool
Lamedb has a simple commandline tool that can be used to cat the contents of a DataFile to stdout or to display the 
schema of the file.

```
usage: tool
 -file <file>   Path to a data file
 -h,--help      display this help text
 -s,--schema    display the schema of a datafile only
```

## Examples

Define a simple record schema:
```
RecordField addressField = new RecordField.Builder()
		.setName("address")
		.addField(new StringField("street"))
		.addField(new StringField("suburb"))
		.addField(new StringField("state"))
		.addField(new StringField("postcode"))
		.build();
```

Define a nested record schema:
```
RecordField addressField = new RecordField.Builder()
		.setName("address")
		.addField(new StringField("street"))
		.addField(new StringField("suburb"))
		.addField(new StringField("state"))
		.addField(new StringField("postcode"))
		.build();

RecordField phoneNumberField = new RecordField.Builder()
		.setName("phoneNumber")
		.addField(new StringField("areaCode"))
		.addField(new StringField("number"))
		.build();

RecordField schema = new RecordField.Builder()
		.setName("person")
		.addField(new StringField("givenName"))
		.addField(new StringField("surname"))
		.addField(new IntField("age"))
		.addField(addressField)
		.addField(new ArrayField("hobbies", new StringField("hobby")))
		.addField(new ArrayField("phoneNumbers", phoneNumberField))
		.build();
```

Create a record using the schema:
```
Record addressRecord = new Record(addressField);
addressRecord.put("street", "123 Fake St");
addressRecord.put("suburb", "Melbourne");
addressRecord.put("state", "VIC");
addressRecord.put("postcode", "3000");

Record record = new Record(schema);

record.put("givenName", "Chris");
record.put("surname", "Scobell");
record.put("age", 27);
record.put("address", addressRecord);
record.put("hobbies", Arrays.asList("Fishing", "Cooking", "Skydiving", "Knitting"));

Record phoneNumber1 = new Record(phoneNumberField);
phoneNumber1.put("areaCode", "+61");
phoneNumber1.put("number", "62924035");

Record phoneNumber2 = new Record(phoneNumberField);
phoneNumber2.put("areaCode", "+61");
phoneNumber2.put("number", "49560930");

record.put("phoneNumbers", Arrays.asList(phoneNumber1, phoneNumber2));
```

Write a single record out to a DataFile:
```
OutputStream os = new BufferedOutputStream(new FileOutputStream("test.out"));

BinaryDataFileWriter writer = new BinaryDataFileWriter(schema, os);

writer.write(record);

writer.close();
```

Read records from a DataFile (starting 200 bytes in):
```
InputStream is = new BufferedInputStream(new FileInputStream("test.out"));
BinaryDataFileReader reader = new BinaryDataFileReader(is);

// Jump ahead 200 bytes
reader.skip(200);

for (Record decodedRecord: reader) {
	System.out.println(decodedRecord);
}
```

## What next?
Things I want to try out in the future:

* Indexes (So it's more like an actual database!)
* Column store encodings
* Support for more types
* More efficient encodings of Ints
* Projection of one Record to another
* Queries (Also more like a DB!)