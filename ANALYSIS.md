# Assignment 3: Graph Algorithms Analysis

## 1. State Reset and Deep DFS

### Request State Reset
In `Main.java`, the program reads multiple JSON requests from standard input in a single process. To make sure data from one request never leaks into the next request, `Algorithms.java` has no static fields. All arrays and collections (`parent`, `size`, `adj`, `visited`, `state`, `dist`, etc.) are created as local variables inside each method call. When a method returns its `Map<String, Object>`, the local structures go out of scope.

### Iterative DFS for Deep Graphs
Since `n` can be up to `100,000`, a graph shaped like a long line (`0 -> 1 -> 2 -> ... -> 99999`) will cause a `StackOverflowError` in Java if we use recursive DFS.

To prevent this, both `scc` and `topoSort` use an explicit stack (`ArrayDeque<Integer> stack`) together with an index array `int[] ptr = new int[n]`. For any vertex `u` currently on the stack, `ptr[u]` stores the index of the next neighbor in `g.get(u)` that has not been checked yet:
- When `ptr[u] < g.get(u).size()`, we read neighbor `v = g.get(u).get(ptr[u]++)` and push `v` onto the stack if it is unvisited.
- When `ptr[u] == g.get(u).size()`, all outgoing edges of `u` have been processed, so we pop `u` from the stack and record it in the finish list.

Because `ptr[u]` only increments, we never rescan earlier neighbors when returning to `u`. This keeps the time and memory at `O(n + m)`.

## 2. Method Descriptions

### 2.1 Disjoint-Set Union (`dsu`)

- **Steps:**
  1. Read `n` and `operations`. If `n == 0` or `operations.length == 0`, return `states = []`.
  2. Initialize `parent[i] = i` and `size[i] = 1` for `0 <= i < n`, `components = n`, and `largest = 1`.
  3. For each pair `[u, v]` in `operations`:
     - Find roots `a = find(u, parent)` and `b = find(v, parent)`. The `find` method walks up to the root and then compresses the path in a second loop by pointing every visited node directly to the root.
     - If `a != b`, attach the smaller tree under the larger tree (`size[a] < size[b]`), update `size[a] += size[b]`, decrease `components` by 1, and update `largest = Math.max(largest, size[a])`.
     - Save `[components, largest]` in `states[i]`.
- **Invariant:** After `k` operations, the trees in `parent` represent the connected components formed by the first `k` edges. For every root `r`, `size[r]` is the number of vertices in that component, `components` is the number of distinct roots, and `largest` is the maximum `size[r]`.
- **Correctness:** At the start (`k = 0`), all `n` vertices are separate components of size 1. When processing `[u, v]`, if `a == b`, `u` and `v` are already connected, so the components and their sizes do not change. If `a != b`, two disjoint components merge into one component of size `size[a] + size[b]`, which reduces the number of components by 1. Path compression only changes parent pointers inside the same tree, preserving the root and component membership.
- **Boundary cases:** `n = 0` or `q = 0` returns `[]`. Self-unions `[u, u]` and duplicate edges have `a == b`, so they do not change `components` or `largest`, and the unchanged state is still recorded.
- **Complexity:** `O(n + q * alpha(n))` amortized time and `O(n + q)` memory (including the `states` output array).

### 2.2 Minimum Spanning Forest: Kruskal (`kruskal`)

- **Steps:**
  1. Read `n` and `edges`. If `n == 0`, return `cost = 0`, `edge_ids = []`, `components = 0`.
  2. Copy edges into `sorted[m][4]` where each element is `[u, v, weight, id]` and sort by `weight` ascending (using original `id` as tie-breaker).
  3. Initialize DSU arrays `parent` and `size` of length `n`, `cost = 0L`, `components = n`, and `edgeIds`.
  4. Loop through `sorted`: skip self-loops (`u == v`). If `find(u) != find(v)`, union their sets, add the weight to `cost`, decrement `components`, and add `id` to `edgeIds`.
- **Invariant:** At each step of the loop, the selected edges in `edgeIds` form an acyclic forest that is a subset of some minimum spanning forest of the graph, and `parent` tracks the connected components of the edges examined so far.
- **Correctness:** By the MST cut property, when we process edges in non-decreasing order of weight and find an edge `(u, v)` whose endpoints are in different DSU components, `(u, v)` is a minimum-weight edge crossing the cut separating `u`'s component from the rest of the graph. Therefore, adding it to the forest is optimal. Edges where `find(u) == find(v)` would create a cycle and are skipped. At the end, every connected component of the graph is spanned with minimum total weight.
- **Boundary cases:** `n = 0` returns `cost = 0, edge_ids = [], components = 0`. Isolated vertices stay as separate components in `components`. Self-loops are skipped (`u == v`). Parallel edges and negative weights work without special cases, and `long cost` prevents 32-bit overflow.
- **Complexity:** `O(n + m log(m + 1))` time (for sorting `m` edges and `m` DSU operations) and `O(n + m)` memory.

