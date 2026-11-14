package fill;

import model.Point2D;
import raster.Raster;

public class SeedFiller implements Filler{
    private final Raster raster;
    private int fillColor;
    private int backgroundColor;
    private int boundaryColor;
    private Point2D seed;
    private boolean boundaryMode = false;

    public SeedFiller(Raster raster) {
        this.raster = raster;
        this.fillColor = 0x00ff00;
    }

    @Override
    public void fill() {
        if(seed == null) return;

        seedFill(seed.getX(), seed.getY());
    }

    private void seedFill(int x, int y) {
        if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight()) {
            return;
        }
        int pixelColor = raster.getPixel(x, y) & 0xffffff;

        if (boundaryMode){
            if (pixelColor == boundaryColor || pixelColor == fillColor){
                return;
            }
        }
        else{
            if (pixelColor != backgroundColor){
                return;
            }
        }

        raster.setPixel(x, y, fillColor) ;

        seedFill(x+1, y);
        seedFill(x-1, y);
        seedFill(x, y+1);
        seedFill(x, y-1);
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setSeed(Point2D seed) {
        this.seed = seed;

        int seedColor =  raster.getPixel(seed.getX(), seed.getY()) & 0xffffff;
        setBackgroundColor(seedColor);
    }

    public void setBoundaryColor(int boundaryColor){
        this.boundaryColor = boundaryColor & 0xffffff;
    }

    public void setBoundaryMode(boolean boundaryMode){
        this.boundaryMode = boundaryMode;
    }

    public void clearSeed(){
        seed=null;
    }

}
