import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * =====================================================================================
 *  WarehouseInventorySystem
 * =====================================================================================
 *  A single, professional command-line application for a warehouse inventory
 *  scenario. It exposes 9 menu options; each one is a real, working feature
 *  backed by its own named class below (no "Module 1 / Module 2" labels
 *  anywhere in the program output — every class and menu item is named after
 *  what it actually does):
 *
 *    1. Smart Product Search Bar     -> SmartProductSearch      (fuzzy / Jaccard similarity)
 *    2. Catalog Keyword Finder       -> CatalogKeywordFinder    (Knuth-Morris-Pratt)
 *    3. Did-You-Mean Suggester       -> DidYouMeanSuggester     (Wagner-Fischer edit distance)
 *    4. Max Shipment Capacity Calc   -> MaxShipmentCapacityCalc (Edmonds-Karp max-flow)
 *    5. Minimum Monitoring Points    -> MinimumMonitoringPoints (vertex-cover 2-approximation)
 *    6. Inventory Sorter + SpeedTest -> InventorySorterSpeedTest(randomized quicksort + parallel benchmark)
 *    7. Low-Stock Checker            -> LowStockChecker         (threshold scan + urgency sort)
 *    8. Supplier Lookup              -> SupplierLookup          (custom hash table, O(1) average lookup)
 *    9. Run ALL of the above as one demo
 *    0. Exit
 *
 *  RUN (from the project root, in any terminal / VS Code terminal):
 *      javac -d bin src/WarehouseInventorySystem.java
 *      java  -cp bin WarehouseInventorySystem
 *
 *  Non-interactive / auto-demo mode (runs every feature once, no typing required):
 *      java  -cp bin WarehouseInventorySystem demo
 *
 *  Every run is also written to results/run_log.txt so you always have proof of
 *  output even after the terminal is closed.
 * =====================================================================================
 */
public class WarehouseInventorySystem {

    private static final List<String> LOG = new ArrayList<>();

    private static void out(String line) {
        System.out.println(line);
        LOG.add(line);
    }

    private static void banner(String title) {
        out("");
        out("=".repeat(90));
        out(title);
        out("=".repeat(90));
    }

    public static void main(String[] args) {
        List<Product> products;
        List<InventoryDataManager.Edge> routes;
        List<Supplier> suppliers;

        try {
            products = InventoryDataManager.loadProducts("data/products.csv");
            routes = InventoryDataManager.loadRoutes("data/routes.csv");
            suppliers = InventoryDataManager.loadSuppliers("data/suppliers.csv");
        } catch (IOException e) {
            System.out.println("Could not read data files (run this from the project root folder): " + e.getMessage());
            return;
        }

        SupplierLookup supplierLookup = new SupplierLookup(suppliers);

        boolean autoDemo = args.length > 0 && args[0].equalsIgnoreCase("demo");
        Scanner scanner = autoDemo ? null : new Scanner(System.in);

        if (autoDemo) {
            runFullDemo(products, routes, supplierLookup);
            saveLog();
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice;
            try {
                if (!scanner.hasNextLine()) {                 // stdin closed / piped with nothing left
                    out("\n(No more input detected - falling back to full automatic demo.)");
                    runFullDemo(products, routes, supplierLookup);
                    break;
                }
                choice = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                runFullDemo(products, routes, supplierLookup);
                break;
            }

            switch (choice) {
                case "1": featureSmartProductSearch(products, scanner); break;
                case "2": featureCatalogKeywordFinder(products, scanner); break;
                case "3": featureDidYouMeanSuggester(products, scanner); break;
                case "4": featureMaxShipmentCapacity(routes); break;
                case "5": featureMinimumMonitoringPoints(routes); break;
                case "6": featureInventorySorterSpeedTest(products); break;
                case "7": featureLowStockChecker(products); break;
                case "8": featureSupplierLookup(products, supplierLookup, scanner); break;
                case "9": runFullDemo(products, routes, supplierLookup); break;
                case "0": running = false; break;
                default: out("Please choose a number from the menu (0-9).");
            }
        }

        saveLog();
        out("\nSession finished. Full transcript saved to results/run_log.txt");
        if (scanner != null) scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("--------------------------------------------------------------");
        System.out.println(" WAREHOUSE INVENTORY SYSTEM");
        System.out.println("--------------------------------------------------------------");
        System.out.println(" 1. Smart Product Search Bar");
        System.out.println(" 2. Catalog Keyword Finder");
        System.out.println(" 3. Did-You-Mean Suggester");
        System.out.println(" 4. Max Shipment Capacity Calc");
        System.out.println(" 5. Minimum Monitoring Points");
        System.out.println(" 6. Inventory Sorter + Speed Test");
        System.out.println(" 7. Low-Stock Checker");
        System.out.println(" 8. Supplier Lookup");
        System.out.println(" 9. Run ALL of the above as one demo");
        System.out.println(" 0. Exit");
        System.out.print(" Choose an option: ");
    }

