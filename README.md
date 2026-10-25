# My Tests (`tests/`)

This folder contains my custom test cases, expected answers, actual outputs, and the test/benchmark program for Assignment 3.

## Files

- `my_cases.jsonl` - 14 custom test requests covering empty/disconnected graphs, isolated vertices, self-unions and repeated unions, self-loops, parallel edges, negative weights, 64-bit sums, unreachable cycles in DAG, and repeated requests in one process to check state reset.
- `my_expected.jsonl` - Expected outputs for the 14 test cases in `my_cases.jsonl`.
- `my_actual.jsonl` - Actual outputs from running `python tools/run.py --input tests/my_cases.jsonl`.
- `validate_custom.py` - Script that checks `my_actual.jsonl` against `my_expected.jsonl` and also tests that alternative valid answers are accepted and invalid witnesses are rejected.
- `TestAndBenchmark.java` - Java program that calls `Algorithms.reconstructRoute` directly, tests deep chains of `100,000` vertices, and runs the Kruskal vs. Prim benchmark on 3 graph sizes.

## Commands to Run

From the repository root:

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/my_cases.jsonl > tests/my_actual.jsonl
python tests/validate_custom.py
javac --release 17 -encoding UTF-8 -d .build/Java src/Java/*.java tests/TestAndBenchmark.java
java -Xmx384m -cp .build/Java TestAndBenchmark
```

## Explanation of Test Cases in `my_cases.jsonl`

1. `dsu` (`n=5`, empty operations): Checks that `states` is `[]` when no operations are given even if `n > 0`.
2. `dsu` (`n=5`, self-unions and duplicate unions): Tests `[0,0]` (does not change count `5`), repeated `[1,2]` and `[2,1]` (keeps count `4`, largest `2`), and merging components up to `[1,5]`.
3. `kruskal` (`n=6`, disconnected with self-loops, negative weights, parallel edges): Loops `[0,0,-500]` and `[5,5,0]` are skipped. Component `{0,1,2}` picks edges `2` (`-5`) and `4` (`3`) for cost `-2`. Component `{3,4}` picks edge `5` (`-10`). Vertex `5` is isolated. Total: `cost = -12`, `components = 3`, `edge_ids = [5,2,4]`.
4. `prim` (same graph as #3): Restarts in each component and gets the same `cost = -12` and `components = 3` (`edge_ids = [2,4,5]`).
5. `kruskal` (`n=4`, 64-bit negative sum): Three edges of weight `-1000000000` give `cost = -3000000000`, testing that `long` is used instead of `int`.
6. `prim` (same graph as #5): Checks 64-bit negative sum `-3000000000` in Prim.
7. `scc` (`n=7`, two 3-cycles, self-loop, parallel cross-edges, isolated vertex): `{0,1,2}` and `{3,4,5}` are two SCCs, vertex `6` is an isolated SCC, and the two edges between `{0,1,2}` and `{3,4,5}` are deduplicated to one condensation edge `[[1,2]]`.
8. `topo` (`n=5`, diamond DAG + isolated vertex + duplicate edge): Acyclic graph `0 -> {1,2} -> 3` with isolated vertex `4`; returns `acyclic = true` and `order = [4,0,2,1,3]`.
9. `topo` (`n=6`, cycle in second component): First component `0->1->2` is a DAG, but second component `3->4->5->3` is a cycle, so `acyclic = false` and `order = []`.
10. `dag` (`n=6, source=1`, negative weights, parallel edges, tied shortest paths, unreachable vertices): Vertices `0` and `5` cannot be reached from `1` (`null`). Shortest distance to `4` is `-2` (tied via `1->2->4` and `1->3->4`).
11. `dag` (`n=5, source=0`, cycle unreachable from source): Cycle `3 <-> 4` cannot be reached from `0`, but the whole graph must be checked, returning `acyclic = false`, `distance = []`, `parent_edge = []`.
12. `dag` (`n=4, source=0`, 64-bit negative path): Path `0->1->2->3` with weights `-1000000000` gives `distance = [0, -1000000000, -2000000000, -3000000000]`.
13. `dsu` (repeat of #2): Checks that state is reset properly between requests.
14. `dag` (repeat of #10): Checks that DAG / DFS state is reset properly between requests.
