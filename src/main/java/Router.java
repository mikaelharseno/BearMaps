import java.util.LinkedList;
import java.util.HashMap;
/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */

    public static LinkedList<Long> shortestPath(GraphDB g, double stlon,
                                                double stlat, double destlon, double destlat) {
        Long start = g.closest(stlon, stlat);
        Long end = g.closest(destlon, destlat);
        double newdistance;
        int index, previndex;
        MyPQ<Long> pq = new MyPQ<>();
        //ArrayList<Long> data = new ArrayList<>();
        HashMap<Integer, Long> data = new HashMap<>();
        HashMap<Long, Integer> data2 = new HashMap<>();
        double[] distTo = new double[g.size];
        int[] edgeTo = new int[g.size];
        boolean[] marked = new boolean[g.size];
        double[] distancesFromEnd = new double[g.size];
        boolean cont = true;
        int i = 0;
        for (Long w : g.nodeList.keySet()) {
            distTo[i] = 1000;
            data.put(i, w);
            data2.put(w, i);
            marked[i] = false;
            pq.insert(w, 1000);
            i = i + 1;
        }
        int startindex = data2.get(start);
        distTo[startindex] = 0;
        distancesFromEnd[startindex] = g.distance(start, end);
        double priority = distTo[startindex] + distancesFromEnd[startindex];
        pq.changePriority(start, priority);
        Long cur;
        while (!pq.isEmpty() && cont) {
            cur = pq.removeMin();
            if (cur.equals(end)) {
                cont = false;
            }
            if (cont) {
                previndex = data2.get(cur);
                for (Long a : g.adjacent(cur)) {
                    index = data2.get(a);
                    if (!marked[index]) {
                        newdistance = g.distance(cur, a) + distTo[previndex];
                        if (newdistance < distTo[index]) {
                            if (distTo[index] == 1000) {
                                distancesFromEnd[index] = g.distance(end, a);
                            }
                            distTo[index] = newdistance;
                            edgeTo[index] = previndex;
                            priority = distTo[index] + distancesFromEnd[index];
                            pq.changePriority(a, priority);
                        }
                    }
                }
                marked[previndex] = true;
            }
        }
        LinkedList<Long> result = new LinkedList<>();
        int curindex = data2.get(end);
        while (curindex != data2.get(start)) {
            result.addFirst(data.get(curindex));
            curindex = edgeTo[curindex];
        }
        result.addFirst(start);
        return result;
    }
}