    private static void runFullDemo(List<Product> products, List<InventoryDataManager.Edge> routes, SupplierLookup supplierLookup) {
        featureSmartProductSearch(products, null);
        featureCatalogKeywordFinder(products, null);
        featureDidYouMeanSuggester(products, null);
        featureMaxShipmentCapacity(routes);
        featureMinimumMonitoringPoints(routes);
        featureInventorySorterSpeedTest(products);
        featureLowStockChecker(products);
        featureSupplierLookup(products, supplierLookup, null);
    }

    private static void saveLog() {
        try {
            Files.createDirectories(Paths.get("results"));
            Path logPath = Paths.get("results", "run_log.txt");
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            StringBuilder sb = new StringBuilder();
            sb.append("\n\n############## RUN AT ").append(stamp).append(" ##############\n");
            for (String line : LOG) sb.append(line).append(System.lineSeparator());
            Files.write(logPath, sb.toString().getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("(Could not write results/run_log.txt: " + e.getMessage() + ")");
        }
    }

    // =================================================================================
    // 1. Smart Product Search Bar  ->  SmartProductSearch (fuzzy / Jaccard similarity)
    // =================================================================================
    private static void featureSmartProductSearch(List<Product> products, Scanner scanner) {
        banner("1. SMART PRODUCT SEARCH BAR");
        String query = "wireles mouse electronic";
        if (scanner != null) {
            System.out.print("Enter a (possibly misspelled) search phrase [default: \"" + query + "\"]: ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) query = line;
        }
        out("Query: \"" + query + "\"");
        out(String.format("%-20s %-10s %-12s %-10s %s", "PRODUCT", "CATEGORY", "SUPPLIER", "SCORE", "NOTE"));
        List<Map.Entry<Product, Double>> ranked = SmartProductSearch.rankBySimilarity(query, products);
        for (int i = 0; i < Math.min(5, ranked.size()); i++) {
            Map.Entry<Product, Double> e = ranked.get(i);
            String note = (i == 0) ? "<- best match" : "";
            out(String.format("%-20s %-10s %-12s %-10.2f %s",
                    e.getKey().name, e.getKey().category, e.getKey().supplier, e.getValue(), note));
        }
        out("Technique: Jaccard similarity over tokenised text (product name + category + supplier).");
        out("Complexity: O(t1 + t2) per comparison where t1, t2 are token counts; O(N) over the catalog.");
    }

    // =================================================================================
    // 2. Catalog Keyword Finder  ->  CatalogKeywordFinder (Knuth-Morris-Pratt)
    // =================================================================================
    private static void featureCatalogKeywordFinder(List<Product> products, Scanner scanner) {
        banner("2. CATALOG KEYWORD FINDER");
        StringBuilder catalogText = new StringBuilder();
        List<int[]> productSpan = new ArrayList<>(); // [start, end, productIndex] in catalogText
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            String chunk = p.name + " ";
            productSpan.add(new int[]{catalogText.length(), catalogText.length() + chunk.length() - 1, i});
            catalogText.append(chunk);
        }
        String text = catalogText.toString();
        String pattern = "Cable";
        if (scanner != null) {
            System.out.print("Enter a keyword to search for in the catalog [default: \"" + pattern + "\"]: ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) pattern = line;
        }
        int[] lps = CatalogKeywordFinder.computeLPS(pattern);
        List<Integer> matches = CatalogKeywordFinder.search(text, pattern);

        out("Catalog text (concatenated product names): \"" + text.trim() + "\"");
        out("Keyword: \"" + pattern + "\"   LPS (failure) array: " + Arrays.toString(lps));
        if (matches.isEmpty()) {
            out("No occurrences found.");
        } else {
            out("Occurrences found at character index(es): " + matches);
            for (int idx : matches) {
                for (int[] span : productSpan) {
                    if (idx >= span[0] && idx <= span[1]) {
                        out("  -> inside product: " + products.get(span[2]).name);
                        break;
                    }
                }
            }
        }
        out("Complexity: O(n + m) - n = text length, m = keyword length (no re-scanning of text on mismatch).");
    }

    // =================================================================================
    // 3. Did-You-Mean Suggester  ->  DidYouMeanSuggester (Wagner-Fischer edit distance)
    // =================================================================================
    private static void featureDidYouMeanSuggester(List<Product> products, Scanner scanner) {
        banner("3. DID-YOU-MEAN SUGGESTER");
        String typo = "Keybord";
        if (scanner != null) {
            System.out.print("Type a misspelled product name [default: \"" + typo + "\"]: ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) typo = line;
        }
        Map.Entry<Product, Integer> best = DidYouMeanSuggester.suggestClosest(typo, products);
        out("You typed:      \"" + typo + "\"");
        out("Did you mean:   \"" + best.getKey().name + "\"  (edit distance = " + best.getValue() + ")");

        out("");
        out("Wagner-Fischer DP table for \"" + typo + "\" vs \"" + best.getKey().name + "\":");
        int[][] table = DidYouMeanSuggester.wagnerFischerTable(typo.toLowerCase(), best.getKey().name.toLowerCase());
        printDpTable(typo.toLowerCase(), best.getKey().name.toLowerCase(), table);
        out("Complexity: O(n*m) time and O(n*m) space (n, m = string lengths); each cell is a min of 3 sub-problems.");
    }

    private static void printDpTable(String a, String b, int[][] table) {
        StringBuilder header = new StringBuilder("        ");
        header.append("  #");
        for (char c : b.toCharArray()) header.append("  ").append(c);
        out(header.toString());
        for (int i = 0; i <= a.length(); i++) {
            StringBuilder row = new StringBuilder();
            row.append(i == 0 ? "  #" : "  " + a.charAt(i - 1)).append(" ");
            for (int j = 0; j <= b.length(); j++) {
                row.append(String.format("%3d", table[i][j]));
            }
            out(row.toString());
        }
    }

    // =================================================================================
    // 4. Max Shipment Capacity Calc  ->  MaxShipmentCapacityCalc (Edmonds-Karp max-flow)
    // =================================================================================
    private static void featureMaxShipmentCapacity(List<InventoryDataManager.Edge> routes) {
        banner("4. MAX SHIPMENT CAPACITY CALC");
        MaxShipmentCapacityCalc flow = new MaxShipmentCapacityCalc();
        for (InventoryDataManager.Edge e : routes) flow.addEdge(e.from, e.to, e.capacity);

        String source = "Warehouse";
        String sink = "StoreD";
        out("Source: " + source + "   Sink: " + sink);
        out("Routes (from -> to : capacity):");
        for (InventoryDataManager.Edge e : routes) out("  " + e.from + " -> " + e.to + " : " + e.capacity);

        int maxFlow = flow.computeMaxFlow(source, sink, WarehouseInventorySystem::out);
        out("MAXIMUM SHIPMENT CAPACITY " + source + " -> " + sink + " = " + maxFlow + " units");
        out("Complexity: O(V * E^2) worst case for Edmonds-Karp (BFS-chosen augmenting paths).");
    }

    // =================================================================================
    // 5. Minimum Monitoring Points  ->  MinimumMonitoringPoints (vertex-cover 2-approx)
    // =================================================================================
    private static void featureMinimumMonitoringPoints(List<InventoryDataManager.Edge> routes) {
        banner("5. MINIMUM MONITORING POINTS");
        Set<String> nodes = new LinkedHashSet<>();
        List<String[]> edges = new ArrayList<>();
        for (InventoryDataManager.Edge e : routes) {
            nodes.add(e.from);
            nodes.add(e.to);
            edges.add(new String[]{e.from, e.to});
        }
        out("Undirected network derived from the same routes (capacities ignored for this feature):");
        for (String[] e : edges) out("  " + e[0] + " -- " + e[1]);

        Set<String> cover = MinimumMonitoringPoints.approximate(edges);
        out("Approximate minimum set of monitoring points (touches every route): " + cover);
        out("Point count = " + cover.size() + " out of " + nodes.size() + " total distribution points.");
        out("Guarantee: this greedy-maximal-matching set is at most 2x the size of an OPTIMAL solution.");
        out("Complexity: O(E) - each matched edge removes itself and every adjacent edge in one pass.");
    }

    // =================================================================================
    // 6. Inventory Sorter + Speed Test  ->  InventorySorterSpeedTest
    // =================================================================================
    private static void featureInventorySorterSpeedTest(List<Product> products) {
        banner("6. INVENTORY SORTER + SPEED TEST");
        int[] quantities = products.stream().mapToInt(p -> p.quantity).toArray();
        out("Quantities before sort: " + Arrays.toString(quantities));
        int[] copy = Arrays.copyOf(quantities, quantities.length);
        InventorySorterSpeedTest.randomizedQuickSort(copy, 0, copy.length - 1, new Random());
        out("Quantities after randomized quicksort: " + Arrays.toString(copy));
        out("Sorted correctly: " + isSorted(copy));
        out("Expected complexity: O(n log n) average case (Las Vegas - always correct, running time is the random variable).");

        out("");
        out("Speed test (sequential vs. parallel sum of a large synthetic array):");
        int n = 20_000_000;
        long[] timings = InventorySorterSpeedTest.parallelVsSequentialBenchmark(n);
        out("  Array size: " + n);
        out("  Sequential stream sum time : " + timings[0] + " ms");
        out("  Parallel stream sum time   : " + timings[1] + " ms");
        double speedup = timings[1] == 0 ? 0 : (double) timings[0] / timings[1];
        out(String.format("  Observed speed-up: %.2fx (varies with machine core count and JIT warm-up)", speedup));
    }

    private static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) if (arr[i - 1] > arr[i]) return false;
        return true;
    }

    // =================================================================================
    // 7. Low-Stock Checker  ->  LowStockChecker (threshold scan + urgency sort)
    // =================================================================================
    private static void featureLowStockChecker(List<Product> products) {
        banner("7. LOW-STOCK CHECKER");
        List<Product> low = LowStockChecker.findLowStock(products);
        if (low.isEmpty()) {
            out("No products are at or below their minimum stock threshold. Everything is healthy.");
        } else {
            out(String.format("%-20s %-10s %8s %10s %12s", "PRODUCT", "SUPPLIER", "QTY", "MIN STOCK", "REORDER QTY"));
            for (Product p : low) {
                int reorder = LowStockChecker.suggestedReorderQuantity(p);
                out(String.format("%-20s %-10s %8d %10d %12d", p.name, p.supplier, p.quantity, p.minStock, reorder));
            }
        }
        out("Technique: single linear scan (quantity <= minStock) then sort by shortage, most urgent first.");
        out("Complexity: O(n) to scan + O(n log n) to sort the flagged items.");
    }

    // =================================================================================
    // 8. Supplier Lookup  ->  SupplierLookup (custom hash table, O(1) average lookup)
    // =================================================================================
    private static void featureSupplierLookup(List<Product> products, SupplierLookup supplierLookup, Scanner scanner) {
        banner("8. SUPPLIER LOOKUP");
        String supplierName = products.isEmpty() ? "" : products.get(1).supplier; // e.g. "KeyWorks"
        if (scanner != null) {
            System.out.print("Enter a supplier name to look up [default: \"" + supplierName + "\"]: ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) supplierName = line;
        }
        Supplier s = supplierLookup.lookup(supplierName);
        if (s == null) {
            out("No supplier record found for \"" + supplierName + "\".");
        } else {
            out("Supplier record for \"" + supplierName + "\":");
            out("  Contact person : " + s.contactPerson);
            out("  Phone          : " + s.phone);
            out("  Email          : " + s.email);
            out("  Lead time      : " + s.leadTimeDays + " day(s)");
            out("  Products supplied:");
            for (Product p : products) {
                if (p.supplier.equalsIgnoreCase(supplierName)) out("    - " + p.name + " (qty: " + p.quantity + ")");
            }
        }
        out("Technique: custom separate-chaining hash table keyed by supplier name.");
        out("Complexity: O(1) average-case lookup, O(n) worst case on heavy hash collisions.");
    }
}

// =====================================================================================
//  DATA MODEL
// =====================================================================================
class Product {
    final String id, name, category, supplier;
    final int quantity, minStock;
    final double price;

    Product(String id, String name, String category, int quantity, int minStock, double price, String supplier) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.minStock = minStock;
        this.price = price;
        this.supplier = supplier;
    }
}

class Supplier {
    final String name, contactPerson, phone, email;
    final int leadTimeDays;

