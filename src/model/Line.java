package model;

public class Line {
    private final Point2D start, end;

    public Line(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }

    public Point2D getStart() {

        return start;
    }

    public Point2D getEnd() {

        return end;
    }

    public Line automaticPosition(){
        int deltaX = end.getX() - start.getX();
        int deltaY = end.getY() - start.getY();

        double angle = Math.toDegrees(Math.atan2(Math.abs(deltaY), Math.abs(deltaX)));
        //vodorovna
        if (angle < 22.5){
            return new Line(start, new Point2D(end.getX(), start.getY()));
        }
        //svisla
        else if (angle > 67.5){
            return new Line (start, new Point2D(start.getX(), end.getY()));
        }
        //uhlopricka
        else{
            int size = (Math.abs(deltaX) + Math.abs(deltaY))/2;

            int signX = 1;
            if (deltaX < 0){
                signX=-1;
            }

            int signY = 1;
            if (deltaY < 0) {
                signY = -1;
            }

            int endX = start.getX() + signX * size;
            int endY = start.getY() + signY * size;

            return new Line (start, new Point2D(endX, endY));
        }
    }

}
