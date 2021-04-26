package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Utils.join;

/**
 * Represents the Stage area.
 * @author Jiahao Qin
 */
public class Stage implements Serializable {
    private static File stage = join(GITLET_DIR, "index");
    private static Stage instance;

    public static synchronized Stage getInstance() {
        if (instance == null) {
            instance = readObject(stage, Stage.class);
        }
        return instance;
    }

    /**
     * Init stage area.
     */
    public static void initStage() {
        try {
            stage.createNewFile();
            writeObject(stage, new Stage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TreeMap<String, Blob> staged;
    private TreeSet<String> removal;

    private Stage() {
        staged = new TreeMap<>();
        removal = new TreeSet<>();
    }

    /**
     * Verify the stage area is empty.
     * @return
     */
    public Boolean isEmpty() {
        return staged.size() == 0 && removal.size() == 0;
    }

    public TreeMap<String, Blob> getStaged() {
        return staged;
    }

    public TreeSet<String> getRemoval() {
        return removal;
    }

    /**
     * Save Stage object.
     */
    public void save() {
        writeObject(stage, this);
    }

    /**
     * Add a file to stage for addition.
     * @param name
     */
    public void add(String name) {
        File file = new File(CWD, name);
        if (!file.exists()) {
            exitWithError("File does not exist.");
        }

        Blob blob = new Blob(name, file);
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        Boolean tracked = blobs.containsKey(name);
        if (!tracked || Blob.isNotSame(blobs.get(name), blob)) {
            diffAndStaged(blob);
        } else {
            staged.remove(name);
        }

        removal.remove(name);
        save();
    }

    /**
     * Add a file to stage for removal, then remove the file
     * in working directory if the file is tracked by current commit.
     * @param name
     */
    public void rm(String name) {
        File file = new File(CWD, name);
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        Boolean isStaged = staged.containsKey(name);
        Boolean isTracked = blobs.containsKey(name);

        if (!isStaged && !isTracked) {
            exitWithError("No reason to remove the file.");
        }

        if (isStaged) {
            staged.remove(name);
        }

        if (isTracked) {
            removal.add(name);
            file.delete();
        }

        save();
    }

    public void addToStaged(Blob blob) {
        staged.put(blob.getName(), blob);
    }

    public void addToRemoval(String key) {
        File file = new File(CWD, key);
        removal.add(key);
        file.delete();
    }

    private void diffAndStaged(Blob blob) {
        String name = blob.getName();

        if (staged.containsKey(name) && Blob.isSame(staged.get(name), blob)) {
            return;
        }

        blob.save();
        staged.put(name, blob);
    }
}
