package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Repository.HEAD_DIR;

/**
 * @author Jiahao Qin
 * @create 2021-04-18 2:12 下午
 */
public class Head {
    private static Head instance;

    public static synchronized Head getInstance() {
        if(instance == null) {
            instance = new Head();
        }
        return instance;
    }

    /** save new branch pointer to HEAD. */
    public static void save(String branch) {
        writeContents(HEAD_DIR, "ref: refs/heads/" + branch);
        instance = null;
    }

    private File branch;
    private String branchName;

    /** get pointer of HEAD, then advances the pointer. */
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

    /** parse content of HEAD which points to a branch reference. */
    private void parse(String content) {
        String path = content.substring(5);
        String[] paths = path.split("/");
        branchName = paths[paths.length - 1];
        branch = join(GITLET_DIR, paths);
    }
}
