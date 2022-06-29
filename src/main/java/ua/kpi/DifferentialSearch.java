package ua.kpi;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DifferentialSearch {

    private int alpha;

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
                }
            });
            var pMin = pMins[t-1];
            g.add(gCurr.entrySet().stream()
                    .filter(e -> e.getValue() > pMin)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return g;

    }

}