    Supplier(String name, String contactPerson, String phone, String email, int leadTimeDays) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.leadTimeDays = leadTimeDays;
    }
}

// =====================================================================================
//  InventoryDataManager — reads the CSV corpus (products, routes, suppliers)
// =====================================================================================
class InventoryDataManager {

    static List<Product> loadProducts(String path) throws IOException {
        List<Product> products = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) { // skip header
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            products.add(new Product(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    Integer.parseInt(parts[3].trim()),
                    Integer.parseInt(parts[4].trim()),
                    Double.parseDouble(parts[5].trim()),
                    parts[6].trim()));
        }
        return products;
    }

    static List<Supplier> loadSuppliers(String path) throws IOException {
        List<Supplier> suppliers = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            suppliers.add(new Supplier(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Integer.parseInt(parts[4].trim())));
        }
        return suppliers;
    }

    static class Edge {
        final String from, to;
        final int capacity;

        Edge(String from, String to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
        }
    }

    static List<Edge> loadRoutes(String path) throws IOException {
        List<Edge> edges = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            edges.add(new Edge(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim())));
        }
        return edges;
    }
}

// =====================================================================================
//  1. SmartProductSearch — fuzzy / document-similarity product search
//  Technique: Jaccard similarity over tokenised text.
// =====================================================================================
class SmartProductSearch {

