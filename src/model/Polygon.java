package model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    //seznam bodu v poradi v jakem byly pridany
    private final List<Point2D> points;

    public Polygon() {

        points = new ArrayList<>();
    }

    //prida bod na konec seznamu
    public void addPoint(Point2D point){
        points.add(point);
    }

    //prida bod na urceny index
    public void addPoint(int index, Point2D point) {
        points.add(index, point);
    }


    public Point2D getPoint(int i) {
        return points.get(i);
    }

    public int getSize() {
        return points.size();
    }

    //metoda pouzivana pri pridavani noveho bodu do polygonu, vrati kopii polygonu puvodniho+novy vkladany, ale neprepise polygon puvodni, pouziva se, abychom videli, jak bude polygon po pridani bodu vypadat
    public Polygon withNewPoint(int index, Point2D point) {
        Polygon copy = new Polygon();
        for (Point2D p : points) {
            copy.addPoint(p);
        }
        copy.addPoint(index, point);
        return copy;
    }

    //smaze body ze seznamu
    public void clear(){
        points.clear();
    }
}
