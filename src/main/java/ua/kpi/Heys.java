package ua.kpi;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Heys {

    private static final int threads = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService threadpool = Executors.newFixedThreadPool(threads);

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
            for (int dx = dxStart; dx < dxEnd; dx++) {
                var dxMap = ddtMap.getOrDefault(dx, new HashMap<>());
                for (int a = 0; a < (1 << BLOCK_SIZE_BITS); a++) {
                    int dy = encryptRound.get(a) ^ encryptRound.get(a ^ dx);
                    dxMap.put(dy, dxMap.getOrDefault(dy, 0.) + probabilityBit);

                }
                var dxVal = dxMap.entrySet().stream().parallel().filter(e -> e.getValue() > pMin).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

        threadpool.shutdown();
        return ddtMap;
    }


    public double[][] computeSboxDdt()  {

        var ddt = new double[HeysSBox.length][HeysSBox.length];

        double probabilityBit = 1. / HeysSBox.length;
        for (int dx = 0; dx < HeysSBox.length; dx++) {
            for (int a = 0; a < HeysSBox.length; a++) {
                int dy = HeysSBox[a] ^ HeysSBox[a ^ dx];
                ddt[dx][dy] = ddt[dx][dy] + probabilityBit;
            }
        }
        return ddt;
    }
}
