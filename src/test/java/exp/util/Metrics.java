package exp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Metrics {

    public static double percentile(List<Long> samples, double p) {

        if (samples == null) return Double.NaN;
        // null 제거 + 가변 리스트로 복사
        List<Long> copy = new ArrayList<>();
        for (Long v : samples) if (v != null) copy.add(v);
        if (copy.isEmpty()) return Double.NaN;

        Collections.sort(copy);

        // p 범위 보정
        if (p < 0) p = 0;
        if (p > 1) p = 1;

        double idx = (copy.size() - 1) * p;
        int lo = (int) Math.floor(idx);
        int hi = (int) Math.ceil(idx);
        if (lo == hi) return copy.get(lo);
        double w = idx - lo;

        return copy.get(lo) * (1 - w) + copy.get(hi) * w;
    }
}
