package batfish.util;

public class SubRange {
	private int _start;
	private int _end;
	
	public SubRange(int start, int end) {
		_start = start;
		_end = end;
	}
	
	public int getStart() {
		return _start;
	}
	
	public int getEnd() {
		return _end;
	}
	
	@Override
	public String toString() {
	   return "[" + _start + "," + _end + "]";
	}
}
