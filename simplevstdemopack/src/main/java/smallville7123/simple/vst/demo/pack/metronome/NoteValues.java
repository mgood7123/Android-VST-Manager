package smallville7123.simple.vst.demo.pack.metronome;

public enum NoteValues{
	four("4");
	
	private String noteValue;

	NoteValues(String noteValue) {
		this.noteValue = noteValue;
	}
	
	@Override public String toString() {
	    return noteValue;
	}
}
