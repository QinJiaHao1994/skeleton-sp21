package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static gitlet.Utils.join;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * */
    public static void init() {
        if(inRepo()) {
            exitWithError("A Gitlet version-control system already exists in the current directory.");
        }

        setup();
        Commit.init();
    }

    public static void commit(String messgae) {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        if(messgae.length() == 0) {
            exitWithError("Please enter a commit message.");
        }

        Commit.getCurrentCommit().addCommit(messgae);
    }

    public static void add(String name) {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        File file = getFile(name);
        Blob blob = new Blob(name, file);
        Stage.getInstance().add(blob);
    }

    public static void rm(String name) {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        File file = new File(CWD, name);
        Stage.getInstance().rm(name, file);
    }

    private static void setup() {
        //create HEAD
        Head.initHead();

        //create objects
        File objects = join(GITLET_DIR, "objects");
        objects.mkdir();
    }

    private static boolean inRepo() {
        return GITLET_DIR.exists();
    }

    private static File getFile(String name) {
        File file = new File(CWD, name);
        if(!file.exists()) {
            exitWithError("File does not exist.");
        }
        return file;
    }
}
