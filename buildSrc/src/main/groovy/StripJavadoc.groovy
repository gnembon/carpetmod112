
class StripJavadoc extends FilterReader {
	StripJavadoc() {
	}
	StripJavadoc(Reader reader) {
		super(new BufferedReader(reader))
	}
	
	private String line = ''
	private Deque<Integer> lookahead = new ArrayDeque<>()
	private boolean inJavadoc = false
	
	int read() {
		while (lookahead.isEmpty()) {
			int c
			line = ''
			while (true) {
				c = super.read()
				if (c == -1) {
					if (lookahead.isEmpty())
						return -1
					else
						break
				}
				if (c == ('\n' as char)) {
					lookahead.add('\n' as char)
					break
				}
				lookahead.add(c)
				line += c as char
			}
		}
		
		if (inJavadoc) {
			if (line.contains('*/'))
				inJavadoc = false
			lookahead.clear()
			return read()
		}
		if (line.contains('/**')) {
			if (!line.contains('*/'))
				inJavadoc = true
			lookahead.clear()
			return read()
		}
		return lookahead.remove()
	}
	
	int read(char[] cbuf, int off, int len) {
		int i
		for (i = 0; i < len && off + i < cbuf.length; i++) {
			int c = read()
			if (c == -1)
				return i == 0 ? -1 : i
			cbuf[off + i] = c
		}
		return i
	}
}
