package ua.kpi;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class App {

    private static final int[] keys = new int[] {
            Integer.parseInt("0011000100110110", 2)
        , Integer.parseInt("0011001000110110", 2)
        , Integer.parseInt("0011001100110110", 2)
        , Integer.parseInt("0011010000110110", 2)
        , Integer.parseInt("0011010100110110", 2)
        , Integer.parseInt("0011011000110110", 2)
        , Integer.parseInt("0011011100110110", 2)};

    private static final Heys h = new Heys();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        System.out.println("Processors " + Runtime.getRuntime().availableProcessors());

        var t1 = System.nanoTime() / 1000000000.;
        var h = new Heys();

        var ct = h.encryptBlock(Integer.parseInt("0011000101100001", 2), keys);
        System.out.println("ct: " + Long.toString(ct, 16) + "(hex), " + Long.toString(ct, 2) + "(bin)");


        var sboxDdt = h.computeSboxDdt();
        System.out.println("sbox ddt " + Arrays.stream(sboxDdt).map(Arrays::toString).collect(Collectors.joining(", ")));

        var ddt = h.computeRoundDdt();
        System.out.println("ddt done ");

        var pMins = new double[]{0.01, 0.01, 0.001, 3.8e-05, 3.8e-06, 3.8e-06, 3.8e-06};
        var ds = new DifferentialSearch(13);
        var searchResult = ds.search(ddt, pMins);
        var maxDiffForKeySearch = searchResult.stream()
                .map(e -> e.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(20)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        System.out.println("maxDiff is done, first 20: " +  maxDiffForKeySearch);

        findKey(searchResult.get(4));

        System.out.println("\n\n\n TIMEEEEEEE " + (System.nanoTime() / 1000000000. - t1));



    }

    private static void findKey(Map<Integer, Double> maxDiffs) {

        var rand = new Random(42);
        var mask16 = (1 << 16) - 1;
        var pts = new ArrayList<>();
        var cts = new ArrayList<>();
        var a = 13;
        var keysCounts = new HashMap<Integer, Integer>();
        for (int i = 0; i < 1000; i++) {
            var pt = rand.nextInt() & mask16;
            var ptA = pt ^ a;
            var ct = h.encryptBlock(pt, keys);
            var ctA = h.encryptBlock(ptA, keys);
            pts.add(new Pair<>(pt, ptA));
            cts.add(new Pair<>(ct, ctA));
           for (int k = 0; k < (1 << 16); k++) {
                var ctk = ct ^k;
                var ctAk = ctA ^k;
                var ctSplitted = new int[]{ (ctk & ((1 << 4) - 1)), ((ctk >> 4) & ((1 << 4) - 1)),
                         ((ctk >> 8) & ((1 << 4) - 1)),  (ctk >> 12)};
                var ctASplitted = new int[]{ (ctAk & ((1 << 4) - 1)),  ((ctAk >> 4) & ((1 << 4) - 1)),
                         ((ctAk >> 8) & ((1 << 4) - 1)),  (ctAk >> 12)};
                var y = h.SInv(h.L(ctSplitted));
                var yA = h.SInv(h.L(ctASplitted));
                var b = y ^yA;
                if(maxDiffs.containsKey(b)) {
                    keysCounts.put(k, keysCounts.getOrDefault(k, 0) + 1);
                }
               // if

            }
        }
        System.out.println("key counts " + keysCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(100)
                .collect(Collectors.toList()));

    }
}
