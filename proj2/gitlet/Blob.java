package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import static gitlet.Repository.OBJECT_DIR;

/**
 * @author Jiahao Qin
 * @create 2021-04-18 12:09 下午
 */
public class Blob implements Comparable<Blob>{

    public static Boolean isSameFile(Blob a, Blob b) {
        if(a == null || b == null) {
            return false;
        }

        return a.getHash().equals(b.getHash());
    }

    public static Boolean isNotSameFile(Blob a, Blob b) {
        return !isSameFile(a, b);
    }

    private String name;
    private String hash;
    private transient File content;

    public Blob(String name, File content) {
        this.name = name;
        this.hash = sha1(serialize(content));
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public void delete() {

    }

    public void save() {
        File object = join(OBJECT_DIR, hash);
        writeContents(object, readContents(content));
    }

    @Override
    public int compareTo(Blob o) {
        return name.compareTo(o.name);
    }
}
