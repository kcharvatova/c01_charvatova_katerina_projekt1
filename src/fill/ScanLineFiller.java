package fill;

import model.Edge;
import model.Point2D;
import model.Polygon;
import raster.Raster;
import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ScanLineFiller implements Filler{
    private Polygon polygon;
    private int fillColor;
    private final Raster raster;
    private PolygonRasterizer polygonRasterizer;


    public ScanLineFiller( Raster raster, PolygonRasterizer polygonRasterizer,  int fillColor) {
        this.raster = raster;
        this.polygonRasterizer = polygonRasterizer;
        this.fillColor = fillColor;
    }

    @Override
    public void fill() {

        if (polygon == null || polygon.getSize() < 3){
            return;
        }
        List<Edge> edges = new ArrayList<>();

        for(int i = 0; i < polygon.getSize(); i++) {
            int indexB = i + 1;
            if(indexB == polygon.getSize())
                indexB = 0;

            Point2D a = polygon.getPoint(i);
            Point2D b = polygon.getPoint(indexB);

            Edge edge = new Edge(a.getX(), a.getY(), b.getX(), b.getY());
            if(!edge.isHorizontal()) {
                edge.orientate();
                edges.add(edge);
            }
        }

        int yMin = polygon.getPoint(0).getY();
        int yMax = yMin;
        for (Edge edge : edges){
            if (edge.getY1() < yMin){
                    yMin= edge.getY1();
            }
            if (edge.getY2() > yMax){
                yMax = edge.getY2();
            }
        }

        for (int y = yMin; y <= yMax; y++) {
            List<Integer> intersections = new ArrayList<>();

            for(Edge edge : edges) {
                if (edge.isIntersection(y)){
                    intersections.add(edge.getIntersectionX(y));
                }
            }


            Collections.sort(intersections);

            for (int i= 0; i+1<intersections.size(); i+=2){
                for (int x = intersections.get(i); x <= intersections.get(i+1); x++){
                    raster.setPixel(x, y, fillColor);
                }
            }
        }

        polygonRasterizer.rasterize(polygon);
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
}
