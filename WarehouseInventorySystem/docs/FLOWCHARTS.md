# Algorithm logic notes (for report diagrams)

Draw these as simple boxes-and-arrows flowcharts in your report. Each one maps
to a class in `src/WarehouseInventorySystem.java`.

## 1. Smart Product Search Bar — `SmartProductSearch`
```
Start -> Read query string
      -> Tokenise query into a set of words
      -> For each product: tokenise (name + category + supplier)
      -> Compute Jaccard similarity = |intersection| / |union|
      -> Sort products by score, descending
      -> Print top matches
End
```

## 2. Catalog Keyword Finder — `CatalogKeywordFinder`
```
Start -> Build LPS (failure) array for the keyword
      -> i = 0 (text pointer), j = 0 (pattern pointer)
      -> Loop while i < text length:
            if text[i] == pattern[j]: advance i, j
               if j == pattern length: record match, j = LPS[j-1]
            else if j != 0: j = LPS[j-1]
            else: advance i
      -> Print all match positions
End
```

## 3. Did-You-Mean Suggester — `DidYouMeanSuggester`
```
Start -> dp[0][j] = j, dp[i][0] = i  (base cases)
      -> For i = 1..n, j = 1..m:
            if chars equal: dp[i][j] = dp[i-1][j-1]
            else: dp[i][j] = 1 + min(dp[i-1][j-1], dp[i-1][j], dp[i][j-1])
      -> Answer = dp[n][m]
      -> Repeat for every catalog product, keep the minimum distance
End
```

## 4. Max Shipment Capacity Calc — `MaxShipmentCapacityCalc`
```
Start -> Build residual capacity graph from routes.csv
      -> Loop:
            BFS from source to sink using only edges with residual > 0
            if no path found: stop
            bottleneck = min residual capacity along the path
            subtract bottleneck along forward edges, add along reverse edges
            totalFlow += bottleneck
      -> Print totalFlow
End
```

## 5. Minimum Monitoring Points — `MinimumMonitoringPoints`
```
Start -> remaining = all route edges
      -> cover = {}
      -> While remaining is not empty:
            pick any edge (u, v) from remaining
            add u and v to cover
            remove every edge touching u or v from remaining
      -> Print cover (guaranteed <= 2x optimal size)
End
```

## 6. Inventory Sorter + Speed Test — `InventorySorterSpeedTest`
```
Start -> randomizedQuickSort(arr, lo, hi):
            if lo >= hi: return
            pick a uniformly random pivot index in [lo, hi]
            swap pivot to the end, partition around it (Lomuto scheme)
            recurse left half, recurse right half
      -> parallelBenchmark(n):
            generate n random integers
            time a sequential stream sum
            time a .parallel() stream sum
            compare the two timings
End
```

## 7. Low-Stock Checker — `LowStockChecker`
```
Start -> For each product: if quantity <= minStock, flag it
      -> Sort flagged products by (minStock - quantity) descending
      -> For each flagged product: suggestedReorderQty = 2*minStock - quantity
      -> Print the low-stock table with reorder suggestions
End
```

## 8. Supplier Lookup — `SupplierLookup` (`SimpleHashTable`)
```
Start -> Build a SimpleHashTable(bucketCount) sized to the supplier list
      -> For each supplier: put(supplierName.toLowerCase(), supplier)
      -> On lookup(name):
            idx = hash(name) mod bucketCount
            walk the chain at buckets[idx] for a matching key
            return the value if found, else null
End
```
