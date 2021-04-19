package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

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

    public void add(Blob blob) {
        String name = blob.getName();
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        if (removal.contains(name)) {
            return;
        }

        Boolean tracked = blobs.containsKey(name);
        if (!tracked || Blob.isNotSameFile(blobs.get(name), blob)) {
            diffAndReplace(blob);
        }else {
            removeFromStaged(blob);
        }

        System.out.println(staged.size());
        System.out.println(removal.size());

        save();
    }

    public void rm(String name, File file) {
        Commit commit = Commit.getCurrentCommit();
        TreeMap<String, Blob> blobs = commit.getBlobs();

        Boolean isStaged = staged.containsKey(name);
        Boolean isTracked = blobs.containsKey(name);

        if(isStaged) {
            staged.remove(name);
        }

        if(isTracked) {
            removal.add(name);
            if(file.exists()) {
                file.delete();
            }
        }

        if(!isStaged && !isTracked) {
            exitWithError("No reason to remove the file.");
        }
    }

    private void removeFromStaged(Blob blob) {
        String name = blob.getName();
        if (staged.containsKey(name)) {
            staged.remove(name);
        }
    }


    private void diffAndReplace(Blob blob) {
        String name = blob.getName();

        if(staged.containsKey(name) && Blob.isSameFile(staged.get(name), blob)) {
            return;
        }

        blob.save();
        staged.put(name, blob);
    }
}