    static Set<String> tokenize(String s) {
        return Arrays.stream(s.toLowerCase().split("[^a-z0-9]+"))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static double jaccardSimilarity(String a, String b) {
        Set<String> setA = tokenize(a);
        Set<String> setB = tokenize(b);
        if (setA.isEmpty() && setB.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        return (double) intersection.size() / union.size();
    }

    static List<Map.Entry<Product, Double>> rankBySimilarity(String query, List<Product> products) {
        List<Map.Entry<Product, Double>> scored = new ArrayList<>();
        for (Product p : products) {
            String doc = p.name + " " + p.category + " " + p.supplier;
            scored.add(new AbstractMap.SimpleEntry<>(p, jaccardSimilarity(query, doc)));
        }
        scored.sort((x, y) -> Double.compare(y.getValue(), x.getValue()));
        return scored;
    }
}

// =====================================================================================
//  2. CatalogKeywordFinder — exact keyword search using Knuth-Morris-Pratt
// =====================================================================================
class CatalogKeywordFinder {

    static int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0, i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty()) return matches;
        int[] lps = computeLPS(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }
}

// =====================================================================================
//  3. DidYouMeanSuggester — Wagner-Fischer dynamic programming for edit distance
// =====================================================================================
class DidYouMeanSuggester {