### 2.3 Minimum Spanning Forest: Prim (`prim`)

- **Steps:**
  1. Read `n` and `edges`. If `n == 0`, return `cost = 0`, `edge_ids = []`, `components = 0`.
  2. Build an undirected adjacency list `adj` where each item is `[neighbor, weight, id]`, ignoring self-loops (`u == v`).
  3. Create `boolean[] visited = new boolean[n]` and a `PriorityQueue<int[]>` ordered by `weight` (and `id` on ties).
  4. Loop `start` from `0` to `n - 1`: if `!visited[start]`, increment `components`, mark `visited[start] = true`, and push all outgoing edges of `start` into `pq`.
  5. While `pq` is not empty, poll the minimum edge `[w, v, id]`. If `visited[v]` is true, skip it. Otherwise mark `visited[v] = true`, add `w` to `cost`, add `id` to `edgeIds`, and push all edges from `v` to unvisited neighbors into `pq`.
- **Invariant:** While exploring a component starting at `start`, the visited vertices in that component and the edges chosen so far form a minimum spanning tree of the visited subgraph, and `pq` contains all edges connecting visited vertices in that component to unvisited vertices.
- **Correctness:** Each time `pq` pops an edge `[w, v, id]` with `visited[v] == false`, it is the minimum-weight edge crossing the cut between the currently visited set in this component and the unvisited vertices. By the cut property, this edge belongs to a minimum spanning tree of the component. When `pq` becomes empty, the component is complete, and the outer loop restarts from the next unvisited vertex until all `components` (including isolated vertices) are covered.
- **Boundary cases:** Disconnected graphs and isolated vertices are handled by the outer loop `0..n-1`. Self-loops are ignored when building `adj`. Parallel edges to already visited vertices are skipped by `if (visited[v]) continue;`. `long cost` handles 64-bit sums.
- **Complexity:** `O(n + m log(m + 1))` time with a lazy priority queue (at most `2m` insertions and removals) and `O(n + m)` memory.

### 2.4 Strongly Connected Components: Kosaraju (`scc`)

- **Steps:**
  1. Read `n` and `edges`. If `n == 0`, return `component = []`, `condensation = []`.
  2. Build adjacency list `g` for the original graph (`u -> v`) and `gr` for the transposed graph (`v -> u`).
  3. **Pass 1:** Run iterative DFS on `g` for all vertices `0..n-1` and record vertices in `order` as they finish.
  4. **Pass 2:** Initialize `comp` with `-1` and `label = 0`. Process `order` in reverse (`i = n - 1` down to `0`). If `start = order.get(i)` is unvisited (`comp[start] == -1`), run iterative DFS on `gr` from `start`, set `comp[v] = label` for all visited vertices, and increment `label`.
  5. **Condensation:** Loop over input `edges`. If `comp[u] != comp[v]`, use `HashSet<String> seen` (`cu + "_" + cv`) to add `[comp[u], comp[v]]` to `cond` at most once.
- **Invariant:** In Pass 2, when starting DFS on `gr` from the unvisited vertex `start` with the highest finish position from Pass 1, the set of unvisited vertices reachable from `start` in `gr` is the exact strongly connected component of `start`.
- **Correctness:** If SCC `A` has a directed edge to SCC `B` in `g`, then the maximum finish time in `A` during Pass 1 is larger than the maximum finish time in `B`. In the transposed graph `gr`, that edge is reversed (`B -> A`). Because Pass 2 processes vertices in decreasing finish order, when we start at `A`, no unvisited SCC is reachable from `A` in `gr` except `A` itself (any SCC that had edges into `A` in `g` had an even larger finish time and was already marked earlier in Pass 2). Thus each DFS in Pass 2 marks one exact SCC. Checking `cu != cv` and using a `HashSet` removes internal edges and duplicate cross-component edges.
- **Boundary cases:** `n = 0` returns empty arrays. Isolated vertices form separate SCCs of size 1. Self-loops and duplicate edges are ignored when building `cond`.
- **Complexity:** `O(n + m)` expected time (two linear DFS passes plus hash-set deduplication) and `O(n + m)` memory.

### 2.5 Topological Ordering (`topo`)

