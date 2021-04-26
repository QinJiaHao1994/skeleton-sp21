package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Repository.HEAD_DIR;

/**
 * Represents the Head pointer.
 * @author Jiahao Qin
 */
public class Head {
    private static Head instance;

    public static synchronized Head getInstance() {
        if (instance == null) {
            instance = new Head();
        }
        return instance;
    }

    /**
     * Save new branch pointer to HEAD.
     * @param branch
     */
    public static void save(String branch) {
        writeContents(HEAD_DIR, "ref: refs/heads/" + branch);
        instance = null;
    }

    /** The branch file of head pointer. */
    private File branch;
    /** The branch name of head pointer. */
    private String branchName;

    /**
     * Get pointer of HEAD, then advances the pointer.
     * @param hash
     */
    public void advancePointer(String hash) {
        writeContents(branch, hash);
    }

    public File getBranch() {
        return branch;
    }

    public String getBranchName() {
        return branchName;
    }

    private Head() {
        String content = readContentsAsString(HEAD_DIR);
        parse(content);
    }

    /**
     * parse content of HEAD which points to a branch reference.
     * @param content
     */
    private void parse(String content) {
        String path = content.substring(5);
        String[] paths = path.split("/");
        branchName = paths[paths.length - 1];
        branch = join(GITLET_DIR, paths);
    }
}
