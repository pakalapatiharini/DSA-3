import java.util.ArrayList;
import java.util.List;

public class PriceSearch {

    public static List<Product> searchByRange(List<Product> sortedByPrice, double minPrice, double maxPrice) {
        List<Product> results = new ArrayList<>();
        if (sortedByPrice.isEmpty() || minPrice > maxPrice) return results;

        int lower = lowerBound(sortedByPrice, minPrice);
        int upper = upperBound(sortedByPrice, maxPrice);

        for (int i = lower; i <= upper && i < sortedByPrice.size(); i++) {
            results.add(sortedByPrice.get(i));
        }
        return results;
    }

    private static int lowerBound(List<Product> list, double target) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (list.get(mid).price < target) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    private static int upperBound(List<Product> list, double target) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (list.get(mid).price <= target) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo - 1;
    }
}
