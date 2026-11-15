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

    //hranice orezavaciho okna
    private int xMin, yMin, xMax, yMax;

    public LineClipper(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = Math.min(xMin, xMax);
        this.xMax = Math.max(xMin, xMax);
        this.yMin = Math.min(yMin, yMax);
        this.yMax = Math.max(yMin, yMax);
    }
    //posun orezavaciho okna, pres sipky v controlleru
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

        // k, h = smernice, k = zmena y na jednotku x, h = zmena x na jednotku y, k vypoctu pruseciku s hranici okna
        double k;
        double h;

        //svisla usecka nema smernici k
        if (x1 != x2){
            k =(y2 - y1) / (x2 - x1);
        }
        else{
            k = 0;
        }

        //vododornva usecka nema smernici h
        if (y1 != y2){
            h = (x2 - x1) / (y2 - y1);
        }
        else{
            h = 0;
        }

        //kody polohy koncovych bodu
        int a = code(x1, y1);
        int b = code(x2, y2);

        //porovnani bitovych kombinaci
        while (true) {
            //oba body maji kod 0000, usecka lezi cela v okne
            if (a  == 0 && b==0) {
                return new Line(
                        new Point2D((int) Math.round(x1), (int) Math.round(y1)),
                        new Point2D((int) Math.round(x2), (int) Math.round(y2)));
            }
            //oba body lezi za stejnou hranici okna
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

            //prusecik s hranici okna, pres kterou je bod venku, na svisle hranici (vlevo, vpravo) je x dane a dopocita se y, na vodorovne naopak
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

    //orezani vice usecek najednou
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

    //nastaveni noveho okna
    public void setWindow(int x1, int y1, int x2, int y2) {
        xMin = Math.min(x1, x2);
        xMax = Math.max(x1, x2);
        yMin = Math.min(y1, y2);
        yMax = Math.max(y1, y2);
    }


    public int getXMin() { return xMin; }
    public int getYMin() { return yMin; }
    public int getXMax() { return xMax; }
    public int getYMax() { return yMax; }
}