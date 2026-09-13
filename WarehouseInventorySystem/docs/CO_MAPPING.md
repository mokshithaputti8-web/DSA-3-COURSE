# Feature → Class → Syllabus Module Mapping

All nine menu items live in **one file**: `src/WarehouseInventorySystem.java`.
Every algorithm class is named after **what it does**, not "Module 1" / "CO1" —
those labels never appear anywhere in the running program. This document is
the internal cross-reference used to confirm each feature is correctly
implementing the algorithm its syllabus module expects.

| # | Menu item | Implementing class | Syllabus module it demonstrates | Technique |
|---|---|---|---|---|
| 1 | Smart Product Search Bar | `SmartProductSearch` | Module 1 — Query classes: fuzzy matching / document similarity | Jaccard similarity over tokenised text |
| 2 | Catalog Keyword Finder | `CatalogKeywordFinder` | Module 2 — String Algorithms | Knuth-Morris-Pratt (KMP) exact pattern search with the LPS/failure array |
| 3 | Did-You-Mean Suggester | `DidYouMeanSuggester` | Module 3 — Advanced Dynamic Programming | Wagner-Fischer DP table for Levenshtein edit distance |
| 4 | Max Shipment Capacity Calc | `MaxShipmentCapacityCalc` | Module 4 — Network Flow | Ford-Fulkerson method, Edmonds-Karp (BFS-chosen augmenting paths) |
| 5 | Minimum Monitoring Points | `MinimumMonitoringPoints` | Module 5 — NP-Completeness and Approximation | 2-approximation for the NP-hard Minimum Vertex Cover problem (greedy maximal matching) |
| 6 | Inventory Sorter + Speed Test | `InventorySorterSpeedTest` | Module 6 — Randomised and Parallel Algorithms | Randomized QuickSort (Las Vegas) + sequential-vs-`.parallel()` stream benchmark |
| 7 | Low-Stock Checker | `LowStockChecker` | Applied data-structures / general algorithm design (supporting feature, not tied to one module) | Linear threshold scan + comparator-based urgency sort |
| 8 | Supplier Lookup | `SupplierLookup` + `SimpleHashTable` | Hashing fundamentals (foundational DSA topic, built by hand rather than using `java.util.HashMap`) | Custom separate-chaining hash table, O(1) average-case lookup |
| 9 | Run ALL | — | Runs 1–8 back to back | — |
| 0 | Exit | — | Ends the session and flushes `results/run_log.txt` | — |

## Supporting (non-algorithm) classes

- `Product`, `Supplier` — plain data holders for one catalog / supplier row.
- `InventoryDataManager` — reads `data/products.csv`, `data/routes.csv`, and
  `data/suppliers.csv` from disk.
- `WarehouseInventorySystem` — the single `public class` (entry point) that
  owns `main`, the menu, and the demo/logging glue. This is the only public
  class, so it is also the file name, as Java requires.

## Why one file

Every implementation is required to live in a single file inside `src/`
instead of being split one-class-per-file. Java allows multiple top-level
classes in one `.java` file as long as only one of them is `public` (and that
one must match the file name) — that rule is what
`WarehouseInventorySystem.java` follows.
