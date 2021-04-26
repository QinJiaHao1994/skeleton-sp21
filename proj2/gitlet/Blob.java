package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

/**
 * Represent a file blob
 * @author Jiahao Qin
 */
public class Blob implements Comparable<Blob>, Serializable {

    /**
     * Compare two blobs are same by their sha1 hash.
     * If the result is same, return True, else return False.
     * @param a
     * @param b
     * @return
     */
    public static Boolean isSame(Blob a, Blob b) {
        if (a == null || b == null) {
            return false;
        }

        return a.getHash().equals(b.getHash());
    }

    /**
     * Compare two blobs are same by their sha1 hash.
     * If the result is same, return False, else return True.
     * @param a
     * @param b
     * @return
     */
    public static Boolean isNotSame(Blob a, Blob b) {
        return !isSame(a, b);
    }

    /** The name of blob. */
    private String name;
    /** The Sha1 hash of blob. */
    private String hash;
    /** The file of blob. */
    private transient File content;

    /**
     * Create a new blob.
     * @param name
     * @param content
     */
    public Blob(String name, File content) {
        this.name = name;
        this.hash = sha1(serialize(readContents(content)));
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public File getContent() {
        if (content == null) {
            content = join(OBJECT_DIR, "blobs", hash);
        }
        return content;
    }

    /**
     * Copy blob from .gitlet/objects/blobs to working directory.
     */
    public void copyToWorkingDir() {
        try {
            content = join(OBJECT_DIR, "blobs", hash);
            File file = new File(CWD, name);
            file.createNewFile();
            writeContents(file, readContents(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save blob to .gitlet/objects/blobs
     */
    public void save() {
        File object = join(OBJECT_DIR, "blobs", hash);
        writeContents(object, readContents(content));
    }

    /**
     * Compare the given file and the file of blob is same by their sha1 hash.
     * @param file
     * @return
     */
    public Boolean isSameContent(File file) {
        return getHash().equals(sha1(serialize(readContents(file))));
    }

    @Override
    public int compareTo(Blob o) {
        return name.compareTo(o.name);
    }
}
