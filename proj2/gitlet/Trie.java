package gitlet;

import java.util.HashMap;

/**
 * @author Qin.JiaHao
 * @create 2021-04-21 8:01 下午
 */
public class Trie {
    private static final int DEFAULT_DEPTH = 7;

    private class Node {
        private HashMap<String, Node> children;

        public Node() {
            children = new HashMap<>();
        }

        private String[] get(String prefix, int index) {
            int length = prefix.length();

            if (index == length) {
                return null;
            }



            return null;
//            int length = prefix.length();
//
//
//
//            char s = prefix.charAt(0);
//            if (!children.containsKey(String.valueOf(s))) {
//                return;
//            }
//
//            bf.append(s);
//
//            return null;
        }

        public void get(String prefix) {
            get(prefix, 0);
        }

        public void put(String name) {
            put(name, 0);
        }

        private void put(String name, int index) {
            int length = name.length();

            if (index == length) {
                return;
            }

            String key;
            if (index < maxDepth - 1) {
                key = String.valueOf(name.charAt(index));
                children.getOrDefault(key, new Node()).put(name, index + 1);
            } else {
                key = name.substring(index);
                children.getOrDefault(key, new Node());
            }
        }
    }

    private Node root;
    private int maxDepth = DEFAULT_DEPTH;

    public Trie() {
        root = new Node();
    }

    public Trie(int depth) {
        this();

        if (depth > DEFAULT_DEPTH) {
            maxDepth = depth;
        }
    }

    public void put(String name) {
        if (name == null) {
            return;
        }

        root.put(name);
    }

    public String get(String prefix) {
//        StringBuffer bf = new StringBuffer();
        root.get(prefix);
//        if(bf.length() == 0) {
//            return null;
//        }
//
//        return bf.toString();
        return null;
    }

    public static void main(String[] args) {

    }
}
