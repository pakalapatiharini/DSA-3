import java.util.LinkedHashMap;
import java.util.Map;

public class ShoppingCart {

    private final Map<String, Integer> items = new LinkedHashMap<>();

    public void addItem(String productName, int quantity) {
        if (quantity <= 0) return;
        items.merge(productName.toLowerCase(), quantity, Integer::sum);
    }

    public boolean removeItem(String productName) {
        return items.remove(productName.toLowerCase()) != null;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public double printCartAndGetTotal(ProductCatalog catalog) {
        double total = 0.0;
        System.out.println("\n--- Your Cart ---");
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Product product = catalog.getProduct(entry.getKey());
            if (product == null) continue;
            int qty = entry.getValue();
            double subtotal = product.price * qty;
            total += subtotal;
            System.out.printf("  %-15s x%-3d  Rs.%-10.2f Subtotal: Rs.%.2f%n",
                    capitalize(entry.getKey()), qty, product.price, subtotal);
        }
        System.out.printf("  ----------------------------------------%n");
        System.out.printf("  Total: Rs.%.2f%n", total);
        return total;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
