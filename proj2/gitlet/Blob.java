package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

/**
 * @author Jiahao Qin
 * @create 2021-04-18 12:09 下午
 */
public class Blob implements Comparable<Blob>, Serializable {

    public static Boolean isSame(Blob a, Blob b) {
        if (a == null || b == null) {
            return false;
        }

        return a.getHash().equals(b.getHash());
    }

    public static Boolean isNotSame(Blob a, Blob b) {
        return !isSame(a, b);
    }

    private String name;
    private String hash;
    private transient File content;

    public Blob(String name, File content) {
        this.name = name;
        this.hash = sha1(serialize(readContents(content)));
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public File getContent() {
        if (content == null) {
            content = join(OBJECT_DIR, "blobs", hash);
        }
        return content;
    }

    public void copyToWorkingDir() {
        try {
            content = join(OBJECT_DIR, "blobs", hash);
            File file = new File(CWD, name);
            file.createNewFile();
            writeContents(file, readContents(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        File object = join(OBJECT_DIR, "blobs", hash);
        writeContents(object, readContents(content));
    }

    public Boolean isSameContent(File file) {
        return getHash().equals(sha1(serialize(readContents(file))));
    }

    @Override
    public int compareTo(Blob o) {
        return name.compareTo(o.name);
    }
}
