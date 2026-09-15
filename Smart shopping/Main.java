import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {

    private static final SearchHistory history = new SearchHistory();
    private static final ShoppingCart cart = new ShoppingCart();

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        ProductCatalog catalog = new ProductCatalog();
        catalog.loadFromCsv(args.length > 0 ? args[0] : "products.csv");

        FuzzySearchEngine engine = new FuzzySearchEngine(catalog.getNameTrie());
        AutoCorrect autoCorrect = new AutoCorrect(engine);

        System.out.println("=================================================");
        System.out.println(" Dictionary-Based Fuzzy Search & Spell Correction");
        System.out.println("          Engine for Smart Shopping");
        System.out.println("=================================================");
        System.out.println("Catalog loaded: " + catalog.size() + " items across "
                + catalog.getAllCategories().size() + " categories");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {
            printMenu();
            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "12";

            switch (choice) {
                case "1" -> searchItem(scanner, catalog, autoCorrect);
                case "2" -> browseAll(catalog);
                case "3" -> browseByCategory(scanner, catalog);
                case "4" -> autocomplete(scanner, catalog);
                case "5" -> keywordSearch(scanner, catalog);
                case "6" -> trendingSearches();
                case "7" -> priceRangeSearch(scanner, catalog);
                case "8" -> topNSearch(scanner, catalog);
                case "9" -> cartMenu(scanner, catalog);
                case "10" -> {
                    System.out.println("\n--- Catalog Structure (Trie) ---");
                    catalog.getNameTrie().printTree();
                }
                case "11" -> {
                    scanner.close();
                    System.out.println("Thank you for shopping with us!");
                    return;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1.  Search for an item (with spell correction)");
        System.out.println("2.  Browse all items");
        System.out.println("3.  Browse items by category");
        System.out.println("4.  Autocomplete (type a prefix)");
        System.out.println("5.  Keyword / substring search");
        System.out.println("6.  Trending searches");
        System.out.println("7.  Search by price range (Binary Search)");
        System.out.println("8.  Top-N cheapest / most expensive (Heap)");
        System.out.println("9.  Shopping cart");
        System.out.println("10. View catalog structure (Trie)");
        System.out.println("11. Exit");
        System.out.print("Choose an option: ");
    }

    private static void searchItem(Scanner scanner, ProductCatalog catalog, AutoCorrect autoCorrect) {
        System.out.print("Enter item name: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) return;

        history.record(query);

        if (catalog.getNameTrie().containsExact(query)) {
            System.out.println("\nItem found:");
            System.out.println(catalog.getProduct(query).toDetailString());
            return;
        }

        int maxDistance = maxDistanceFor(query);
        String corrected = autoCorrect.correctWord(query, maxDistance);

        if (corrected == null) {
            System.out.println("  No item found matching \"" + query + "\", and no close spelling match either.");
            return;
        }

        System.out.println("  Did you mean: \"" + corrected + "\"?");
        System.out.println("\nItem found:");
        System.out.println(catalog.getProduct(corrected).toDetailString());
    }

    private static void browseAll(ProductCatalog catalog) {
        System.out.println("\n--- All Items (" + catalog.size() + ") ---");
        for (Product p : catalog.getAllProducts()) {
            System.out.println("  " + p);
        }
    }

    private static void browseByCategory(Scanner scanner, ProductCatalog catalog) {
        TreeSet<String> categories = catalog.getAllCategories();
        System.out.println("\nAvailable categories:");
        int i = 1;
        for (String c : categories) {
            System.out.println("  " + (i++) + ". " + c);
        }
        System.out.print("Type a category name: ");
        String chosen = resolveCategory(scanner.nextLine().trim(), categories);

        List<Product> items = catalog.getCategoryProductObjects(chosen);
        if (items.isEmpty()) {
            System.out.println("  No category found with that name.");
        } else {
            System.out.println("\n--- " + chosen + " (" + items.size() + " items) ---");
            for (Product p : items) {
                System.out.println("  " + p);
            }
        }
    }

    private static void autocomplete(Scanner scanner, ProductCatalog catalog) {
        System.out.print("Start typing an item name: ");
        String prefix = scanner.nextLine().trim().toLowerCase();
        if (prefix.isEmpty()) return;

        List<String> matches = catalog.getNameTrie().getWordsWithPrefix(prefix, 10);
        if (matches.isEmpty()) {
            System.out.println("  No items start with \"" + prefix + "\"");
        } else {
            System.out.println("  Suggestions:");
            for (String name : matches) {
                System.out.println("    - " + catalog.getProduct(name));
            }
        }
    }

    private static void keywordSearch(Scanner scanner, ProductCatalog catalog) {
        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        if (keyword.isEmpty()) return;

        List<String> allNames = catalog.getAllNames();
        List<String> matches = RabinKarp.searchDictionary(allNames, keyword);

        if (matches.isEmpty()) {
            System.out.println("  No items contain \"" + keyword + "\"");
        } else {
            System.out.println("  Items containing \"" + keyword + "\":");
            for (String name : matches) {
                System.out.println("    - " + catalog.getProduct(name));
            }
        }
    }

    private static void trendingSearches() {
        List<Map.Entry<String, Integer>> top = history.topSearches(5);
        if (top.isEmpty()) {
            System.out.println("  No searches recorded yet this session.");
        } else {
            System.out.println("\n--- Trending Searches ---");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : top) {
                System.out.println("  " + rank++ + ". " + entry.getKey() + "  (" + entry.getValue() + " searches)");
            }
        }
    }

    private static void priceRangeSearch(Scanner scanner, ProductCatalog catalog) {
        try {
            System.out.print("Minimum price: ");
            double min = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Maximum price: ");
            double max = Double.parseDouble(scanner.nextLine().trim());

            List<Product> sorted = catalog.getSortedByPrice();
            List<Product> results = PriceSearch.searchByRange(sorted, min, max);

            if (results.isEmpty()) {
                System.out.println("  No items found between Rs." + min + " and Rs." + max);
            } else {
                System.out.println("\n--- Items between Rs." + min + " and Rs." + max + " ---");
                for (Product p : results) {
                    System.out.println("  " + p);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("  Please enter valid numbers.");
        }
    }

    private static void topNSearch(Scanner scanner, ProductCatalog catalog) {
        TreeSet<String> categories = catalog.getAllCategories();
        System.out.println("\nAvailable categories:");
        for (String c : categories) {
            System.out.println("  - " + c);
        }
        System.out.print("Category (or 'all' for entire catalog): ");
        String categoryInput = scanner.nextLine().trim();

        List<Product> pool = categoryInput.equalsIgnoreCase("all")
                ? catalog.getAllProducts()
                : catalog.getCategoryProductObjects(resolveCategory(categoryInput, categories));

        if (pool.isEmpty()) {
            System.out.println("  No items found for that category.");
            return;
        }

        System.out.print("Cheapest or most expensive? (cheap/expensive): ");
        String mode = scanner.nextLine().trim().toLowerCase();
        System.out.print("How many items (N)? ");
        int n;
        try {
            n = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Please enter a valid number.");
            return;
        }

        List<Product> result = mode.startsWith("cheap")
                ? TopNFinder.cheapest(pool, n)
                : TopNFinder.mostExpensive(pool, n);

        System.out.println("\n--- Top " + result.size() + " (" + mode + ") ---");
        for (Product p : result) {
            System.out.println("  " + p);
        }
    }

    private static void cartMenu(Scanner scanner, ProductCatalog catalog) {
        while (true) {
            System.out.println("\n--- Shopping Cart ---");
            System.out.println("1. Add item");
            System.out.println("2. View cart & total");
            System.out.println("3. Remove item");
            System.out.println("4. Back to main menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Item name: ");
                    String name = scanner.nextLine().trim().toLowerCase();
                    if (!catalog.getNameTrie().containsExact(name)) {
                        System.out.println("  No such item in catalog. Try searching first (option 1 in main menu).");
                        break;
                    }
                    System.out.print("Quantity: ");
                    try {
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        cart.addItem(name, qty);
                        System.out.println("  Added " + qty + " x " + name + " to cart.");
                    } catch (NumberFormatException e) {
                        System.out.println("  Please enter a valid quantity.");
                    }
                }
                case "2" -> {
                    if (cart.isEmpty()) {
                        System.out.println("  Your cart is empty.");
                    } else {
                        cart.printCartAndGetTotal(catalog);
                    }
                }
                case "3" -> {
                    System.out.print("Item name to remove: ");
                    String name = scanner.nextLine().trim().toLowerCase();
                    System.out.println(cart.removeItem(name) ? "  Removed." : "  Item not in cart.");
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private static String resolveCategory(String input, TreeSet<String> categories) {
        for (String c : categories) {
            if (c.equalsIgnoreCase(input)) return c;
        }
        return input;
    }

    private static int maxDistanceFor(String query) {
        if (query.length() <= 4) return 2;
        if (query.length() <= 8) return 3;
        return 4;
    }
}