    static int[][] wagnerFischerTable(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int replace = dp[i - 1][j - 1] + 1;
                    int delete = dp[i - 1][j] + 1;
                    int insert = dp[i][j - 1] + 1;
                    dp[i][j] = Math.min(replace, Math.min(delete, insert));
                }
            }
        }
        return dp;
    }

    static int editDistance(String a, String b) {
        int[][] dp = wagnerFischerTable(a, b);
        return dp[a.length()][b.length()];
    }

    static Map.Entry<Product, Integer> suggestClosest(String query, List<Product> products) {
        Product best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Product p : products) {
            int d = editDistance(query.toLowerCase(), p.name.toLowerCase());
            if (d < bestDistance) {
                bestDistance = d;
                best = p;
            }
        }
        return new AbstractMap.SimpleEntry<>(best, bestDistance);
    }
}

// =====================================================================================
//  4. MaxShipmentCapacityCalc — Ford-Fulkerson method with BFS-chosen augmenting
//  paths (Edmonds-Karp), applied to the warehouse -> hub -> store distribution network.
// =====================================================================================
class MaxShipmentCapacityCalc {

    private final Map<String, Map<String, Integer>> capacity = new HashMap<>();
    private final Set<String> nodes = new LinkedHashSet<>();

    void addEdge(String from, String to, int cap) {
        nodes.add(from);
        nodes.add(to);
        capacity.computeIfAbsent(from, k -> new HashMap<>()).merge(to, cap, Integer::sum);
        capacity.computeIfAbsent(to, k -> new HashMap<>()).putIfAbsent(from, 0); // reverse residual edge
    }

