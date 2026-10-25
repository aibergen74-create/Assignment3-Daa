import java.util.*;

public class TestAndBenchmark {

    static Map<String, Object> makeInput(String task, int n, int[][] edges, Integer source) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("task", task);
        map.put("n", n);
        List<List<Integer>> list = new ArrayList<>();
        for (int[] r : edges) {
            if (r.length == 2) {
                list.add(Arrays.asList(r[0], r[1]));
            } else {
                list.add(Arrays.asList(r[0], r[1], r[2]));
            }
        }
        if (task.equals("dsu")) {
            map.put("operations", list);
        } else {
            map.put("edges", list);
        }
        if (source != null) {
            map.put("source", source);
        }
        return map;
    }

    static void testRouteHelper() {
        System.out.println("Testing reconstructRoute:");

        int[][] edges1 = {{0, 1, 3}, {0, 2, 8}, {1, 2, -7}, {2, 3, 4}};
        Map<String, Object> res1 = Algorithms.dag(makeInput("dag", 5, edges1, 0));
        Long[] dist1 = (Long[]) res1.get("distance");
        int[] parent1 = (int[]) res1.get("parent_edge");

        System.out.println("Target 3 (reachable): " + Algorithms.reconstructRoute(0, 3, parent1, edges1) + ", dist=" + dist1[3]);
        System.out.println("Target 0 (source): " + Algorithms.reconstructRoute(0, 0, parent1, edges1) + ", dist=" + dist1[0]);
        System.out.println("Target 4 (unreachable): " + Algorithms.reconstructRoute(0, 4, parent1, edges1) + ", dist=" + dist1[4]);

        int[][] edges2 = {{0, 1, 5}, {1, 2, 4}, {1, 3, 2}, {3, 2, 2}, {1, 2, 4}, {2, 4, -6}, {3, 4, -4}};
        Map<String, Object> res2 = Algorithms.dag(makeInput("dag", 6, edges2, 1));
        Long[] dist2 = (Long[]) res2.get("distance");
        int[] parent2 = (int[]) res2.get("parent_edge");
        System.out.println("Target 4 from source 1 (tied/parallel): " + Algorithms.reconstructRoute(1, 4, parent2, edges2)
                + ", parentEdge[4]=" + parent2[4] + ", dist=" + dist2[4]);
    }

    static void testDeepChain() {
        System.out.println("\nTesting 100k chain:");
        int n = 100000;
        int[][] dir = new int[n - 1][2];
        int[][] wDir = new int[n - 1][3];
        for (int i = 0; i < n - 1; i++) {
            dir[i][0] = i;
            dir[i][1] = i + 1;
            wDir[i][0] = i;
            wDir[i][1] = i + 1;
            wDir[i][2] = 10;
        }

        Map<String, Object> sccRes = Algorithms.scc(makeInput("scc", n, dir, null));
        System.out.println("SCC 100k done, length=" + ((int[]) sccRes.get("component")).length);

        Map<String, Object> topoRes = Algorithms.topo(makeInput("topo", n, dir, null));
        System.out.println("Topo 100k done, acyclic=" + topoRes.get("acyclic"));

        Map<String, Object> dagRes = Algorithms.dag(makeInput("dag", n, wDir, 0));
        Long[] dist = (Long[]) dagRes.get("distance");
        int[] parent = (int[]) dagRes.get("parent_edge");
        List<Integer> route = Algorithms.reconstructRoute(0, n - 1, parent, wDir);
        System.out.println("DAG 100k done, dist[99999]=" + dist[n - 1] + ", route size=" + route.size());
    }

    static int[][] randomGraph(int n, int m, long seed) {
        Random rnd = new Random(seed);
        int[][] edges = new int[m][3];
        for (int i = 0; i < m; i++) {
            edges[i][0] = rnd.nextInt(n);
            edges[i][1] = rnd.nextInt(n);
            edges[i][2] = rnd.nextInt(200000) - 100000;
        }
        return edges;
    }

    static void benchmark() {
        System.out.println("\nBenchmark Kruskal vs Prim:");
        int[][] configs = {
            {10000, 25000, 42},
            {50000, 125000, 43},
            {100000, 250000, 44}
        };

        for (int[] c : configs) {
            int n = c[0];
            int m = c[1];
            long seed = c[2];

            int[][] edges = randomGraph(n, m, seed);
            Map<String, Object> req = makeInput("kruskal", n, edges, null);

            Algorithms.kruskal(req);
            Algorithms.prim(req);

            double[] kt = new double[3];
            double[] pt = new double[3];
            long kCost = 0, pCost = 0;
            int kComp = 0, pComp = 0;

            for (int r = 0; r < 3; r++) {
                long t0 = System.nanoTime();
                Map<String, Object> kr = Algorithms.kruskal(req);
                long t1 = System.nanoTime();
                kt[r] = (t1 - t0) / 1e6;
                kCost = (Long) kr.get("cost");
                kComp = (Integer) kr.get("components");

                long t2 = System.nanoTime();
                Map<String, Object> pr = Algorithms.prim(req);
                long t3 = System.nanoTime();
                pt[r] = (t3 - t2) / 1e6;
                pCost = (Long) pr.get("cost");
                pComp = (Integer) pr.get("components");
            }

            double[] ks = kt.clone();
            double[] ps = pt.clone();
            Arrays.sort(ks);
            Arrays.sort(ps);

            System.out.printf("n=%d, m=%d, seed=%d | cost=%d, components=%d%n", n, m, seed, kCost, kComp);
            System.out.printf("  Kruskal: %.2f, %.2f, %.2f ms (median %.2f ms)%n", kt[0], kt[1], kt[2], ks[1]);
            System.out.printf("  Prim:    %.2f, %.2f, %.2f ms (median %.2f ms)%n", pt[0], pt[1], pt[2], ps[1]);
        }
    }

    public static void main(String[] args) {
        testRouteHelper();
        testDeepChain();
        benchmark();
    }
}
