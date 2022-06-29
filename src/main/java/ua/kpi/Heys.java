package ua.kpi;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Heys {

    private static final int threads = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService threadpool
            = Executors.newFixedThreadPool(threads);

    @AllArgsConstructor
    private class DdtBatchClass implements Callable<Map<Integer, Map<Integer, Double>>> {
        private int dxStart;
        private int dxEnd;
        private int id;
        private Map<Integer, Integer> encryptRound;
        private double pMin;

        @Override
        public Map<Integer, Map<Integer, Double>> call() {

            double probabilityBit = 1. / (1 << BLOCK_SIZE_BITS);
            Map<Integer, Map<Integer, Double>> ddtMap = new HashMap<>();

            System.out.println("dxStart " + dxStart + " dxEnd " + dxEnd + " id " + id);
            var t = System.nanoTime() / 1000000000.;
            for (int dx = dxStart; dx < dxEnd; dx++) {
                if (dx % 500 == 0) {
                    System.out.println("id " + id + " dx " + dx + " t: " + (System.nanoTime() / 1000000000. - t));
                }
                t = System.nanoTime() / 1000000000.;
                var dxMap = ddtMap.getOrDefault(dx, new HashMap<>());
                for (int a = 0; a < (1 << BLOCK_SIZE_BITS); a++) {
                    int dy = encryptRound.get(a) ^ encryptRound.get(a ^ dx);
                    dxMap.put(dy, dxMap.getOrDefault(dy, 0.) + probabilityBit);
                    if (a % 10000 == 0) {
                        System.out.println("id " + id + " a " + a + " s: " + ddtMap.size() + " " + dxMap.size());
                    }
                }
                var dxVal = dxMap.entrySet().stream().parallel().filter(e -> e.getValue() > pMin).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                System.out.println("id " + id + " dx " + dx + " valsize: " + dxVal.size());
                ddtMap.put(dx, dxVal);

            }
            System.out.println("done " + id);
            return ddtMap;
        }
    }

    private final int[] HeysSBox = new int[]{
         //   0xA, 0x9, 0xD, 0x6, 0xE, 0xB, 0x4, 0x5, 0xF, 0x1, 0x3, 0xC, 0x7, 0x0, 0x8, 0x2
               0x8, 0x0, 0xC, 0x4, 0x9, 0x6, 0x7, 0xB, 0x2, 0x3, 0x1, 0xF, 0x5, 0xE, 0xA, 0xD
            /*  , 0xF, 0x6, 0x5, 0x8, 0xE, 0xB, 0xA, 0x4, 0xC, 0x0, 0x3, 0x7, 0x2, 0x9, 0x1, 0xD*/
            // ,
            /*0x3, 0x8, 0xD, 0x9, 0x6, 0xB, 0xF, 0x0, 0x2, 0x5, 0xC, 0xA, 0x4, 0xE, 0x1, 0x7
            , 0xF, 0x8, 0xE, 0x9, 0x7, 0x2, 0x0, 0xD, 0xC, 0x6, 0x1, 0x5, 0xB, 0x4, 0x3, 0xA
            , 0x2, 0x8, 0x9, 0x7, 0x5, 0xF, 0x0, 0xB, 0xC, 0x1, 0xD, 0xE, 0xA, 0x3, 0x6, 0x4
            , 0x3, 0x8, 0xB, 0x5, 0x6, 0x4, 0xE, 0xA, 0x2, 0xC, 0x1, 0x7, 0x9, 0xF, 0xD, 0x0
            , 0x1, 0x2, 0x3, 0xE, 0x6, 0xD, 0xB, 0x8, 0xF, 0xA, 0xC, 0x5, 0x7, 0x9, 0x0, 0x4
            , 0xE, 0x9, 0x3, 0x7, 0xF, 0x4, 0xC, 0xB, 0x6, 0xA, 0xD, 0x1, 0x0, 0x5, 0x8, 0x2
            , 0xA, 0xD, 0xC, 0x7, 0x6, 0xE, 0x8, 0x1, 0xF, 0x3, 0xB, 0x4, 0x0, 0x9, 0x5, 0x2
            , 0x4, 0xB, 0x1, 0xF, 0x9, 0x2, 0xE, 0xC, 0x6, 0xA, 0x8, 0x7, 0x3, 0x5, 0x0, 0xD
            , 0x4, 0x5, 0x1, 0xC, 0x7, 0xE, 0x9, 0x2, 0xA, 0xF, 0xB, 0xD, 0x0, 0x8, 0x6, 0x3
            , 0xC, 0xB, 0x3, 0x9, 0xF, 0x0, 0x4, 0x5, 0x7, 0x2, 0xE, 0xD, 0x1, 0xA, 0x8, 0x6
            , 0x8, 0x7, 0x3, 0xA, 0x9, 0x6, 0xE, 0x5, 0xD, 0x0, 0x4, 0xC, 0x1, 0x2, 0xF, 0xB
            , 0xF, 0x0, 0xE, 0x6, 0x8, 0xD, 0x5, 0x9, 0xA, 0x3, 0x1, 0xC, 0x4, 0xB, 0x7, 0x2
            , 0x4, 0x3, 0xE, 0xD, 0x5, 0x0, 0x2, 0xB, 0x1, 0xA, 0x7, 0x6, 0x9, 0xF, 0x8, 0x6C*/
    };


    private final int[] HeysSBoxInv = new int[]{
           // 1: 0xD, 0x9, 0xF, 0xA, 0x6, 0x7, 0x3, 0xC, 0xE, 0x1, 0x0, 0x5, 0xB, 0x2, 0x4, 0x8
            0x1, 0xA, 0x8, 0x9, 0x3, 0xC, 0x5, 0x6, 0x0, 0x4, 0xE, 0x7, 0x2, 0xF, 0xD, 0xB

    };

    private final int BLOCK_SIZE_BITS = 16;
    private final int KEY_SIZE_BITS = 112;
    private final int ROUND_KEY_SIZE_BITS = 16;

    private final long mask16 = (1L << 16L) - 1L;
    private final int ROUNDS = 6;
    private final int SBLOCKS_NUMBER = 4;
    private final int N = 4;


    public int encryptBlock(int block, int[] keys) {


        for (int r = 0; r < 6; r++) {

            block = encryptRound(block, keys[r]);
        }
        block = block ^ keys[6];

        return block;

    }

    private int encryptRound(int block, int roundKey) {

        var y = block ^ roundKey;


        return encryptRound(y);
    }

    private int encryptRound(int y) {

        return L(S(y));

    }

    private int[] S(int y) {

        int[] ys = new int[N];
        ys[0] = HeysSBox[(int) (y & ((1L << 4) - 1))];
        ys[1] = HeysSBox[(int) ((y >> 4) & ((1L << 4) - 1))];
        ys[2] = HeysSBox[(int) ((y >> 8) & ((1L << 4) - 1))];
        ys[3] = HeysSBox[y >> 12];
        return ys;
    }

    public int SInv(int y) {

        int[] ys = new int[N];
        ys[0] = HeysSBoxInv[(int) (y & ((1L << 4) - 1))];
        ys[1] = HeysSBoxInv[(int) ((y >> 4) & ((1L << 4) - 1))];
        ys[2] = HeysSBoxInv[(int) ((y >> 8) & ((1L << 4) - 1))];
        ys[3] = HeysSBoxInv[y >> 12];
        return (ys[0] << 12) ^ (ys[1] << 8) ^ (ys[2] << 4) ^ ys[3];
    }

    public int L(int[] ys) {
        int z = 0;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                z = z | ((ys[i] & (1 << j)) == 0 ? 0 : 1) << (j * N + i);
            }
        }

        return z;
    }

   /* @staticmethod
    def substitution(bytes):
            return [(S[(bytes[0] >> WORD_LEN) & 0xf] << WORD_LEN)  ^ S[bytes[0] & 0xf], ( S[(bytes[1] >> WORD_LEN) & 0xf] << WORD_LEN) ^ S[bytes[1] & 0xf]]

    @staticmethod
    def substitution_(bytes):
            return [(S_[(bytes[0] >> WORD_LEN) & 0xf] << WORD_LEN) ^ S_[bytes[0] & 0xf], (S_[(bytes[1] >> WORD_LEN) & 0xf] << WORD_LEN) ^ S_[bytes[1] & 0xf]]
*/

    public Map<Integer, Map<Integer, Double>> computeRoundDdt() throws ExecutionException, InterruptedException {


        Map<Integer, Map<Integer, Double>> ddtMap = new HashMap<>();

        List<Future<Map<Integer, Map<Integer, Double>>>> futures = new ArrayList<>();

        Map<Integer, Integer> encRound = new HashMap<>();
        for (int a = 0; a < (1 << BLOCK_SIZE_BITS); a++) {
            encRound.put(a, encryptRound(a));

        }

        var batchSize = (1 << BLOCK_SIZE_BITS) / threads;
        for (int t = 0; t < threads; t++) {
            var future = threadpool.submit(new DdtBatchClass(batchSize * t,
                    t == threads - 1 ? (1 << BLOCK_SIZE_BITS) : batchSize * t + batchSize,
                    t, encRound, 0.01));
            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            ddtMap.putAll(futures.get(i).get());
        }

            /*for (int a = 0; a < (1 << BLOCK_SIZE_BITS); a++) {
                if (a % 1000 == 0) {
                    System.out.println("a " + a);
                }
                for (int dx = 0; dx < (1 << BLOCK_SIZE_BITS); dx++) {
                    int dy = encryptRound(a) ^ encryptRound(a ^ dx);
                    var dxMap = ddtMap.getOrDefault(dx, new HashMap<>());
                    dxMap.put(dy, dxMap.getOrDefault(dy, 0.) + probabilityBit);
                    ddtMap.put(dx, dxMap);
                }
            }*/
        threadpool.shutdown();
        return ddtMap;
    }
/*
    public String decryptBlock(String bitSequence, long[][] roundKeys) {
        ArrayUtils.reverse(roundKeys);
        return encryptBlock(bitSequence, roundKeys);

    }*/

  
/*
        FactorialCalculator task = new FactorialCalculator(10);
        System.out.println("Submitting Task ...");

        Future future = threadpool.submit(task);

        System.out.println("Task is submitted");

        while (!future.isDone()) {
            System.out.println("Task is not completed yet....");
            Thread.sleep(1); //sleep for 1 millisecond before checking again
        }

        System.out.println("Task is completed, let's check result");
        long factorial = future.get();
        System.out.println("Factorial of 1000000 is : " + factorial);

        threadpool.shutdown();*/
    //}
}
