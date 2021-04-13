package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> test = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                test.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = test.size();
                System.out.println("size: " + size);
            } else {
                if(L.size() != 0) {
                    L.removeLast();
                    int lastVal = test.removeLast();
                    System.out.println("removeLast(" + lastVal + ")");
                }
            }
        }
    }

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> test = new BuggyAList<>();

        correct.addLast(4);
        correct.addLast(5);
        correct.addLast(6);

        test.addLast(4);
        test.addLast(5);
        test.addLast(6);

        assertEquals(correct.size(), test.size());

        assertEquals(correct.removeLast(), test.removeLast());
        assertEquals(correct.removeLast(), test.removeLast());
        assertEquals(correct.removeLast(), test.removeLast());
    }
}
