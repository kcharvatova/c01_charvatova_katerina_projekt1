package clip;

import model.Edge;
import model.Line;
import model.Point2D;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class PolygonClipper {

    private final Polygon clipPolygon;

    public PolygonClipper(){
        clipPolygon = new Polygon();
        int centerX = 400;
        int centerY = 300;
        int radius = 150;
        for (int i = 0; i<5; i++){
            double angle = 2 * Math.PI * i / 5 - Math.PI / 2;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));
            clipPolygon.addPoint(new Point2D(x, y));
        }
    }
    public Polygon clip(Polygon clipPolygon, Polygon subject) {
        //in - vrcholy orezavaneho polygonu
        List<Point2D> in = new ArrayList<>();
        for (int i = 0; i < subject.getSize(); i++) {
            in.add(subject.getPoint(i));
        }
        //out - vrcholy orezaneho polygonu
        List<Point2D> out = new ArrayList<>();

        //hrany orezavaciho polygonu
        List<Edge> clipEdges = new ArrayList<>();
        for (int i = 0; i < clipPolygon.getSize(); i++) {
            Point2D a = clipPolygon.getPoint(i);
            Point2D b = clipPolygon.getPoint((i + 1) % clipPolygon.getSize());
            clipEdges.add(new Edge(a.getX(), a.getY(), b.getX(), b.getY()));
        }

        //znamenko orientace orezavaciho polygonu (+1 nebo -1)
        int orientation = orientation(clipPolygon);

        for (Edge edge : clipEdges) {
            out.clear(); //vycistime pri kazde iteraci
            if (in.isEmpty()) {
                break;
            }
            Point2D v1 = in.get(in.size() - 1);
            for (Point2D v2 : in) {
                if (isInside(v2, edge, orientation)) {
                    if (!isInside(v1, edge, orientation)) {
                        out.add(intersection(v1, v2, edge)); //var. 4
                    }
                    out.add(v2); //var. 1, 4
                } else {
                    if (isInside(v1, edge, orientation)) {
                        out.add(intersection(v1, v2, edge)); //var. 2
                    }
                }
                v1 = v2;
            }
            //aktualizuj orezavany polygon
            in = new ArrayList<>(out);
        }

        Polygon result = new Polygon();
        for (Point2D p : in) {
            result.addPoint(p);
        }
        return result;
    }

    //bod je uvnitr, pokud lezi na stejne strane hrany jako vnitrek polygonu
    private boolean isInside(Point2D p, Edge edge, int orientation) {
        long cross = (long) (edge.getX2() - edge.getX1()) * (p.getY() - edge.getY1()) - (long) (edge.getY2() - edge.getY1()) * (p.getX() - edge.getX1());
        return cross * orientation >= 0;
    }

    //pruseik usecky v1 v2 s primkou, na ktere lezi hrana
    private Point2D intersection(Point2D v1, Point2D v2, Edge edge) {
        double x1 = v1.getX();
        double y1 = v1.getY();
        double x2 = v2.getX();
        double y2 = v2.getY();
        double x3 = edge.getX1();
        double y3 = edge.getY1();
        double x4 = edge.getX2();
        double y4 = edge.getY2();

        double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denominator;

        return new Point2D((int) Math.round(x1 + t * (x2 - x1)), (int) Math.round(y1 + t * (y2 - y1)));
    }

    //orientace polygonu ze znamenka plochy: +1 nebo -1
    private int orientation(Polygon polygon) {
        long area = 0;
        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D p = polygon.getPoint(i);
            Point2D q = polygon.getPoint((i + 1) % polygon.getSize());
            area += (long) p.getX() * q.getY() - (long) q.getX() * p.getY();
        }
        if (area >= 0){
            return 1;
        }
        else{
            return -1;
        }
    }

    public List<Line> clipOutline(Polygon clipPolygon, Polygon subject){
        List<Line> result = new ArrayList<>();
        if (subject.getSize() < 2){
            return result;
        }for (int i = 0; i < subject.getSize(); i++) {
            //hrana jako polygon o dvou bodech
            Polygon edge = new Polygon();
            edge.addPoint(subject.getPoint(i));
            edge.addPoint(subject.getPoint((i + 1) % subject.getSize()));

            Polygon clipped = clip(clipPolygon, edge);
            if (clipped.getSize() < 2) continue;

            //konce orezane usecky: prvni bod a bod nejvzdalenejsi od nej
            Point2D p = clipped.getPoint(0);
            Point2D q = p;
            double best = 0;
            for (int j = 1; j < clipped.getSize(); j++) {
                Point2D c = clipped.getPoint(j);
                double distance = Math.hypot(c.getX() - p.getX(), c.getY() - p.getY());
                if (distance > best) {
                    best = distance;
                    q = c;
                }
            }
            if (best > 0) {
                result.add(new Line(p, q));
            }
        }
        return result;

    }

    public Polygon getClipPolygon(){
        return clipPolygon;
    }


}