- **Steps:**
  1. Read `n` and `edges`. If `n == 0`, return `acyclic = true`, `order = []`.
  2. In `topoSort(n, edges)`, build directed adjacency list `g` and `int[] state = new int[n]` (`0` = unvisited, `1` = active on current DFS stack, `2` = finished).
  3. Run iterative DFS from each vertex `0..n-1` with `state[i] == 0`:
     - When checking neighbor `v` of `u`: if `state[v] == 1`, we found an edge to an active ancestor on the stack (a directed cycle), so return `null`.
     - If `state[v] == 0`, set `state[v] = 1` and push `v`.
     - If `state[v] == 2`, ignore it.
     - When `u` has no more neighbors (`ptr[u] == nextList.size()`), pop `u`, set `state[u] = 2`, and add `u` to `order`.
  4. If `topoSort` returns `null`, return `acyclic = false, order = []`. Otherwise reverse `order` and return `acyclic = true, order`.
- **Invariant:** During DFS, all vertices with `state == 1` form a simple directed path from the current root on the stack to `stack.peek()`, and no subgraph of `state == 2` vertices contains a cycle.
- **Correctness:** A directed graph has a cycle if and only if DFS encounters a back-edge to an active vertex (`state == 1`). When the graph is acyclic, for every edge `u -> v`, vertex `v` always finishes before `u` (either `v` was already `state == 2` before `u` inspected `u -> v`, or `v` was `state == 0` and was pushed above `u` and popped before `u`). Reversing the finish list therefore places `u` before `v` for every edge `u -> v`.
- **Boundary cases:** `n = 0` returns `acyclic = true, order = []`. A self-loop `u -> u` sees `state[u] == 1` right away and returns `acyclic = false`. Disconnected components are all checked by the outer `0..n-1` loop.
- **Complexity:** `O(n + m)` time and `O(n + m)` memory.

### 2.6 DAG Shortest Paths and Route Helper (`dag` & `reconstructRoute`)

- **Steps (`dag`):**
  1. Call `topoSort(n, edges)` on the entire graph. If it returns `null` (cycle anywhere in the graph), return `acyclic = false`, `distance = []`, `parent_edge = []`.
  2. Build adjacency list `adj` with `[v, weight, edgeId]` for each edge.
  3. Create `Long[] dist = new Long[n]` (all `null`) and `int[] parentEdge = new int[n]` (all `-1`). Set `dist[source] = 0L`.
  4. For each `u` in topological `order`: if `dist[u] == null`, skip `u` (do not relax from unreachable vertices). Otherwise, for each outgoing edge `[v, w, id]` from `u`, let `d = dist[u] + w`. If `dist[v] == null || d < dist[v]`, set `dist[v] = d` and `parentEdge[v] = id`.
- **Invariant:** After relaxing outgoing edges from the first `k` vertices in `order`, for every vertex `v` whose shortest path from `source` uses only intermediate vertices among those first `k` vertices, `dist[v]` equals the true shortest distance and `parentEdge[v]` stores the last edge ID on that shortest path.
- **Correctness:** Every path in a DAG goes forward in topological order. Because all predecessors `u` of `v` appear before `v` in `order`, `dist[u]` is already optimal before `u`'s outgoing edges are relaxed. Relaxing edges in topological order therefore relaxes the edges of every shortest path from `source` in sequence, which works even with negative edge weights.
- **Boundary cases:** Cycles unreachable from `source` are caught because `topoSort` checks all `0..n-1`. Unreachable vertices remain `dist[v] = null` and `parentEdge[v] = -1` (so a valid distance of `-1` is not confused with unreachable). `Long[]` prevents 32-bit overflow.
- **Complexity:** `O(n + m)` time and `O(n + m)` memory.

#### Route Reconstruction Helper (`reconstructRoute`)
- **Behavior:** `Algorithms.reconstructRoute(int source, int target, int[] parentEdge, int[][] edges)`
  - Returns an **empty list `[]`** if `target` is unreachable (`parentEdge[target] == -1`).
  - Returns `[source]` when `target == source`.
  - Otherwise starts at `cur = target`, adds `cur` to `route`, follows `cur = edges[parentEdge[cur]][0]` back until `cur == source`, adds `source`, and reverses `route`. Runs in `O(L)` time and space for `L` vertices on the route.
- **Helper Test Results (from `tests/TestAndBenchmark.java`):**

