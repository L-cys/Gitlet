package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * The class to represent the contents of files.
 *
 * @author chenyuanshan
 */

public class Blob implements Serializable {

    /**
     * The filename of this blob to represent.
     */
    String _filename;
    /**
     * The sha1 value of this blob.
     */
    String _blobSHA1;
    /**
     * Contents in byte.
     */
    byte[] _contents;
    /**
     * Contents in String.
     */
    String _contentstring;

    /**
     * Initialize a new Blob with name FILENAME.
     */
    public Blob(String filename) {
        _filename = filename;
    }

    /**
     * Set the contents of this blob from FILE.
     */
    public void setcontents(File file) {
        _contents = Utils.readContents(file);
        _contentstring = Utils.readContentsAsString(file);
    }

    /**
     * @return the SHA1 value of this blob.
     */
    public String getSHA1() {
        String cts = new String(_contents);
        _blobSHA1 = Utils.sha1(cts + _filename);
        return _blobSHA1;
    }


}
