# Warehouse Inventory System — Report Template

## 1. Title
Warehouse Inventory System using String, Dynamic Programming, Network Flow,
Approximation, Randomized/Parallel, and Hashing Algorithms

## 2. Team
- Member 1: Mokshitha (2520030396)
- Member 2:
- ID:

## 3. Supervisor
- Name:

## 4. Abstract
This project implements nine practical warehouse-inventory features inside a
single Java program (`src/WarehouseInventorySystem.java`), sharing one common
dataset: a product catalog (`data/products.csv`), a delivery-route network
(`data/routes.csv`), and a supplier directory (`data/suppliers.csv`). It
covers fuzzy/document-similarity search, KMP string matching, edit-distance
dynamic programming, Edmonds-Karp max-flow, a vertex-cover approximation for
an NP-hard problem, randomized + parallel algorithms, a threshold-based
low-stock checker, and a hand-built hash table for supplier lookups.

## 5. Feature-wise implementation
See `docs/CO_MAPPING.md` for the full table of menu item -> class -> syllabus
module -> technique. Add a screenshot of the terminal output for each feature.

## 6. How to run
```
javac -d bin src/WarehouseInventorySystem.java
java  -cp bin WarehouseInventorySystem
```
Choose 1-8 to run one feature, 9 to run all of them, or 0 to exit.
For a fully automatic run (no typing): `java -cp bin WarehouseInventorySystem demo`

## 7. Results
Add screenshots of:
1. Smart Product Search Bar — ranked similarity results
2. Catalog Keyword Finder — KMP matches + LPS array
3. Did-You-Mean Suggester — Wagner-Fischer DP table
4. Max Shipment Capacity Calc — augmenting paths + max-flow value
5. Minimum Monitoring Points — the monitoring-point set
6. Inventory Sorter + Speed Test — sorted output + parallel vs sequential timings
7. Low-Stock Checker — flagged items + reorder suggestions
8. Supplier Lookup — a supplier's contact record
9. `results/run_log.txt` proving every run is saved to disk

## 8. Complexity summary
| # | Feature | Algorithm | Time complexity |
|---|---|---|---|
| 1 | Smart Product Search Bar | Jaccard similarity | O(N) over catalog, O(t) per comparison |
| 2 | Catalog Keyword Finder | KMP | O(n + m) |
| 3 | Did-You-Mean Suggester | Wagner-Fischer | O(n * m) |
| 4 | Max Shipment Capacity Calc | Edmonds-Karp | O(V * E^2) worst case |
| 5 | Minimum Monitoring Points | Vertex cover 2-approx | O(E) |
| 6 | Inventory Sorter + Speed Test | Randomized QuickSort | O(n log n) average |
| 7 | Low-Stock Checker | Linear scan + sort | O(n) scan, O(n log n) sort |
| 8 | Supplier Lookup | Custom hash table | O(1) average lookup |

## 9. Conclusion
Summarize how one shared warehouse dataset was used to demonstrate string,
dynamic-programming, flow, approximation, randomized/parallel, and hashing
algorithms end-to-end, and note the guarantees/limits of each (e.g. the
monitoring-point set is only a 2-approximation, not always optimal; the
low-stock reorder amount is a simple heuristic, not a demand forecast).
