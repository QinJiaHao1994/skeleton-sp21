package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int ops = 10000;
        AList<Integer> Ns = new AList();
        AList<Integer> opCounts = new AList();
        AList<Double> times = new AList();
        for (int i = 1000; i < 256000; i *= 2) {
            opCounts.addLast(10000);
            Ns.addLast(i);
        }

        int size = Ns.size();
        SLList<Integer> l = new SLList();

        for(int i = 0; i < size; i += 1) {
            int n = Ns.get(i);

            while(l.size() <= n) {
                l.addLast(n);
            }

            Stopwatch sw = new Stopwatch();
            for(int j = 0; j < ops; j+= 1) {
                l.getLast();
            }

            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
