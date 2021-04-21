package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Repository.CWD;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;
import static gitlet.Utils.join;

/**
 * @author Qin.JiaHao
 * @create 2021-04-18 3:51 下午
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

    public Boolean isEmpty() {
        return staged.size() == 0 && removal.size() == 0;
    }

    public TreeMap<String, Blob> getStaged() {
        return staged;
    }

    public TreeSet<String> getRemoval() {
        return removal;
    }

    public void save() {
        writeObject(stage, this);
    }

    public void add(String name) {
        File file = new File(CWD, name);
        if(!file.exists()) {
            exitWithError("File does not exist.");
        }

        Blob blob = new Blob(name, file);
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        Boolean tracked = blobs.containsKey(name);
        if (!tracked || Blob.isNotSame(blobs.get(name), blob)) {
            putToStaged(blob);
        }else {
            staged.remove(name);
        }

        removal.remove(name);
        save();
    }

    public void rm(String name) {
        File file = new File(CWD, name);
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        Boolean isStaged = staged.containsKey(name);
        Boolean isTracked = blobs.containsKey(name);

        if (isStaged) {
            staged.remove(name);
        }

        if (isTracked) {
            removal.add(name);
            if (file.exists()) {
                file.delete();
            }
        }

        if (!isStaged && !isTracked) {
            exitWithError("No reason to remove the file.");
        }
    }

    public void remove(String name) {
        staged.remove(name);
        removal.remove(name);
    }

    private void putToStaged(Blob blob) {
        String name = blob.getName();

        if (staged.containsKey(name) && Blob.isSame(staged.get(name), blob)) {
            return;
        }

        blob.save();
        staged.put(name, blob);
    }
}
