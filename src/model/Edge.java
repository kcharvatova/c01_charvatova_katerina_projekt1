package model;

public class Edge {
    public int x1, y1, x2, y2;

    public Edge(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean isHorizontal() {
        return y1 == y2;
    }

    public void orientate() {
        if (y1>y2){
            int tmp = y1;
            y1 = y2;
            y2 = tmp;

            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
    }

    public boolean isIntersection(int y) {
        return y >= y1 && y<y2;
    }

    public int getIntersectionX(int y) {
        float k = (x2 - x1) / (float) (y2-y1);
        return Math.round(x1 + k* (y-y1));
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }
}
