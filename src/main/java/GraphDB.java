import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    HashMap<Long, Node> nodeList;
    int size;

    public class Node {
        double lat, lon;
        long id;
        HashMap<Long, Node> adjList;
        HashMap<Long, Double> distancesToAdj;
        ArrayList<String> tags;
        boolean marked;

        public Node(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            adjList = new HashMap<>();
            tags = new ArrayList<>();
            distancesToAdj = new HashMap<>();
        }

        public void addEdge(Node a) {
            double dist = distance(id, a.id);
            adjList.put(a.id, a);
            a.adjList.put(id, this);
            distancesToAdj.put(a.id, dist);
            a.distancesToAdj.put(id, dist);
        }

        public void removeEdge(Node a) {
            a.adjList.remove(this.id);
            a.distancesToAdj.remove(this.id);
            adjList.remove(a.id);
            distancesToAdj.remove(a.id);
        }
    }

    public Node addNode(long id, double lat, double lon) {
        Node result = new Node(id, lat, lon);
        nodeList.put(id, result);
        return result;
    }



    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            nodeList = new HashMap<>();
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        size = nodeList.size();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> toBeRemoved = new ArrayList<>();
        for (Long w : nodeList.keySet()) {
            if (nodeList.get(w).adjList.size() == 0) {
                toBeRemoved.add(w);
            }
        }
        for (Long s : toBeRemoved) {
            nodeList.remove(s);
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodeList.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        return nodeList.get(v).adjList.keySet();
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        Node node1 = nodeList.get(v);
        Node node2 = nodeList.get(w);
        if (node1.adjList.containsKey(w)) {
            return node1.distancesToAdj.get(w);
        }
        double a = (node1.lat - node2.lat);
        double b = (node1.lon - node2.lon);
        return Math.sqrt(a * a + b * b);
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        long closest = nodeList.keySet().iterator().next();
        double a = (lat - nodeList.get(closest).lat);
        double b = (lon - nodeList.get(closest).lon);
        double closestDistance = Math.sqrt(a * a + b * b), now;
        boolean first = true;
        for (Long w : nodeList.keySet()) {
            a = (lat - nodeList.get(w).lat);
            b = (lon - nodeList.get(w).lon);
            now = Math.sqrt(a * a + b * b);
            if (now < closestDistance) {
                closest = w;
                closestDistance = now;
            }
        }
        return closest;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return nodeList.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return nodeList.get(v).lat;
    }
}
