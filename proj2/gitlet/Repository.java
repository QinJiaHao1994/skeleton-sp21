package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 * Handle all commands at a high level.
 * @author Jiahao Qin
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects directory. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    /** The references directory. */
    public static final File REF_DIR = join(GITLET_DIR, "refs", "heads");
    /** The Head file. */
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * */
    public static void init() {
        if (inRepo()) {
            exitWithError("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }

        setup();
        Commit.init();
    }

    /**
     * Create a commit in the current branch.
     * @param messgae
     */
    public static void commit(String messgae) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        if (messgae.length() == 0) {
            exitWithError("Please enter a commit message.");
        }

        Commit.getCurrentCommit().addCommit(messgae);
    }

    /**
     * Add a file to stage for addition.
     * @param name
     */
    public static void add(String name) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }


        Stage.getInstance().add(name);
    }

    /**
     * Add a file to stage for removal, then remove the file
     * in working directory if the file is tracked by current commit.
     * @param name
     */
    public static void rm(String name) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Stage.getInstance().rm(name);
    }

    /**
     * Display information about each commit in current branch.
     */
    public static void log() {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        Commit commit = Commit.getCurrentCommit();
        commit.recursiveLog();
    }

    /**
     * Display information about all the commits.
     */
    public static void globalLog() {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        List<String> hashes = Commit.getAllCommitHashes();
        for (String hash: hashes) {
            Commit.getCommitFromHash(hash).log();
            System.out.println();
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message.
     * @param message
     */
    public static void find(String message) {
        if (!inRepo()) {
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

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void status() {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(REF_DIR);
        for (String branchName: branchNames) {
            if (branchName.equals(Head.getInstance().getBranchName())) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
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
            } else if (!stagedFiles.get(stagedFilename).isSameContent(join(CWD, stagedFilename))) {
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
            Blob trackedFile = trackedFiles.get(trackedFilename);
            if (!currentFilenames.contains(trackedFilename)) {
                modifiedFiles.add(trackedFilename + " (deleted)");
            } else if (!trackedFile.isSameContent(join(CWD, trackedFilename))) {
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

    /**
     * checkout a given branch.
     * @param branchName
     */
    public static void checkoutBranch(String branchName) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        File branch = join(REF_DIR, branchName);
        if (!branch.exists()) {
            exitWithError("No such branch exists.");
        }

        if (branchName.equals(Head.getInstance().getBranchName())) {
            exitWithError("No need to checkout the current branch.");
        }

        Commit commit = Commit.getCommitFromBranch(branch);
        checkoutByCommit(commit);
        Head.save(branchName);
    }

    /**
     * Checkout a file from current commit;
     * @param filename
     */
    public static void checkout(String filename) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Commit commit = Commit.getCurrentCommit();
        checkoutFile(commit, filename);
    }

    /**
     * Checkout a file from given commit;
     * @param commitId
     * @param filename
     */
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

    /**
     * Create a new branch
     * @param branchName
     */
    public static void branch(String branchName) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

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

    /**
     * Remove a branch
     * @param branchName
     */
    public static void rmBranch(String branchName) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        File branch = join(REF_DIR, branchName);
        if (!branch.exists()) {
            exitWithError("A branch with that name does not exist.");
        }

        if (branchName.equals(Head.getInstance().getBranchName())) {
            exitWithError("Cannot remove the current branch.");
        }

        branch.delete();
    }

    /**
     * Checkout all the files tracked by the given commit and change the branch pointer to that commit.
     * @param commitId
     */
    public static void reset(String commitId) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        Commit commit = Commit.getCommitFromHashPrefix(commitId);
        if (commit == null) {
            exitWithError("No commit with that id exists");
        }

        checkoutByCommit(commit);
        writeContents(Head.getInstance().getBranch(), commit.getHash());
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param branchName
     */
    public static void merge(String branchName) {
        if (!inRepo()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        if (!Stage.getInstance().isEmpty()) {
            exitWithError("You have uncommitted changes.");
        }

        File branch = join(REF_DIR, branchName);
        if (!branch.exists()) {
            exitWithError("A branch with that name does not exist.");
        }

        String currentBranchName = Head.getInstance().getBranchName();
        if (branchName.equals(currentBranchName)) {
            exitWithError("Cannot merge a branch with itself.");
        }


        Commit mergeCommit = Commit.getCommitFromBranch(branch);
        mergeCommit.setBranchName(branchName);
        verifyUntrackedWillBeOverwritten(mergeCommit);

        Commit currentCommit = Commit.getCurrentCommit();
        currentCommit.setBranchName(currentBranchName);

        mergeHelper(currentCommit, mergeCommit);
    }

    /**
     * Diff files in the current branch, current branch and the split point.
     * @param currentCommit
     * @param mergeCommit
     */
    private static void mergeHelper(Commit currentCommit, Commit mergeCommit) {
        Commit splitPoint = Commit.findSplitPoint(currentCommit, mergeCommit);
        if (Commit.isSame(splitPoint, mergeCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (Commit.isSame(splitPoint, currentCommit)) {
            Repository.checkoutBranch(mergeCommit.getBranchName());
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Boolean hasConflicts = false;
        TreeMap<String, Blob> splitBlobs = splitPoint.getBlobs();
        TreeMap<String, Blob> headBlobs = currentCommit.getBlobs();
        TreeMap<String, Blob> otherBlobs = mergeCommit.getBlobs();
        Stage stage = Stage.getInstance();

        for (String filename: splitBlobs.keySet()) {
            Boolean inHead = headBlobs.containsKey(filename);
            Boolean inOther = otherBlobs.containsKey(filename);
            Blob splitBlob = splitBlobs.get(filename);

            if (inHead && inOther) {
                Blob headBlob = headBlobs.get(filename);
                Blob otherBlob = otherBlobs.get(filename);
                Boolean modifiedInHead = Blob.isNotSame(headBlob, splitBlob);
                Boolean modifiedInOther = Blob.isNotSame(otherBlob, splitBlob);
                Boolean modifiedInDiffWays =  Blob.isNotSame(headBlob, otherBlob);

                if (modifiedInOther && !modifiedInHead) {
                    otherBlob.copyToWorkingDir();
                    stage.addToStaged(otherBlob);
                } else if (modifiedInHead && modifiedInOther && modifiedInDiffWays) {
                    hasConflicts = true;
                    conflictHelper(filename, headBlob.getContent(), otherBlob.getContent());
                }
            }
            if (inHead && !inOther) {
                Blob headBlob = headBlobs.get(filename);
                Boolean modifiedInHead = Blob.isNotSame(headBlob, splitBlob);

                if (modifiedInHead) {
                    hasConflicts = true;
                    conflictHelper(filename, headBlob.getContent(), null);
                } else {
                    stage.addToRemoval(filename);
                }
            }
            if (inOther && !inHead) {
                Blob otherBlob = otherBlobs.get(filename);
                Boolean modifiedInOther = Blob.isNotSame(otherBlob, splitBlob);

                if (modifiedInOther) {
                    hasConflicts = true;
                    conflictHelper(filename, null, otherBlob.getContent());
                }
            }

            otherBlobs.remove(filename);
        }

        for (String filename: otherBlobs.keySet()) {
            Blob otherBlob = otherBlobs.get(filename);
            Blob headBlob = headBlobs.get(filename);
            Boolean inHead = headBlobs.containsKey(filename);
            if (!inHead) {
                otherBlob.copyToWorkingDir();
                stage.addToStaged(otherBlob);
            } else if (Blob.isNotSame(otherBlob, headBlob)) {
                hasConflicts = true;
                conflictHelper(filename, headBlob.getContent(), otherBlob.getContent());
            }
        }

        currentCommit.merge(mergeCommit);
        if (hasConflicts) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Handle conflict files in merge process.
     * @param filename
     * @param headFile
     * @param otherFile
     */
    private static void conflictHelper(String filename, File headFile, File otherFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("<<<<<<< HEAD\n");
        if (headFile != null) {
            sb.append(readContentsAsString(headFile));
        }
        sb.append("=======\n");
        if (otherFile != null) {
            sb.append(readContentsAsString(otherFile));
        }
        sb.append(">>>>>>>\n");

        try {
            Stage stage = Stage.getInstance();
            File file = join(CWD, filename);
            file.createNewFile();
            writeContents(file, sb.toString());
            Blob blob = new Blob(filename, file);
            blob.save();
            stage.addToStaged(blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checkout given filename from given commit.
     * @param commit
     * @param filename
     */
    private static void checkoutFile(Commit commit, String filename) {
        TreeMap<String, Blob> blobs = commit.getBlobs();
        if (!blobs.containsKey(filename)) {
            exitWithError("File does not exist in that commit.");
        }

        Blob blob = blobs.get(filename);
        blob.copyToWorkingDir();
    }

    /**
     * Checkout all the files tracked by the given commit.
     * @param commit
     */
    private static void checkoutByCommit(Commit commit) {
        verifyUntrackedWillBeOverwritten(commit);

        TreeMap<String, Blob> currentBlobs = Commit.getCurrentCommit().getBlobs();
        TreeMap<String, Blob> checkoutBlobs = commit.getBlobs();

        for (Blob blob: checkoutBlobs.values()) {
            blob.copyToWorkingDir();
            currentBlobs.remove(blob.getName());
        }

        for (String filename: currentBlobs.keySet()) {
            join(CWD, filename).delete();
        }

        Stage.initStage();
    }

    /**
     * Setup all directories when init the repository.
     */
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

    /**
     * Verify whether the command is execute in the repository.
     * @return
     */
    private static boolean inRepo() {
        return GITLET_DIR.exists();
    }

    /**
     * Print iterable items line by line.
     * @param items
     * @param <E>
     */
    private static <E> void printIterable(Iterable<E> items) {
        for (E item: items) {
            System.out.println(item.toString());
        }
    }

    /**
     * Verify whether untracked files will be overwritten.
     * @param commit
     */
    private static void verifyUntrackedWillBeOverwritten(Commit commit) {
        TreeMap<String, Blob> currentBlobs = Commit.getCurrentCommit().getBlobs();
        TreeMap<String, Blob> checkoutBlobs = commit.getBlobs();
        List<String> filenames = plainFilenamesIn(CWD);

        for (String filename: filenames) {
            if (!currentBlobs.containsKey(filename) && checkoutBlobs.containsKey(filename)) {
                exitWithError("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
    }
}
