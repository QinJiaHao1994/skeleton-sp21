package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;
import static gitlet.Repository.GITLET_DIR;

/**
 * @author Qin.JiaHao
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

    /**
     * Creates HEAD file and fill in "ref: refs/heads/master"
     * */
    public static void initHead() {
        try {
            //create refs
            File heads = join(GITLET_DIR, "refs", "heads");
            heads.mkdirs();

            File master = join(heads, "master");
            master.createNewFile();

            String content = "ref: refs/heads/master";
            File head = join(GITLET_DIR, "HEAD");
            head.createNewFile();
            writeContents(head, content);

            instance = new Head(head, master);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File head;
    private File pointer;

    /** get pointer of HEAD, then advances the pointer. */
    public void advance(String hash) {
        writeContents(pointer, hash);
    }

    /** save new branch pointer to HEAD*/
    public void save(String branch) {
        writeContents(head, "ref: refs/heads/" + branch);
    }

    public File getPointer() {
        return pointer;
    }

    private Head(File head, File pointer) {
        this.head = head;
        this.pointer = pointer;
    }

    private Head() {
        head = join(GITLET_DIR, "HEAD");
        String content = readContentsAsString(head);
        parse(content);
    }

    /** parse content of HEAD which points to a branch reference. */
    private void parse(String content) {
        String path = content.substring(5);
        String[] paths = path.split("/");
        pointer = join(GITLET_DIR, paths);
    }
}
