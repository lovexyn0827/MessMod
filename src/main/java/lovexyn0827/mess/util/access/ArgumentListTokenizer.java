package lovexyn0827.mess.util.access;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.StringReader;

class ArgumentListTokenizer implements Iterable<String>, Iterator<String> {
	private final StringReader input;
	private boolean quoted = false;
	
	public ArgumentListTokenizer(String input) {
		this.input = new StringReader(input);
	}

	@Override
	public boolean hasNext() {
		return this.input.canRead();
	}

	@Override
	public String next() {
		StringReader in = this.input;
		StringBuilder sb = new StringBuilder();
		in.skipWhitespace();
		if(!in.canRead()) {
			throw new NoSuchElementException();
		}
		
		char c0 = in.peek();
		if(c0 == '\''){
			in.skip();
			this.quoted = true;
		}
		
		while(in.canRead()) {
			char c = in.read();
			switch(c) {
			case '\\':
				sb.append(this.readEscape());
				break;
			case '\'':
				if(this.quoted) {
					this.quoted = false;
				}
				
				in.skip();
				return sb.toString();
			case ',':
				if (this.quoted) {
					sb.append(c);
					break;
				} else {
					return sb.toString();
				}
			default:
				sb.append(c);
				break;
			}
		}
		
		if(this.quoted == true) {
			throw new IllegalStateException("Incomplete quote");
		}
		
		return sb.toString();
	}

	private String readEscape() {
		StringReader in = this.input;
		if(!in.canRead()) {
			throw new IllegalStateException("Incomplete escape");
		}
		
		char c = in.read();
		switch(c) {
		case 'b':
			return "\b";
		case 't':
			return "\t";
		case 'n':
			return "\n";
		case 'f':
			return "\f";
		case 'r':
			return "\r";
		case '"':
			return "\"";
		case '\'':
			return "'";
		case '\\':
			return "\\";
		case 'u':
			if(in.canRead(4)) {
				String hexStr = in.getString().substring(in.getCursor(), in.getCursor() + 4);
				return Character.valueOf((char) Integer.parseUnsignedInt(hexStr, 16)).toString();
			} else {
				throw new IllegalStateException("Incomplete Unicode character");
			}
		default :
			throw new IllegalStateException("Unsupported escape: " + c);
		}
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}
	
	public String[] toArray() {
		return Iterators.toArray(this, String.class);
	}
}