    /** Returns the max flow value from source to sink; reports each augmenting path via the logger. */
    int computeMaxFlow(String source, String sink, java.util.function.Consumer<String> logger) {
        int totalFlow = 0;
        int iteration = 1;
        Map<String, String> parent;
        while ((parent = bfsFindPath(source, sink)) != null) {
            int bottleneck = Integer.MAX_VALUE;
            String v = sink;
            List<String> path = new ArrayList<>();
            path.add(v);
            while (!v.equals(source)) {
                String u = parent.get(v);
                bottleneck = Math.min(bottleneck, capacity.get(u).get(v));
                v = u;
                path.add(v);
            }
            Collections.reverse(path);

            v = sink;
            while (!v.equals(source)) {
                String u = parent.get(v);
                capacity.get(u).merge(v, -bottleneck, Integer::sum);
                capacity.get(v).merge(u, bottleneck, Integer::sum);
                v = u;
            }
            totalFlow += bottleneck;
            logger.accept("  Augmenting path " + iteration + ": " + String.join(" -> ", path) + "  (bottleneck = " + bottleneck + ")");
            iteration++;
        }
        return totalFlow;
    }

    private Map<String, String> bfsFindPath(String source, String sink) {
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(source);
        visited.add(source);
        while (!queue.isEmpty()) {
            String u = queue.poll();
            if (u.equals(sink)) return parent;
            for (Map.Entry<String, Integer> edge : capacity.getOrDefault(u, Collections.emptyMap()).entrySet()) {
                String v = edge.getKey();
                int residual = edge.getValue();
                if (residual > 0 && !visited.contains(v)) {
                    visited.add(v);
                    parent.put(v, u);
                    queue.add(v);
                }
            }
        }
        return visited.contains(sink) ? parent : null;
    }
}

// =====================================================================================
//  5. MinimumMonitoringPoints — classic 2-approximation for the NP-hard MINIMUM
//  VERTEX COVER problem: repeatedly pick any uncovered edge, add both endpoints to
//  the cover, and discard every edge touching either endpoint (greedy maximal matching).
// =====================================================================================
class MinimumMonitoringPoints {

    static Set<String> approximate(List<String[]> edges) {
        List<String[]> remaining = new ArrayList<>(edges);
        Set<String> cover = new LinkedHashSet<>();
        while (!remaining.isEmpty()) {
            String[] edge = remaining.remove(0);
            String u = edge[0], v = edge[1];
            cover.add(u);
            cover.add(v);
            remaining.removeIf(e -> e[0].equals(u) || e[1].equals(u) || e[0].equals(v) || e[1].equals(v));
        }
        return cover;
    }
}

