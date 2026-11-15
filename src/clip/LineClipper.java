package clip;

import model.Line;
import model.Point2D;

import java.util.ArrayList;
import java.util.List;

public class LineClipper {
    private static final int TOP    = 0b0001; // y < yMin
    private static final int BOTTOM = 0b0010; // y > yMax
    private static final int RIGHT  = 0b0100; // x > xMax
    private static final int LEFT   = 0b1000; // x < xMin

    private int xMin, yMin, xMax, yMax;

    public LineClipper(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = Math.min(xMin, xMax);
        this.xMax = Math.max(xMin, xMax);
        this.yMin = Math.min(yMin, yMax);
        this.yMax = Math.max(yMin, yMax);
    }

    public void move(int dx, int dy){
        xMin += dx;
        xMax += dx;
        yMin += dy;
        yMax +=dy;
    }

    //ohodnoceni vrcholu bitovym kodem
    private int code(double x, double y) {
        int code = 0;
        if (y < yMin){
            code = code | TOP;
        }
        if (y > yMax) {
            code = code | BOTTOM;
        }
        if (x > xMax) {
            code = code | RIGHT;
        }
        if (x < xMin) {
            code = code | LEFT;
        }
        return code;
    }

    // Vrati novou usecku lezici uvnitr okna, nebo null, pokud lezi cela mimo
    public Line clip(Line line) {
        double x1 = line.getStart().getX();
        double y1 = line.getStart().getY();
        double x2 = line.getEnd().getX();
        double y2 = line.getEnd().getY();
        double k;
        double h;

        if (x1 != x2){
            k =(y2 - y1) / (x2 - x1);
        }
        else{
            k = 0;
        }

        if (y1 != y2){
            h = (x2 - x1) / (y2 - y1);
        }
        else{
            h = 0;
        }

        int a = code(x1, y1);
        int b = code(x2, y2);

        while (true) {
            //porovnani bitovych kombinaci
            if (a  == 0 && b==0) {
                return new Line(
                        new Point2D((int) Math.round(x1), (int) Math.round(y1)),
                        new Point2D((int) Math.round(x2), (int) Math.round(y2)));
            }
            if ((a & b) != 0) {
                return null;
            }


            int out;       // kod bodu, ktery je venku
            double ox, oy; // souradnice toho bodu

            if (a != 0) {
                out = a;
                ox = x1;
                oy = y1;
            } else {
                out = b;
                ox = x2;
                oy = y2;
            }
            double x, y;

            if ((out & LEFT) != 0) {            // bod vlevo
                x = xMin;
                y = k * (xMin - ox) + oy;
            } else if ((out & RIGHT) != 0) {    // bod vpravo
                x = xMax;
                y = k * (xMax - ox) + oy;
            } else if ((out & TOP) != 0) {      // bod nahore
                y = yMin;
                x = h * (yMin - oy) + ox;
            } else {                            //  bod dole
                y = yMax;
                x = h * (yMax - oy) + ox;
            }

            // posune vnejsi bod na hranici okna a prepocita jeho kod
            if (out == a) {
                x1 = x; y1 = y;
                a = code(x1, y1);
            } else {
                x2 = x; y2 = y;
                b = code(x2, y2);
            }
        }
    }
    public List<Line> clip (List<Line> lines){
        List<Line> result = new ArrayList<>();
        for (Line line : lines) {
            Line clipped = clip(line);   // orezani jedne usecky
            if (clipped != null) {
                result.add(clipped);
            }
        }
        return result;
    }

    public int getXMin() { return xMin; }
    public int getYMin() { return yMin; }
    public int getXMax() { return xMax; }
    public int getYMax() { return yMax; }
}