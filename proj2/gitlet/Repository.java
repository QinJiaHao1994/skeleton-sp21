package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Utils.join;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File LOG_DIR = join(GITLET_DIR, "logs", "refs", "heads");
    public static final File REF_DIR = join(GITLET_DIR, "refs", "heads");
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");

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

    public static void log() {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        String branch = Head.getInstance().getBranch();
        File logFile = join(LOG_DIR, branch);
        printFile(logFile);
    }

    public static void  globalLog() {
        List<String> filenames = plainFilenamesIn(LOG_DIR);
        for (String filename: filenames) {
            File logFile = join(LOG_DIR, filename);
            printFile(logFile);
        }
    }

    private static void printFile(File file) {
        String content = readContentsAsString(file);
        String[] logArr = content.split("\n\n");
        reverseArray(logArr);
        for (String commit : logArr) {
            System.out.println(commit);
            System.out.println();
        }
    }

    private static <T> void reverseArray(T[] arr) {
        if(arr == null) {
            return;
        }

        int length = arr.length;
        T temp;

        for (int i = 0; i < length / 2; i ++) {
            temp = arr[i];
            arr[i] = arr[length - 1 - i];
            arr[length - 1 - i] = temp;
        }
    }

    private static void setup() {
        try {
            //create refs
            REF_DIR.mkdirs();
            join(REF_DIR, "master").createNewFile();

            //create logs
            LOG_DIR.mkdirs();
            join(LOG_DIR, "master").createNewFile();

            //create HEAD
            HEAD_DIR.createNewFile();
            String content = "ref: refs/heads/master";
            writeContents(HEAD_DIR, content);

            //create objects
            OBJECT_DIR.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
