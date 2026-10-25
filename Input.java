import java.util.List;
import java.util.Map;

public class Input {

    public static int vertexCount(Map<String, Object> input) {
        Object val = input.get("n");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    public static int source(Map<String, Object> input) {
        Object val = input.get("source");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    public static int[][] operations(Map<String, Object> input) {
        return parse2DArray(input.get("operations"));
    }

    public static int[][] edges(Map<String, Object> input) {
        return parse2DArray(input.get("edges"));
    }

    private static int[][] parse2DArray(Object obj) {
        if (obj == null) {
            return new int[0][0];
        }
        if (obj instanceof int[][]) {
            return (int[][]) obj;
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            int[][] result = new int[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                Object rowObj = list.get(i);
                if (rowObj instanceof int[]) {
                    result[i] = (int[]) rowObj;
                } else if (rowObj instanceof List) {
                    List<?> rowList = (List<?>) rowObj;
                    result[i] = new int[rowList.size()];
                    for (int j = 0; j < rowList.size(); j++) {
                        result[i][j] = ((Number) rowList.get(j)).intValue();
                    }
                } else {
                    result[i] = new int[0];
                }
            }
            return result;
        }
        return new int[0][0];
    }
}