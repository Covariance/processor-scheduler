package generators;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="https://www.geeksforgeeks.org/random-tree-generator-using-prufer-sequence-with-examples/">Geeks for geeks</a>
 */

public class GFG {
    // Prints edges of tree
    // represented by give Prufer code
    static Graph printTreeEdges(int[] prufer, int m) {
        int vertices = m + 2;
        int[] vertex_set = new int[vertices];

        // Initialize the array of vertices
        for (int i = 0; i < vertices; i++)
            vertex_set[i] = 0;

        // Number of occurrences of vertex in code
        for (int i = 0; i < vertices - 2; i++)
            vertex_set[prufer[i] - 1] += 1;


        List<List<Integer>> edgeSet = IntStream.range(0, vertices)
                .mapToObj(i -> new ArrayList<Integer>())
                .collect(Collectors.toList());

        // Find the smallest label not present in
        // prufer[].
        for (int i = 0; i < vertices - 2; i++) {
            for (int j = 0; j < vertices; j++) {

                // If j+1 is not present in prufer set
                if (vertex_set[j] == 0) {

                    // Remove from Prufer set and print
                    // pair.
                    vertex_set[j] = -1;

                    edgeSet.get(j).add(prufer[i] - 1);

                    vertex_set[prufer[i] - 1]--;

                    break;
                }
            }
        }

        int lastA = 0, lastB = 1;
        // For the last element
        for (int i = 0, j = 0; i < vertices; i++) {
            if (vertex_set[i] == 0 && j == 0) {
                lastA = i;
                j++;
            } else if (vertex_set[i] == 0 && j == 1) {
                lastB = i;
            }
        }
        edgeSet.get(lastA).add(lastB);
        return new Graph(vertices, edgeSet);
    }

    // Function to Generate Random Tree
    static Graph generateRandomTree(Random rand, int n) {
        int length = n - 2;
        int[] arr = new int[length];

        // Loop to Generate Random Array
        for (int i = 0; i < length; i++) {
            arr[i] = rand.nextInt(length + 1) + 1;
        }
        return printTreeEdges(arr, length);
    }
}