// =====================================================================================
//  6. InventorySorterSpeedTest
//  (a) Randomized QuickSort: a Las Vegas algorithm — always correct, expected O(n log n).
//  (b) A tiny embarrassingly-parallel benchmark contrasting sequential vs parallel streams.
// =====================================================================================
class InventorySorterSpeedTest {

    static void randomizedQuickSort(int[] arr, int lo, int hi, Random rnd) {
        if (lo >= hi) return;
        int pivotIndex = lo + rnd.nextInt(hi - lo + 1);
        swap(arr, pivotIndex, hi);
        int pivot = arr[hi];
        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (arr[j] < pivot) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, hi);
        randomizedQuickSort(arr, lo, i - 1, rnd);
        randomizedQuickSort(arr, i + 1, hi, rnd);
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /** Returns {sequentialMillis, parallelMillis} for summing a synthetic array of size n. */
    static long[] parallelVsSequentialBenchmark(int n) {
        int[] data = new Random(42).ints(n, 0, 1000).toArray();

        long t0 = System.nanoTime();
        long sequentialSum = IntStream.range(0, data.length).mapToLong(i -> data[i]).sum();
        long t1 = System.nanoTime();

        long t2 = System.nanoTime();
        long parallelSum = IntStream.range(0, data.length).parallel().mapToLong(i -> data[i]).sum();
        long t3 = System.nanoTime();

        if (sequentialSum != parallelSum) {
            throw new IllegalStateException("Sequential and parallel results disagree - should never happen.");
        }
        return new long[]{(t1 - t0) / 1_000_000, (t3 - t2) / 1_000_000};
    }
}

// =====================================================================================
//  7. LowStockChecker — threshold scan (quantity <= minStock) + urgency sort
// =====================================================================================
class LowStockChecker {

    static List<Product> findLowStock(List<Product> products) {
        List<Product> low = new ArrayList<>();
        for (Product p : products) {
            if (p.quantity <= p.minStock) low.add(p);
        }
        // Most urgent (biggest shortfall below the minimum) first.
        low.sort((a, b) -> (b.minStock - b.quantity) - (a.minStock - a.quantity));
        return low;
    }

    /** Simple restock heuristic: top the item back up to twice its minimum threshold. */
    static int suggestedReorderQuantity(Product p) {
        return Math.max(0, (p.minStock * 2) - p.quantity);
    }
}

// =====================================================================================
//  8. SupplierLookup — backed by SimpleHashTable, a custom separate-chaining
//  hash table (built from scratch instead of java.util.HashMap) for O(1) average
//  lookups of a supplier's contact details by name.
// =====================================================================================
class SimpleHashTable<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final Node<K, V>[] buckets;
    private int size = 0;

    @SuppressWarnings("unchecked")
    SimpleHashTable(int bucketCount) {
        this.buckets = new Node[Math.max(4, bucketCount)];
    }

    private int indexFor(K key) {
        return Math.floorMod(key.hashCode(), buckets.length);
    }

    void put(K key, V value) {
        int idx = indexFor(key);
        for (Node<K, V> n = buckets[idx]; n != null; n = n.next) {
            if (n.key.equals(key)) {
                n.value = value;
                return;
            }
        }
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[idx];
        buckets[idx] = newNode;
        size++;
    }

    V get(K key) {
        int idx = indexFor(key);
        for (Node<K, V> n = buckets[idx]; n != null; n = n.next) {
            if (n.key.equals(key)) return n.value;
        }
        return null;
    }

    int size() {
        return size;
    }

    int bucketCount() {
        return buckets.length;
    }
}

class SupplierLookup {

    private final SimpleHashTable<String, Supplier> index;

    SupplierLookup(List<Supplier> suppliers) {
        index = new SimpleHashTable<>(Math.max(8, suppliers.size() * 2));
        for (Supplier s : suppliers) index.put(s.name.toLowerCase(), s);
    }

    Supplier lookup(String supplierName) {
        return index.get(supplierName.toLowerCase());
    }
}
