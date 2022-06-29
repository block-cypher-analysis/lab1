package ua.kpi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DifferentialSearch {

    private int alpha;
  //  private double pMin;

    public List<Map<Integer, Double>> search(Map<Integer, Map<Integer, Double>>  ddt, double[] pMins){

        List<Map<Integer, Double>> g = new ArrayList<>();
        g.add(new HashMap<>());
        g.get(0).put(alpha, 1.);

        for(int t=1;t<7;t++){
            var gPrev = g.get(t-1);
            var gCurr = new HashMap<Integer, Double>();

            gPrev.forEach((beta, p) -> {
                var ddtArr = ddt.get(beta);
                for(Map.Entry<Integer, Double> gammaQ : ddtArr.entrySet()) {
                    gCurr.put(gammaQ.getKey(), gCurr.getOrDefault(gammaQ.getKey(), 0.) +
                            gammaQ.getValue() * p);
                }/*
                for(int gamma=0; gamma < ddtArr.size(); gamma++){
                    var q = ddtArr.getOrDefault(gamma, 0.);
                    var qp = gCurr.getOrDefault(gamma, 0.);
                    gCurr.put(gamma, qp + q * p);
                }*/
            });
            var pMin = pMins[t-1];
            System.out.println("tt " + t + " " + gCurr.size());
            g.add(gCurr.entrySet().stream()
                    .filter(e -> e.getValue() > pMin)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            System.out.println("tt2 " + t + " " + g.get(t).size());
        }
        return g;

    }

}
