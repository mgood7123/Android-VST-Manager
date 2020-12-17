package smallville7123.simple.vst.demo.pack.sequencer.pfseq;

/*
Relative time position.
When used for position, relative to beginning of musical bar.
When used for item length, relative to item clip start.

Fractional position corresponds to a "piano roll" user interface that looks like this:


        beginning of bar                                end of bar
        |                                               |
        beginning of 1st beat   beginning of 2nd beat   |
        |                       |                       |
        -------------------------------------------------
        |     |     |     |     |     |     |     |     |
        |     |     |     |     |     |     |    Pos    |
        |     |     |     |     |     |     |     |     |
        -------------------------------------------------

        assuming 2 beats per bar (time signature numerals can be set in the config)

        "Pos" in the above grid represents 1 3/4 beats, and would be defined like this:
        beats: 1
        mode: MODE_FRACTIONAL
        binaryDivisions: 4
        binaryPos: 3
        isTriplet: false


a grid with triplets might look like this:

WHAT THE USER EXPECTS:
       beginning of bar                                                        end of bar
       |                                                                       |
       beginning of 1st beat               beginning of 2nd beat               |
       |                                   |                                   |
       -------------------------------------------------------------------------
       |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |
       |  :  :  |  :  :  |  :  :  |  : Pos |  :  :  |  :  :  |  :  :  |  :  :  |
       |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |
       -------------------------------------------------------------------------

THE ACTUAL GRID
       beginning of bar                                                        end of bar
       |                                                                       |
       beginning of 1st beat               beginning of 2nd beat               |
       |                                   |                                   |
       |     beginning of bar              |                                   |     end of bar
       |     |                             |                                   |     |
       |     beginning of 1st beat         |     beginning of 2nd beat         |     |
       |     |                             |     |                             |     |
       -------------------------------------------------------------------------------
       |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :
       |  : PLAY|  :  :  |  :  :  |  : Pos |  :  :  |  :  :  |  :  :  |  :  :  |  :  :
       |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :  |  :  :
       -------------------------------------------------------------------------------

        beats: 0
        mode: MODE_FRACTIONAL
        binaryDivisions: 4
        binaryPos: 3
        isTriplet: true
        tripletPos: 2
 */

public class PFSeqTimeOffset {
    public static final int MODE_FRACTIONAL = 0;
    public static final int MODE_PERCENT = 1;

    private int beats; // 0-based.
    private int mode; // one of the PFSeqTimeOffset.MODE_ constants

    // only used if mode is PERCENT:
    private double percent; // between 0 and 1, inclusive

    // only used if mode is FRACTIONAL:
    private int binaryDivisions; // how many binary divisions you need in a beat. 1 or binary number like 2, 4, 8, 16, etc
    private int binaryPos; // 0-based. 0 for on-beat
    private boolean isTriplet;
    private int tripletPos; // 0-based. 0 for on the binary position. 1 for a third of the way between that and the next binary position, etc

    // private constructor. use static make method
    private PFSeqTimeOffset(int beats, int mode, double percentPos, int binaryDivisions, int binaryPos, boolean isTriplet, int tripletPos) {
        this.beats = beats;
        this.mode = mode;
        this.percent = percentPos;
        this.binaryDivisions = binaryDivisions;
        this.binaryPos = binaryPos;
        this.isTriplet = isTriplet;
        this.tripletPos = tripletPos;
    }
    public static smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset make(int beatOfBar, int mode, double percentPos, int binaryDivisions, int binaryPos, boolean isTriplet, int tripletPos) {
        if (mode == smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset.MODE_FRACTIONAL) {
            if ( !(binaryDivisions == 1 || binaryDivisions % 2 == 0 )
                    || binaryPos < 0
                    || binaryPos >= binaryDivisions) {
                return null;
            }
            if (isTriplet && (tripletPos > 2 || tripletPos < 0) ) {
                return null;
            }
        }
        if (mode == smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset.MODE_PERCENT) {
            if (percentPos < 0 || percentPos > 1) {
                return null;
            }
        }

        smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset timeOffset = new smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset( beatOfBar,  mode,  percentPos,  binaryDivisions,  binaryPos,  isTriplet,  tripletPos);

        return timeOffset;
    }

    /*
    get the time position as a percent (of the duration of the beat)
     */
    public double getPercent() {
        if (getMode() == smallville7123.simple.vst.demo.pack.sequencer.pfseq.PFSeqTimeOffset.MODE_PERCENT) {
            return percent;
        } else {
            double theReturn = ((double) getBinaryPos()) / getBinaryDivisions();
            if (isTriplet()) {
                return theReturn + ( 1 / ((double) getBinaryDivisions()) / 3 * ((double) getTripletPos()) );
            } else {
                return theReturn;
            }
        }
    }

    public int getBinaryDivisions() {
        return binaryDivisions;
    }
    public int getBinaryPos() {
        return binaryPos;
    }
    public boolean isTriplet() {
        return isTriplet;
    }
    public int getTripletPos() {
        return tripletPos;
    }
    public int getBeats() {
        return beats;
    }
    public int getMode() {
        return mode;
    }
}
