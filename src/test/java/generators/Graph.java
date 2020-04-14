package generators;

import java.util.List;

public class Graph {
    private final int n;
    private final List<List<Integer>> edgeSet;

    public Graph(int n, List<List<Integer>> edgeSet) {
        this.n = n;
        this.edgeSet = edgeSet;
    }

    public int getN() {
        return n;
    }

    public List<List<Integer>> getEdgeSet() {
        return edgeSet;
    }
}
