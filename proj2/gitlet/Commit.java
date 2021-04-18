package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeSet;

import static gitlet.Utils.*;
import static gitlet.Utils.join;

/** Represents a gitlet commit object.
 *  Handle gitlet commands.
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    public static final File OBJECT_DIR = join(Repository.GITLET_DIR, "objects");

    public static Commit getCommit() {
        File pointer = Head.getInstance().getPointer();
        String hash = readContentsAsString(pointer);
        File object = join(OBJECT_DIR, hash);
        return readObject(object, Commit.class);
    }

    private transient String hash;
    private String message;
    private Date timestamp;
    private String[] parentHashs;
    private transient Commit[] parents;
    private TreeSet<Blob> blobs;

    public Commit() {
        message = "initial commit";
        timestamp = new Date(0);
    }

    public void save() {
        try {
            hash = sha1(serialize(this));
            File object = join(OBJECT_DIR, hash);
            object.createNewFile();
            writeObject(object, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHash() {
        return hash;
    }

    // set head to refs/heads/master

}
