package gitlet;

import java.io.File;

/**
 * @author Jiahao Qin
 * @create 2021-04-18 12:09 下午
 */
public class Blob implements Comparable<Blob>{
    private String name;
    private String hash;
    private transient File content;

    @Override
    public int compareTo(Blob o) {
        return name.compareTo(o.name);
    }
}
