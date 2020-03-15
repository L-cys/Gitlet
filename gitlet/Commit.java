package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * A class to tracked all the files,
 * and record the date and log message of one Commit.
 *
 * @author chenyuanshan
 */

public class Commit implements Serializable {


    /**
     * The current SHA1 value of me.
     */
    String _Sha_1;
    /**
     * Date message of me.
     */
    String _date;
    /**
     * The message the users give.
     */
    String _logMSG;
    /**
     * Parent SHA1 to track.
     */
    String _parentSHA1;
    /**
     * The files of current commit.
     */
    HashMap<String, Blob> _currentBlob;
    /**
     * True if this commit is for the merging.
     */
    boolean _merged = false;
    /**
     * Merge parent a's SHA1.
     */
    String parentA_sha1 = null;
    /**
     * Merge parent b's SHA1.
     */
    String parentB_sha1 = null;
    /**
     * All the files that have been removed.
     */
    HashMap<String, Blob> _removedfiles;

    /**
     * Nineteen.
     */
    private int nineteen = 19;

    /**
     * Twentyfour.
     */
    private int twentyfour = 24;

    /**
     * @param logMSG      the log message of this commit.
     * @param currentBlob to track current blob of this commit.
     * @param parentSHA1  The way to track parents SHA1 value
     */
    public Commit(String logMSG,
                  HashMap<String, Blob> currentBlob, String parentSHA1) {
        _logMSG = logMSG;
        _currentBlob = currentBlob;
        _parentSHA1 = parentSHA1;

        Date dNow = new Date();
        String d = dNow.toString();
        _date = d.substring(0, nineteen) + " " + d.substring(twentyfour) + " -0800";
        if (logMSG.equals("initial commit")) {
            _date = "Wed Dec 31 16:00:00 1969 -0800";
        }
        _removedfiles = new HashMap<>();

    }

    /**
     * Calculate the SHA1 value of this commit.
     *
     * @return _Sha_1.
     */
    public String getSha1() {
        String s = "";
        for (Blob bb : _currentBlob.values()) {
            s = s + bb.toString();
        }
        _Sha_1 = Utils.sha1(_logMSG + _date + s);
        return _Sha_1;
    }

    public void set_merged(Boolean n, String parentA, String parentB) {
        _merged = n;
        parentA_sha1 = parentA;
        parentB_sha1 = parentB;
    }
    public void set_removedfiles(Blob b) {
        _removedfiles.put(b._filename, b);
    }

}
