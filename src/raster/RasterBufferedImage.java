package raster;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {
    private final BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        //osetreni sahani mimo platno, aby program nevyhazoval chyby, bod 0, 0 lezi v levem rohu, x roste doprava a y roste dolu
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return;
        }
        image.setRGB(x, y, color);
    }

    @Override
    public int getPixel(int x, int y) {
        //osetreni mmimo obrazovku, neco se musi vratit
        return(image.getRGB(x, y));
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void clear() {
        Graphics g = image.getGraphics();
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
    }

    public BufferedImage getImage() {
        return image;
    }
}