| Case | Graph & Source | Target | Selected `parentEdge` IDs | Result of `reconstructRoute` | Direction & Weight Verification |
|---|---|---|---|---|---|
| Reachable target | Published DAG (`n=5, source=0`, `edges=[[0,1,3],[0,2,8],[1,2,-7],[2,3,4]]`) | `3` | `parentEdge[1]=0`, `parentEdge[2]=2`, `parentEdge[3]=3` | `[0, 1, 2, 3]` | Edges `0 -> 1 -> 2 -> 3`; total weight `3 + (-7) + 4 = 0`, matches `dist[3] = 0`. |
| Source vertex | Same graph (`source=0`) | `0` | `parentEdge[0] = -1` | `[0]` | Path of 0 edges from `0` to `0`; total weight `0`, matches `dist[0] = 0`. |
| Unreachable target | Same graph (`source=0`) | `4` | `parentEdge[4] = -1` | `[]` | `dist[4] = null` and `parentEdge[4] = -1`; returns empty list `[]`. |
| Tied / parallel edges | Custom test 10 (`n=6, source=1`, `edges=[[0,1,5],[1,2,4],[1,3,2],[3,2,2],[1,2,4],[2,4,-6],[3,4,-4]]`) | `4` | `parentEdge[3]=2` (`1->3, w=2`), `parentEdge[4]=6` (`3->4, w=-4`) | `[1, 3, 4]` | Edges `2` (`1->3`) and `6` (`3->4`); total weight `2 + (-4) = -2`, matches `dist[4] = -2`. |

## 3. Kruskal vs. Prim Benchmark

### Experimental Setup
- **Hardware & Runtime:** Linux x86_64 (2 vCPUs), OpenJDK 17.0.20.1 (`-Xmx384m`).
- **Timer & Warm-up:** `System.nanoTime()` inside `tests/TestAndBenchmark.java`. Before timing each size, 1 untimed warm-up run of `kruskal` and `prim` was executed, followed by 3 timed runs per method.
- **Boundary:** Compilation and JSON parsing are excluded. Both methods are timed across the exact same call boundary (`Algorithms.kruskal(req)` and `Algorithms.prim(req)`), which includes `Input` array extraction, building the internal graph/DSU structures, running the algorithm, and building the result map.
- **Generator & Seeds:** Random graphs generated in `TestAndBenchmark.java` with `new Random(seed)` (`seeds = 42, 43, 44`) and weights in `[-100000, 100000]`. At all three sizes, both methods returned the exact same `cost` and `components`.

### Timing Results

| Size (`n`, `m`) | Seed | Verified Optimum (`cost`, `components`) | Method | Run 1 (ms) | Run 2 (ms) | Run 3 (ms) | Median (ms) |
|---|---|---|---|---:|---:|---:|---:|
| `n = 10,000`, `m = 25,000` | `42` | `cost = -530776534`, `components = 78` | Kruskal | 36.31 | 49.44 | 48.71 | 48.71 |
| `n = 10,000`, `m = 25,000` | `42` | `cost = -530776534`, `components = 78` | Prim | 48.76 | 56.56 | 66.72 | 56.56 |
| `n = 50,000`, `m = 125,000` | `43` | `cost = -2652755230`, `components = 347` | Kruskal | 421.24 | 261.27 | 184.82 | 261.27 |
| `n = 50,000`, `m = 125,000` | `43` | `cost = -2652755230`, `components = 347` | Prim | 196.45 | 194.91 | 212.49 | 196.45 |
| `n = 100,000`, `m = 250,000` | `44` | `cost = -5264569613`, `components = 665` | Kruskal | 265.27 | 230.05 | 270.11 | 265.27 |
| `n = 100,000`, `m = 250,000` | `44` | `cost = -5264569613`, `components = 665` | Prim | 594.82 | 795.28 | 643.99 | 643.99 |

### Observations and Limitations
- **Observations:** Asymptotically both Kruskal and lazy Prim run in `O(n + m log(m + 1))` time. In the measurements above, their times are close at `n = 10,000` and `n = 50,000`, while Kruskal is faster at `n = 100,000` (`265.27 ms` median vs. `643.99 ms` for Prim). This is because Kruskal sorts a single `int[m][4]` array and uses primitive `int[]` arrays for DSU, while Prim allocates `ArrayList` adjacency lists and many `int[]` elements inside `PriorityQueue`, which increases object allocation and garbage collection overhead at `m = 250,000`.
- **Limitations:** Running benchmarks inside a single JVM process means garbage collection pauses can cause spikes on individual runs (such as Run 1 for Kruskal at `n = 50,000`). Also, all three test graphs are sparse (`m = 2.5n`); on very dense graphs, Prim with an indexed heap would scale better than sorting all edges in Kruskal.

## 4. Build, Run, and Test Commands

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/my_cases.jsonl > tests/my_actual.jsonl
python tests/validate_custom.py
javac --release 17 -encoding UTF-8 -d .build/Java src/Java/*.java tests/TestAndBenchmark.java
java -Xmx384m -cp .build/Java TestAndBenchmark
```

## 5. Academic Integrity

I used the course lecture slides, *Introduction to Algorithms* (Cormen et al.), and the official Java 17 API documentation while implementing the algorithms and writing the analysis. No external solution code was copied, and all test results and benchmark measurements were executed and verified on my setup.
