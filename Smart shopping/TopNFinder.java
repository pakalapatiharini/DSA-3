import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TopNFinder {

    public static List<Product> cheapest(List<Product> products, int n) {
        PriorityQueue<Product> maxHeap = new PriorityQueue<>(
                Comparator.comparingDouble((Product p) -> p.price).reversed());

        for (Product p : products) {
            maxHeap.offer(p);
            if (maxHeap.size() > n) {
                maxHeap.poll();
            }
        }
        List<Product> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(p -> p.price));
        return result;
    }

    public static List<Product> mostExpensive(List<Product> products, int n) {
        PriorityQueue<Product> minHeap = new PriorityQueue<>(
                Comparator.comparingDouble(p -> p.price));

        for (Product p : products) {
            minHeap.offer(p);
            if (minHeap.size() > n) {
                minHeap.poll();
            }
        }
        List<Product> result = new ArrayList<>(minHeap);
        result.sort(Collections.reverseOrder(Comparator.comparingDouble(p -> p.price)));
        return result;
    }
}
