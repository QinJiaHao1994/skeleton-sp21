package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Utils.join;

public class Commit implements Serializable {

    private static Commit currentCommit;

    public static void init() {
        Commit commit = new Commit();
        commit.save();
    }

    public static Commit getCurrentCommit() {
        if (currentCommit == null) {
            File branch = Head.getInstance().getBranch();
            currentCommit = getCommitFromBranch(branch);
        }
        return currentCommit;
    }

    public static Commit getCommitFromBranch(File branch) {
        String hash = readContentsAsString(branch);
        return getCommitFromHash(hash);
    }

    public static Commit getCommitFromHash(String hash) {
        String prefix = hash.substring(0, 2);
        File object = join(OBJECT_DIR, "commits", prefix, hash.substring(2));
        Commit commit = readObject(object, Commit.class);
        commit.setHash(hash);
        return commit;
    }

    public static Commit getCommitFromHashPrefix(String commitId) {
        int length = commitId.length();
        if (length < 4) {
            return null;
        }

        String prefix = commitId.substring(0, 2);
        File prefixDir = join(OBJECT_DIR, "commits", prefix);

        if (!prefixDir.exists()) {
            return null;
        }

        List<String> results = new LinkedList<>();

        for(String hashFragment : plainFilenamesIn(prefixDir)) {
            if(hashFragment.startsWith(commitId.substring(2))) {
                results.add(hashFragment);
            }
        }

        if(results.size() != 1) {
            return null;
        }

        String lastPath = results.get(0);
        File object = join(prefixDir, lastPath);
        Commit commit = readObject(object, Commit.class);
        commit.setHash(prefix + lastPath);
        return commit;
    }

    public static List<String> getAllCommitHashes() {
        File commits = join(OBJECT_DIR, "commits");
        List<String> hashes = new LinkedList<>();
        for(String prefix: commits.list()) {
            File prefixDir = join(commits, prefix);
            for(String hashFragment : plainFilenamesIn(prefixDir)) {
                hashes.add(prefix + hashFragment);
            }
        }

        return hashes;
    }

    public static Boolean isSame(Commit p, Commit q) {
        if (p == null || q == null) {
            return false;
        }

        return p.hash.equals(q.hash);
    }

    public static Commit findSplitPoint(Commit currentCommit, Commit mergeCommit) {
        Queue<Commit> queue = new LinkedList<>();
        queue.add(currentCommit);
        queue.add(mergeCommit);

        HashMap<String, String> commitMap = new HashMap<>();

        while (!queue.isEmpty()) {
            Commit commit = queue.remove();
            String hash = commit.getHash();
            String branchName = commit.getBranchName();

            if (commitMap.containsKey(hash) && !commitMap.get(hash).equals(branchName)) {
                return commit;
            }

            if (!commitMap.containsKey(hash)) {
                commitMap.put(hash, branchName);
                for (Commit parent : commit.parents()) {
                    queue.add(parent);
                }
            }
        }

        return null;
    }

    private transient String hash;
    private transient String branchName;
    private TreeMap<String, Blob> blobs;
    private String message;
    private String timestamp;
    private List<String> parentIds;

    private Commit() {
        message = "initial commit";
        timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        parentIds = new ArrayList<>();
        blobs = new TreeMap<>();
    }

    private Commit(String message, ArrayList<String> parentIds, TreeMap<String, Blob> blobs) {
        this.message = message;
        this.timestamp = ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy xxxx"));
        this.parentIds = parentIds;
        this.blobs = blobs;
    }

    public Commit[] parents() {
        int length = parentIds.size();
        Commit[] parents = new Commit[length];

        for (int i = 0; i < length; i++) {
            parents[i] = getCommitFromHash(parentIds.get(i));
            parents[i].branchName = branchName;
        }

        return parents;
    }

    /** Derives a new commit. */
    public void addCommit(String message) {
        ArrayList<String> parentIds = new ArrayList<>();
        parentIds.add(currentCommit.getHash());
        commitHelper(message, parentIds);
    }

    public void merge(Commit mergeCommit) {
        String message = "Merged " + mergeCommit.getBranchName() + " into " + currentCommit.getBranchName() + ".";
        ArrayList<String> parentIds = new ArrayList<>();
        parentIds.add(currentCommit.getHash());
        parentIds.add(mergeCommit.getHash());
        commitHelper(message, parentIds);
    }

    public String getMessage() {
        return message;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
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

    private void advancePointer() {
        Head.getInstance().advancePointer(hash);
    }

    private void save() {
        try {
            setHash(sha1(serialize(this)));
            String prefix = hash.substring(0, 2);
            File prefixDir = join(OBJECT_DIR, "commits", prefix);
            prefixDir.mkdir();
            File object = join(prefixDir, hash.substring(2));
            object.createNewFile();
            writeObject(object, this);

            advancePointer();
            Stage.initStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void commitHelper(String message, ArrayList<String> parentIds) {
        TreeMap<String, Blob> blobs = new TreeMap<>();

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
}
