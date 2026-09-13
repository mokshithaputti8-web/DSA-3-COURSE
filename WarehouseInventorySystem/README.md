# Warehouse Inventory System

A single, professional Java command-line application for a warehouse
inventory scenario. It has **9 real, working features**, each backed by its
own named class — nothing is labeled "Module 1 / Module 2" anywhere in the
program:

| # | Feature |
|---|---|
| 1 | Smart Product Search Bar |
| 2 | Catalog Keyword Finder |
| 3 | Did-You-Mean Suggester |
| 4 | Max Shipment Capacity Calc |
| 5 | Minimum Monitoring Points |
| 6 | Inventory Sorter + Speed Test |
| 7 | Low-Stock Checker |
| 8 | Supplier Lookup |
| 9 | Run ALL of the above as one demo |
| 0 | Exit |

For the internal mapping of each feature to the algorithm/syllabus module it
actually demonstrates (useful for your report, not shown in the program
itself), see [`docs/CO_MAPPING.md`](docs/CO_MAPPING.md) and
[`docs/FLOWCHARTS.md`](docs/FLOWCHARTS.md).

## Folder structure

```text
WarehouseInventorySystem/
├── README.md
├── bin/                 <- compiled .class files go here (javac -d bin ...)
├── src/
│   └── WarehouseInventorySystem.java   <- ALL code, one file, 9 named features + main
├── data/
│   ├── products.csv     <- product catalog (used by features 1, 2, 3, 6, 7, 8)
│   ├── routes.csv        <- distribution network (used by features 4, 5)
│   └── suppliers.csv     <- supplier directory (used by feature 8)
├── docs/
│   ├── CO_MAPPING.md      <- feature -> class -> syllabus module -> technique table
│   └── FLOWCHARTS.md      <- plain-text flowcharts for the report
├── results/
│   └── run_log.txt        <- every run is appended here automatically
└── reports/
    └── REPORT_TEMPLATE.md
```

This matches the mandatory six-part layout (`bin`, `data`, `docs`, `reports`,
`results`, `src`) plus a top-level `README`.

## Requirements

- JDK 17 or newer (tested on JDK 21)
- VS Code with the Java Extension Pack is recommended, but any terminal works

## Run it

Open a terminal **in the project root folder** (`WarehouseInventorySystem/`):

```bash
javac -d bin src/WarehouseInventorySystem.java
java  -cp bin WarehouseInventorySystem
```

You'll get a menu:

```
--------------------------------------------------------------
 WAREHOUSE INVENTORY SYSTEM
--------------------------------------------------------------
 1. Smart Product Search Bar
 2. Catalog Keyword Finder
 3. Did-You-Mean Suggester
 4. Max Shipment Capacity Calc
 5. Minimum Monitoring Points
 6. Inventory Sorter + Speed Test
 7. Low-Stock Checker
 8. Supplier Lookup
 9. Run ALL of the above as one demo
 0. Exit
 Choose an option:
```

Pick a number and press Enter. Options 1-3 and 8 will ask you to type a
search term / keyword / misspelled word / supplier name — just press Enter
with nothing typed to use a sensible built-in default.

**One-shot, non-interactive demo** (runs every feature automatically, no
typing needed — useful for a quick sanity check or for auto-grading):

```bash
java -cp bin WarehouseInventorySystem demo
```

Every run — interactive or `demo` — is also appended to `results/run_log.txt`,
so you always have a saved transcript of the output even after closing the
terminal.

## Verified

This project was compiled and run end-to-end (`javac` + `java`, both the
interactive menu and `demo` mode) before delivery, so it is confirmed working
out of the box.

## Suggested GitHub milestones

1. `review-1` — data files + `InventoryDataManager` loading
2. `review-2` — Features 1-3 (search, keyword finder, did-you-mean) + Feature 8 (supplier lookup)
3. `review-3` — Features 4-6 (shipment capacity, monitoring points, sorter/speed test)
4. `final` — Feature 7 (low-stock checker), testing, `docs/`, screenshots, and the filled-in report
