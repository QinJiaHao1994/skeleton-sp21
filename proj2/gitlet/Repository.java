package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

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
//    public static final File LOG_DIR = join(GITLET_DIR, "logs", "refs", "heads");
    public static final File REF_DIR = join(GITLET_DIR, "refs", "heads");
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * */
    public static void init() {
        if (inRepo()) {
            exitWithError("A Gitlet version-control system already exists in the current directory.");
        }

        setup();
        Commit.init();
    }

    public static void commit(String messgae) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        if (messgae.length() == 0) {
            exitWithError("Please enter a commit message.");
        }

        Commit.getCurrentCommit().addCommit(messgae);
    }

    public static void add(String name) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }


        Stage.getInstance().add(name);
    }

    public static void rm(String name) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Stage.getInstance().rm(name);
    }

    public static void log() {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        Commit commit = Commit.getCurrentCommit();
        commit.recursiveLog();
    }

    public static void globalLog() {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        List<String> hashes = Commit.getAllCommitHashes();
        for (String hash: hashes) {
            Commit.getCommitFromHash(hash).log();
            System.out.println();
        }
    }

    public static void find(String message) {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        List<String> hashes = Commit.getAllCommitHashes();
        Commit commit;
        Boolean notFind = true;
        for (String hash: hashes) {
            commit = Commit.getCommitFromHash(hash);
            if (commit.getMessage().equals(message)) {
                notFind = false;
                System.out.println(hash);
            }
        }

        if (notFind) {
            exitWithError("Found no commit with that message.");
        }
    }

    public static void status() {
        if(!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(REF_DIR);
        for (String branch: branches) {
            if (branch.equals(Head.getInstance().getBranch())) {
                System.out.println("*" + branch);
            }else {
                System.out.println(branch);
            }
        }
        System.out.println();

        Stage stage = Stage.getInstance();
        TreeMap<String, Blob> stagedFiles = stage.getStaged();
        TreeMap<String, Blob> trackedFiles = Commit.getCurrentCommit().getBlobs();

        Set<String> trackedFilenames = new TreeSet<>(trackedFiles.keySet());
        Set<String> currentFilenames = new TreeSet<>(plainFilenamesIn(CWD));
        Set<String> stagedFilenames = stagedFiles.keySet();
        Set<String> removalFileNames = stage.getRemoval();

        Set<String> modifiedFiles = new TreeSet<>();
        Set<String> untrackedFiles = new TreeSet<>();

        for (String stagedFilename: stagedFilenames) {
            if (!currentFilenames.contains(stagedFilename)) {
                modifiedFiles.add(stagedFilename + " (deleted)");
            }else if(!stagedFiles.get(stagedFilename).isSameContent(join(CWD, stagedFilename))) {
                modifiedFiles.add(stagedFilename + " (modified)");
            }

            trackedFilenames.remove(stagedFilename);
            currentFilenames.remove(stagedFilename);
        }

        for (String removalFilename: removalFileNames) {
            if (currentFilenames.contains(removalFilename)) {
                untrackedFiles.add(removalFilename);
            }

            trackedFilenames.remove(removalFilename);
            currentFilenames.remove(removalFilename);
        }

        for (String trackedFilename: trackedFilenames) {
            if (!currentFilenames.contains(trackedFilename)) {
                modifiedFiles.add(trackedFilename + " (deleted)");
            }

            if (!trackedFiles.get(trackedFilename).isSameContent(join(CWD, trackedFilename))) {
                modifiedFiles.add(trackedFilename + " (modified)");
            }

            currentFilenames.remove(trackedFilename);
        }

        untrackedFiles.addAll(currentFilenames);

        System.out.println("=== Staged Files ===");
        printIterable(stagedFilenames);
        System.out.println();

        System.out.println("=== Removed Files ===");
        printIterable(removalFileNames);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        printIterable(modifiedFiles);
        System.out.println();

        System.out.println("=== Untracked Files ===");
        printIterable(untrackedFiles);
        System.out.println();
    }

    public static void checkoutBranch(String branchName) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        List<String> branches = plainFilenamesIn(REF_DIR);
        if (!branches.contains(branchName)) {
            exitWithError("No such branch exists.");
        }

        if (branchName.equals(Head.getInstance().getBranch())) {
            exitWithError("No need to checkout the current branch.");
        }

        File branch = join(REF_DIR, branchName);
        String hash = readContentsAsString(branch);
        Commit commit = Commit.getCommitFromHash(hash);
        checkoutByCommit(commit);
        Head.save(branchName);
    }

    public static void checkout(String filename) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Commit commit = Commit.getCurrentCommit();
        checkoutFile(commit, filename);
    }

    public static void checkout(String commitId, String filename) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Commit commit = Commit.getCommitFromHashPrefix(commitId);
        if (commit == null) {
            exitWithError("No commit with that id exists");
        }

        checkoutFile(commit, filename);
    }

    public static void branch(String branchName) {
        File branch = join(REF_DIR, branchName);
        if (branch.exists()) {
            exitWithError("A branch with that name already exists.");
        }

        try {
            branch.createNewFile();
            writeContents(branch, Commit.getCurrentCommit().getHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rmBranch(String branchName) {
        File branch = join(REF_DIR, branchName);
        if(branch.exists()) {
            exitWithError("A branch with that name does not exist.");
        }

        if (branchName.equals(Head.getInstance().getBranch())) {
            exitWithError("Cannot remove the current branch.");
        }

        branch.delete();
    }

    public static void reset(String commitId) {
        Commit commit = Commit.getCommitFromHashPrefix(commitId);
        if (commit == null) {
            exitWithError("No commit with that id exists");
        }

        checkoutByCommit(commit);
        writeContents(Head.getInstance().getPointer(), commit.getHash());
    }

    public static void merge(String branchName) {

    }

    private static void checkoutFile(Commit commit, String filename) {
        TreeMap<String, Blob> blobs = commit.getBlobs();
        if (!blobs.containsKey(filename)) {
            exitWithError("File does not exist in that commit.");
        }

        Blob blob = blobs.get(filename);
        blob.fillContent();
        blob.copyToWorkingDir();
    }

    private static void checkoutByCommit(Commit commit) {
        verifyUntrackedWillBeOverwritten(commit);

        TreeMap<String, Blob> currentBlobs = Commit.getCurrentCommit().getBlobs();
        TreeMap<String, Blob> checkoutBlobs = commit.getBlobs();

        for (Blob blob: checkoutBlobs.values()) {
            blob.fillContent();
            blob.copyToWorkingDir();
            currentBlobs.remove(blob.getName());
        }

        for(String filename: currentBlobs.keySet()) {
            join(CWD, filename).delete();
        }

        Stage.initStage();
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

            //create HEAD
            HEAD_DIR.createNewFile();
            String content = "ref: refs/heads/master";
            writeContents(HEAD_DIR, content);

            //create objects
            OBJECT_DIR.mkdir();
            join(OBJECT_DIR, "commits").mkdir();
            join(OBJECT_DIR, "blobs").mkdir();
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

    private static <E> void printIterable(Iterable<E> items) {
        for (E item: items) {
            System.out.println(item.toString());
        }
    }

    private static void verifyUntrackedWillBeOverwritten(Commit commit) {
        TreeMap<String, Blob> currentBlobs = Commit.getCurrentCommit().getBlobs();
        TreeMap<String, Blob> checkoutBlobs = commit.getBlobs();
        List<String> filenames = plainFilenamesIn(CWD);

        for (String filename: filenames) {
            if(!currentBlobs.containsKey(filename) && checkoutBlobs.containsKey(filename)) {
                exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }
}
