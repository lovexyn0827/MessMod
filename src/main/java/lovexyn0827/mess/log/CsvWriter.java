package lovexyn0827.mess.log;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringEscapeUtils;

public class CsvWriter implements AutoCloseable, Flushable {
	private Writer writer;

	private CsvWriter(Builder b, Writer writer) {
		this.writer = writer;
		try {
			this.writer.write(toLine(b.headers.stream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String toLine(Stream<?> line) {
		return line.map((obj) -> obj == null ? "[NULL]" : obj.toString())
				.map(StringEscapeUtils::escapeCsv)
				.collect(Collectors.joining(",")) + "\r\n";
	}

	public void close() throws IOException {
		this.writer.flush();
		this.writer.close();
	}

	public void flush() throws IOException {
		this.writer.flush();
	}
	
	public void println(Object... data) {
		try {
			this.writer.write(toLine(Arrays.stream(data)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class Builder {
		private List<String> headers = new ArrayList<>();
		
		public Builder addColumn(String header) {
			this.headers.add(header);
			return this;
		}
		
		public CsvWriter build(Writer writer) {
			return new CsvWriter(this, writer);
		}
	}
}
