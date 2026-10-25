import java.util.*;

public final class Algorithms {

    private static int find(int v, int[] parent) {
        int root = v;
        while (root != parent[root]) {
            root = parent[root];
        }
        while (v != root) {
            int next = parent[v];
            parent[v] = root;
            v = next;
        }
        return root;
    }

    /** DSU with path compression and union by size/rank; return states. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0 || operations.length == 0) {
            result.put("states", new int[0][0]);
            return result;
        }

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int largest = 1;
        int[][] states = new int[operations.length][2];

        for (int i = 0; i < operations.length; i++) {
            int a = find(operations[i][0], parent);
            int b = find(operations[i][1], parent);
            if (a != b) {
                if (size[a] < size[b]) {
                    int t = a;
                    a = b;
                    b = t;
                }
                parent[b] = a;
                size[a] += size[b];
                components--;
                if (size[a] > largest) {
                    largest = size[a];
                }
            }
            states[i][0] = components;
            states[i][1] = largest;
        }

        result.put("states", states);
        return result;
    }

    /** Separate Kruskal implementation; return cost, edge_ids, components. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("cost", 0L);
            result.put("edge_ids", new int[0]);
            result.put("components", 0);
            return result;
        }

        int m = edges.length;
        int[][] sorted = new int[m][4];
        for (int i = 0; i < m; i++) {
            sorted[i][0] = edges[i][0];
            sorted[i][1] = edges[i][1];
            sorted[i][2] = edges[i][2];
            sorted[i][3] = i;
        }
        Arrays.sort(sorted, (a, b) -> {
            int c = Integer.compare(a[2], b[2]);
            return c != 0 ? c : Integer.compare(a[3], b[3]);
        });

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        long cost = 0;
        int components = n;
        List<Integer> edgeIds = new ArrayList<>();

        for (int i = 0; i < m; i++) {
            int u = sorted[i][0];
            int v = sorted[i][1];
            if (u == v) continue;

            int a = find(u, parent);
            int b = find(v, parent);
            if (a != b) {
                if (size[a] < size[b]) {
                    int t = a;
                    a = b;
                    b = t;
                }
                parent[b] = a;
                size[a] += size[b];
                components--;
                cost += sorted[i][2];
                edgeIds.add(sorted[i][3]);
            }
        }

        result.put("cost", cost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    /** Separate Prim implementation; restart for every unvisited component. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("cost", 0L);
            result.put("edge_ids", new int[0]);
            result.put("components", 0);
            return result;
        }

        List<List<int[]>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int i = 0; i < edges.length; i++) {
            int u = edges[i][0];
            int v = edges[i][1];
            int w = edges[i][2];
            if (u != v) {
                adj.get(u).add(new int[]{v, w, i});
                adj.get(v).add(new int[]{u, w, i});
            }
        }

        boolean[] visited = new boolean[n];
        long cost = 0;
        int components = 0;
        List<Integer> edgeIds = new ArrayList<>();

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> {
            int c = Integer.compare(a[0], b[0]);
            return c != 0 ? c : Integer.compare(a[2], b[2]);
        });

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            components++;
            visited[start] = true;

            for (int[] e : adj.get(start)) {
                if (!visited[e[0]]) {
                    pq.add(new int[]{e[1], e[0], e[2]});
                }
            }

            while (!pq.isEmpty()) {
                int[] cur = pq.poll();
                int v = cur[1];
                if (visited[v]) continue;

                visited[v] = true;
                cost += cur[0];
                edgeIds.add(cur[2]);

                for (int[] e : adj.get(v)) {
                    if (!visited[e[0]]) {
                        pq.add(new int[]{e[1], e[0], e[2]});
                    }
                }
            }
        }

        result.put("cost", cost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    /** Kosaraju's two DFS passes; return component and exact condensation. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("component", new int[0]);
            result.put("condensation", new int[0][0]);
            return result;
        }

        List<List<Integer>> g = new ArrayList<>(n);
        List<List<Integer>> gr = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            g.add(new ArrayList<>());
            gr.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
            gr.get(e[1]).add(e[0]);
        }

        boolean[] visited = new boolean[n];
        int[] ptr = new int[n];
        List<Integer> order = new ArrayList<>(n);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;
            visited[i] = true;
            stack.push(i);

            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Integer> nextList = g.get(u);
                if (ptr[u] < nextList.size()) {
                    int v = nextList.get(ptr[u]++);
                    if (!visited[v]) {
                        visited[v] = true;
                        stack.push(v);
                    }
                } else {
                    stack.pop();
                    order.add(u);
                }
            }
        }

        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int label = 0;

        for (int i = n - 1; i >= 0; i--) {
            int start = order.get(i);
            if (comp[start] != -1) continue;

            comp[start] = label;
            stack.push(start);
            while (!stack.isEmpty()) {
                int u = stack.pop();
                for (int v : gr.get(u)) {
                    if (comp[v] == -1) {
                        comp[v] = label;
                        stack.push(v);
                    }
                }
            }
            label++;
        }

        Set<String> seen = new HashSet<>();
        List<int[]> cond = new ArrayList<>();
        for (int[] e : edges) {
            int cu = comp[e[0]];
            int cv = comp[e[1]];
            if (cu != cv && seen.add(cu + "_" + cv)) {
                cond.add(new int[]{cu, cv});
            }
        }

        result.put("component", comp);
        result.put("condensation", cond);
        return result;
    }

    private static List<Integer> topoSort(int n, int[][] edges) {
        List<List<Integer>> g = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            g.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            g.get(e[0]).add(e[1]);
        }

        int[] state = new int[n]; // 0 = unvisited, 1 = active, 2 = finished
        int[] ptr = new int[n];
        List<Integer> order = new ArrayList<>(n);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (state[i] != 0) continue;
            state[i] = 1;
            stack.push(i);

            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Integer> nextList = g.get(u);
                if (ptr[u] < nextList.size()) {
                    int v = nextList.get(ptr[u]++);
                    if (state[v] == 1) {
                        return null;
                    } else if (state[v] == 0) {
                        state[v] = 1;
                        stack.push(v);
                    }
                } else {
                    stack.pop();
                    state[u] = 2;
                    order.add(u);
                }
            }
        }

        Collections.reverse(order);
        return order;
    }

    /** Three-state DFS and reverse finish order; return acyclic and order. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("acyclic", true);
            result.put("order", new int[0]);
            return result;
        }

        List<Integer> order = topoSort(n, edges);
        if (order == null) {
            result.put("acyclic", false);
            result.put("order", new int[0]);
        } else {
            result.put("acyclic", true);
            result.put("order", order);
        }
        return result;
    }

    /** Whole-graph cycle check and DAG relaxation; return acyclic, distance, parent_edge. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        Map<String, Object> result = new LinkedHashMap<>();
        List<Integer> order = topoSort(n, edges);
        if (order == null) {
            result.put("acyclic", false);
            result.put("distance", new Long[0]);
            result.put("parent_edge", new int[0]);
            return result;
        }

        List<List<int[]>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int i = 0; i < edges.length; i++) {
            adj.get(edges[i][0]).add(new int[]{edges[i][1], edges[i][2], i});
        }

        Long[] dist = new Long[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parentEdge, -1);
        dist[source] = 0L;

        for (int u : order) {
            if (dist[u] == null) continue;
            for (int[] e : adj.get(u)) {
                int v = e[0];
                long d = dist[u] + e[1];
                if (dist[v] == null || d < dist[v]) {
                    dist[v] = d;
                    parentEdge[v] = e[2];
                }
            }
        }

        result.put("acyclic", true);
        result.put("distance", dist);
        result.put("parent_edge", parentEdge);
        return result;
    }

    public static List<Integer> reconstructRoute(int source, int target, int[] parentEdge, int[][] edges) {
        List<Integer> route = new ArrayList<>();
        if (target < 0 || target >= parentEdge.length) return route;
        if (target == source) {
            route.add(source);
            return route;
        }
        if (parentEdge[target] == -1) return route;

        int cur = target;
        while (cur != source) {
            route.add(cur);
            int id = parentEdge[cur];
            if (id == -1) return new ArrayList<>();
            cur = edges[id][0];
        }
        route.add(source);
        Collections.reverse(route);
        return route;
    }
}
