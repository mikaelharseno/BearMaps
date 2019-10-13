import java.util.HashMap;
import java.util.Map;
import java.lang.Math.*;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    private Quadheap data;

    public Rasterer(String imgRoot) {
        // YOUR CODE HERE
        data = new Quadheap(7, ROOT_ULLAT, ROOT_ULLON, ROOT_LRLAT, ROOT_LRLON, imgRoot);

    }


    private class Quadheap {
        Quadleaf[] data;
        int length;
        int height;
        String imgRoot;

        Quadheap(int height, double rootUllat, double rootUllon,
                        double rootLrlat, double rootLrlon, String img) {
            this.height = height;
            this.imgRoot = img;
            Double a = Math.pow(4, (height + 1));
            length = (a.intValue() - 1) / 3;
//            System.out.println(length);
            data = new Quadleaf[length];
            double[] inputs = new double[4];
            inputs[0] = rootUllat;
            inputs[1] = rootUllon;
            inputs[2] = rootLrlat;
            inputs[3] = rootLrlon;
            data[0] = new Quadleaf(0, inputs,
                    0, 0, 0, 0, height, imgRoot);
//            System.out.println(data[0].ullat);
            breed(data[0]);
        }

        private void breed(Quadleaf a) {
            if (a.height < a.maxheight) {
                double[] inputs = new double[4];
                int newI = 2 * a.i, newJ = 2 * a.j;
                int next1 = a.index * 4 + 1;
                inputs[0] = a.ullat;
                inputs[1] = a.ullon;
                inputs[2] = (a.ullat + a.lrlat) / 2;
                inputs[3] = (a.ullon + a.lrlon) / 2;
                data[next1] = new Quadleaf(a.height + 1, inputs, next1,
                        newI, newJ, a.coordinate * 10 + 1, a.maxheight, imgRoot);
                int next2 = a.index * 4 + 2;
                inputs[0] = a.ullat;
                inputs[1] = (a.ullon + a.lrlon) / 2;
                inputs[2] = (a.ullat + a.lrlat) / 2;
                inputs[3] = a.lrlon;
                data[next2] = new Quadleaf(a.height + 1, inputs, next2,
                        newI, newJ + 1, a.coordinate * 10 + 2, a.maxheight, imgRoot);
                int next3 = a.index * 4 + 3;
                inputs[0] = (a.ullat + a.lrlat) / 2;
                inputs[1] = a.ullon;
                inputs[2] = a.lrlat;
                inputs[3] = (a.ullon + a.lrlon) / 2;
                data[next3] = new Quadleaf(a.height + 1, inputs, next3,
                        newI + 1, newJ, a.coordinate * 10 + 3, a.maxheight, imgRoot);
                int next4 = a.index * 4 + 4;
                inputs[0] = (a.ullat + a.lrlat) / 2;
                inputs[1] = (a.ullon + a.lrlon) / 2;
                inputs[2] = a.lrlat;
                inputs[3] = a.lrlon;
                data[next4] = new Quadleaf(a.height + 1, inputs, next4,
                        newI + 1, newJ + 1, a.coordinate * 10 + 4, a.maxheight, imgRoot);
//                System.out.println(a.index);
                breed(data[next1]);
                breed(data[next2]);
                breed(data[next3]);
                breed(data[next4]);
            }
        }

        public String[][] quadleafArrayToStringArray(Quadleaf[][] input) {
            String[][] out = new String[input.length][input[0].length];
            for (int i = 0; i < input.length; i = i + 1) {
                for (int j = 0; j < input[0].length; j = j + 1) {
                    out[i][j] = input[i][j].printLeaf();
                }
            }
            return out;
        }

        public Quadleaf[][] returnQuadleafArray(Quadleaf start, Quadleaf end) {
            int iStart = start.i;
            int jStart = start.j;
            int iEnd = end.i;
            int jEnd = end.j;
            Quadleaf[][] results = new Quadleaf[iEnd - iStart + 1][jEnd - jStart + 1];
            int coor = start.coordinate;
            int coora = start.coordinate;
            for (int i = 0; i <= (iEnd - iStart); i = i + 1) {
                if (i > 0) {
                    coor = moveDown(coor);
                    coora = coor;
                }
                for (int j = 0; j <= (jEnd - jStart); j = j + 1) {
                    if (j > 0) {
                        coora = moveRight(coora);
                    }
                    results[i][j] = data[coordinateToIndex(coora)];
                }
            }
            return results;
        }

        boolean inRange(Quadleaf base, double lat, double lon, boolean upperLeft) {
            if (upperLeft) {
                return (lat <= base.ullat && lat > base.lrlat
                        && lon < base.lrlon && lon >= base.ullon);
            } else {
                return (lat < base.ullat && lat >= base.lrlat
                        && lon <= base.lrlon && lon > base.ullon);
            }
        }

        public Quadleaf containingLeaf(double lat, double lon,
                                       double reqLonDPP, boolean upperLeft) {
            Quadleaf base = data[0];
            while (base.LonDPP > reqLonDPP && !(isLeaf(base))) {
                if (inRange(data[base.index * 4 + 1], lat, lon, upperLeft)) {
                    base = data[base.index * 4 + 1];
                } else if (inRange(data[base.index * 4 + 2], lat, lon, upperLeft)) {
                    base = data[base.index * 4 + 2];
                } else if (inRange(data[base.index * 4 + 3], lat, lon, upperLeft)) {
                    base = data[base.index * 4 + 3];
                } else if (inRange(data[base.index * 4 + 4], lat, lon, upperLeft)) {
                    base = data[base.index * 4 + 4];
                } else {
                    System.out.println("Invalid conditions.");
                }
            }
            return base;
        }

        public boolean isLeaf(Quadleaf a) {
            return (a.index * 4 + 1) >= length;
        }

        int coordinateToIndex(int coordinate) {
            int result = 0, reversedNum = 0;
            while (coordinate != 0) {
                reversedNum = reversedNum * 10 + coordinate % 10;
                coordinate = coordinate / 10;
            }
            coordinate = reversedNum;
            while (coordinate > 0) {
                result = 4 * result + (coordinate % 10);
                coordinate = coordinate / 10;
            }
            return result;
        }

        private int moveRight(int coordinate) {
            if (coordinate == 0) {
                System.out.println("Moveright invalid.");
                return 1;
            }
            if ((coordinate % 10) == 1 || (coordinate % 10) == 3) {
                return coordinate + 1;
            } else {
                return moveRight(coordinate / 10) * 10 + (coordinate % 10) - 1;
            }
        }

        private int moveDown(int coordinate) {
            if (coordinate == 0) {
                System.out.println("Movedown invalid.");
                return 1;
            }
            if ((coordinate % 10) == 1 || (coordinate % 10) == 2) {
                return coordinate + 2;
            } else {
                return moveDown(coordinate / 10) * 10 + (coordinate % 10) - 2;
            }
        }



    }

    private class Quadleaf {
        int index, i, j, coordinate, height;
        double LonDPP, ullat, ullon, lrlat, lrlon;
        int maxheight;
        String imgRoot;

        Quadleaf(int height, double[] inputs, int index, int i,
                 int j, int coordinate, int maxheight, String img) {
            this.height = height;
            this.ullat = inputs[0];
            this.ullon = inputs[1];
            this.lrlat = inputs[2];
            this.lrlon = inputs[3];
            LonDPP = (lrlon - ullon) / 256.0;
            this.index = index;
            this.i = i;
            this.j = j;
            this.coordinate = coordinate;
            this.maxheight = maxheight;
            this.imgRoot = img;
        }

        String printLeaf() {
            if (coordinate == 0) {
                return "Invalid";
            }
            return imgRoot + Integer.toString(coordinate) + ".png";
        }

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */

    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();

        //if (params.get("lrlon") > ROOT_LRLON || params.get("ullon") < ROOT_ULLON
        //        || params.get("lrlat") < ROOT_LRLAT || params.get("ullat") > ROOT_ULLAT) {
        //    System.out.println("Index out of bounds");
        //    return results;
        //}
        double ulLat = params.get("ullat");
        double ulLon = params.get("ullon");
        double lrLat = params.get("lrlat");
        double lrLon = params.get("lrlon");
        double reqLonDPP = (lrLon - ulLon) / params.get("w");
        Quadleaf a = data.containingLeaf(ulLat, ulLon, reqLonDPP, true);
        Quadleaf b = data.containingLeaf(lrLat, lrLon, reqLonDPP, false);
//        System.out.println(a.printLeaf());
//        System.out.println(b.printLeaf());
//        System.out.println(reqLonDPP);
//        System.out.println(a.LonDPP);
//        System.out.println("reference");
//        System.out.println(a.LonDPPforheight(0));
//        System.out.println(a.LonDPPforheight(1));
//        System.out.println(a.LonDPPforheight(2));
//        System.out.println(a.LonDPPforheight(3));
//        System.out.println(a.LonDPPforheight(4));
//        System.out.println(a.LonDPPforheight(5));
//        System.out.println(a.LonDPPforheight(6));
//        System.out.println(a.LonDPPforheight(7));
//        System.out.println(a.LonDPPforheight(8));
//        System.out.println(a.ullon);
//        System.out.println(a.ullat);
//        System.out.println(a.lrlon);
//        System.out.println(a.lrlat);
//        System.out.println(a.height);
        results.put("raster_ul_lon", a.ullon);
        results.put("raster_ul_lat", a.ullat);
        results.put("raster_lr_lon", b.lrlon);
        results.put("raster_lr_lat", b.lrlat);
        results.put("depth", a.height);
        String[][] abc = data.quadleafArrayToStringArray(data.returnQuadleafArray(a, b));
//        for (int i = 0; i < abc.length; i = i + 1) {
//            for (int j = 0; j < abc[0].length; j = j + 1) {
//                System.out.print(abc[i][j]);
//            }
//            System.out.println("");
//        }
        results.put("render_grid", abc);
        boolean success = true;
        results.put("query_success", success);
        return results;
    }
}
