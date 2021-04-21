package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;

import static gitlet.Repository.*;
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

    private static Commit currentCommit;

    public static Commit getCurrentCommit() {
        if (currentCommit == null) {
            File pointer = Head.getInstance().getPointer();
            String hash = readContentsAsString(pointer);
            currentCommit = getCommitFromHash(hash);
        }
        return currentCommit;
    }

    public static void init() {
        Commit commit = new Commit();
        commit.save();
    }

    public static Commit getCommitFromHash(String hash) {
        File object = join(OBJECT_DIR, "commits", hash);
        Commit commit = readObject(object, Commit.class);
        commit.setHash(hash);
        return commit;
    }

    private transient String hash;
    private String message;
    private String timestamp;
    private ArrayList<String> parentIds;
    private TreeMap<String, Blob> blobs;

    public Commit() {
        message = "initial commit";
        timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        parentIds = new ArrayList<>();
        blobs = new TreeMap<>();
    }

    public Commit(String message, ArrayList<String> parentIds, TreeMap<String, Blob> blobs) {
        this.message = message;
        this.timestamp = ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy xxxx"));
        this.parentIds = parentIds;
        this.blobs = blobs;
    }

    /** Derives a new commit. */
    public void addCommit(String message) {
        ArrayList<String> parentIds = new ArrayList<>();
        TreeMap<String, Blob> blobs = new TreeMap<>();

        parentIds.add(currentCommit.getHash());
        blobs.putAll(currentCommit.getBlobs());

        // merge files staged for addition or removal
        Stage index = Stage.getInstance();
        if (index.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }

        blobs.putAll(index.getStaged());
        for (String name: index.getRemoval()) {
            blobs.remove(name);
        }

        Commit commit = new Commit(message, parentIds, blobs);
        commit.save();
    }

    public void mergeCommit(Commit mergeCommit) {

    }

    private void save() {
        try {
            setHash(sha1(serialize(this)));
            File object = join(OBJECT_DIR, "commits", hash);
            object.createNewFile();
            writeObject(object, this);

            advancePointer();
            Stage.initStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public TreeMap<String, Blob> getBlobs() {
        return blobs;
    }

    private void advancePointer() {
        Head.getInstance().advancePointer(hash);
    }

    public void recursiveLog() {
        log();

        if(parentIds.size() != 0) {
            System.out.println();
            Commit parentCommit = getCommitFromHash(parentIds.get(0));
            parentCommit.recursiveLog();
        }
    }

    public void log() {
        System.out.println("===");
        System.out.println("commit " + hash);
        if(parentIds.size() > 1) {
            System.out.print("Merge:");
            for (String id: parentIds) {
                System.out.print(" " + id.substring(0,7));
            }
            System.out.println();
        }
        System.out.println("Date: " + timestamp);
        System.out.println(message);
    }
}
