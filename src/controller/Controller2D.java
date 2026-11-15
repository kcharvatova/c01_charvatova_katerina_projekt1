package controller;

import fill.ScanLineFiller;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.Panel;
import model.Line;
import fill.SeedFiller;
import clip.PolygonClipper;
import clip.LineClipper;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Controller2D {
    private final Panel panel;

    // Rasterizers
    private LineRasterizer lineRasterizer; //usecky
    private LineRasterizer polygonLineRasterizer; //polygon
    private PolygonRasterizer polygonRasterizer;
    private LineRasterizer fillLineRasterizer; //obrys vyplneneho polygonu
    private PolygonRasterizer fillPolygonRasterizer;
    private LineRasterizer clipLineRasterizer; //obrys orezavaciho polygonu
    private PolygonRasterizer clipPolygonRasterizer;
    private LineRasterizer windowRasterizer; //okraj orezavaciho okna

    //Fillers
    private SeedFiller seedFiller;
    private ScanLineFiller scanLineFiller;
    private ScanLineFiller clipFiller;

    //Clippers
    private PolygonClipper polygonClipper;
    private LineClipper lineClipper;

    private Polygon polygon;
    private final List<Line> lines=new ArrayList<>();

    private Point2D start; //misto kliknuti
    //Modes
    private boolean polygonMode=false; //true=kreslime polygon
    private boolean automaticLines = false;
    private boolean seedFillMode = false;
    private boolean scanLineFillMode = false;
    private boolean clipMode = false;
    private boolean windowMode;

    private int insertIndex; //index, na ktery se v seznamu vlozi novy vrchol
    private final int scanLineFillColor = 0x445650;
    private final int seedFillColor = 0xf5789a;
    private final int moveStep = 10;
    private int editedClipIndex = -1;

    public Controller2D(Panel panel) {
        this.panel = panel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());

        polygonLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonLineRasterizer.setColor(0x00ffff);
        polygonRasterizer = new PolygonRasterizer(polygonLineRasterizer);

        fillLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        fillLineRasterizer.setColor(0x00ffff);
        fillPolygonRasterizer = new PolygonRasterizer(fillLineRasterizer);

        clipLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        clipLineRasterizer.setColor(0xffffff);
        clipPolygonRasterizer = new PolygonRasterizer(clipLineRasterizer);

        windowRasterizer = new LineRasterizerTrivial(panel.getRaster());
        windowRasterizer.setColor(0xffffff);
        lineClipper = new LineClipper(250, 150, 550, 450);

        seedFiller = new SeedFiller(panel.getRaster());
        seedFiller.setBoundaryColor(polygonLineRasterizer.getColor());
        seedFiller.setFillColor(seedFillColor);
        scanLineFiller = new ScanLineFiller(panel.getRaster(), fillPolygonRasterizer, scanLineFillColor);
        clipFiller = new ScanLineFiller(panel.getRaster(), clipPolygonRasterizer, 0x333366);

        polygonClipper =new PolygonClipper();

        polygon = new Polygon();

        initListeners();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                start = new Point2D(e.getX(), e.getY());

                if (clipMode && SwingUtilities.isRightMouseButton(e)) {
                    editedClipIndex = polygonClipper.getClipPolygon().getClosestPointIndex(start);
                    return;
                }

                //pridani noveho vrcholu na konec seznamu
                if (polygonMode) {
                    insertIndex = polygon.getSize();
                }

                if (seedFillMode){
                    seedFiller.setSeed(new Point2D(e.getX(), e.getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (clipMode && SwingUtilities.isRightMouseButton(e)) {
                    editedClipIndex = -1;
                    drawScene(polygon);
                    return;
                }

                Point2D end = new Point2D(e.getX(), e.getY());
                if (windowMode) {
                    return;
                }

                if (seedFillMode){
                    drawScene(polygon);
                    return;
                }

                if (polygonMode) {
                    polygon.addPoint(insertIndex, end);
                }
                else {
                    Line line = new Line (start, end);
                    if (automaticLines){
                        line = line.automaticPosition();
                    }
                    lines.add(line);
                }
                drawScene(polygon);
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point2D cursor = new Point2D(e.getX(), e.getY());

                if (windowMode) {
                    lineClipper.setWindow(start.getX(), start.getY(), cursor.getX(), cursor.getY());
                    drawScene(polygon);
                    return;
                }

                if (clipMode && SwingUtilities.isRightMouseButton(e)) {
                    if (editedClipIndex != -1) {
                        polygonClipper.moveClipPoint(editedClipIndex, cursor);
                        drawScene(polygon);
                    }
                    return;
                }

                if (seedFillMode){
                    return;
                }
                if (polygonMode) {
                    drawPolygonPreview(cursor);
                }
                else {
                    Line preview = new Line (start, cursor);
                    if (automaticLines){
                        preview = preview.automaticPosition();
                    }
                    drawScene(polygon, preview);
                }
            }
        });

        //meneni modu (L:usecky, P:polygon, SHIFT: vodorovna/svisla/uhlopricna usecka,Q: seedfill (omezeny barvou pozadi), W: seedfill (omezeni barvou hranice) C:vse smazat)
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_L){
                    polygonMode = false;
                    automaticLines = false;
                    seedFillMode = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }
                if (e.getKeyCode() == KeyEvent.VK_P){
                    polygonMode = true;
                    automaticLines = false;
                    seedFillMode = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }

                if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                    automaticLines = true;
                    polygonMode = false;
                    seedFillMode = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygon.clear();
                    seedFiller.clearSeed();
                    seedFillMode = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }
                if (e.getKeyCode() == KeyEvent.VK_Q){
                    seedFillMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    seedFiller.setBoundaryMode(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_W){
                    seedFillMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    windowMode = false;
                    seedFiller.setBoundaryMode(true);
                }
                if (e.getKeyCode() == KeyEvent.VK_E){
                    scanLineFillMode = true;
                    seedFillMode = false;
                    polygonMode = false;
                    automaticLines = false;
                    clipMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }
                if (e.getKeyCode() == KeyEvent.VK_R){
                    clipMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    seedFillMode = false;
                    scanLineFillMode = false;
                    windowMode = false;
                    drawScene(polygon);
                }

                if (e.getKeyCode() == KeyEvent.VK_T){
                    windowMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    seedFillMode = false;
                    scanLineFillMode = false;
                    clipMode = false;
                    drawScene(polygon);
                }
                if (windowMode) {
                    int dx = 0, dy = 0;
                    if (e.getKeyCode() == KeyEvent.VK_LEFT)  dx = -moveStep;
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = moveStep;
                    if (e.getKeyCode() == KeyEvent.VK_UP)    dy = -moveStep;
                    if (e.getKeyCode() == KeyEvent.VK_DOWN)  dy = moveStep;
                    if (dx != 0 || dy != 0) {
                        lineClipper.move(dx, dy);
                        drawScene(polygon);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        List<Line> clipped = lineClipper.clip(lines);
                        lines.clear();
                        lines.addAll(clipped);
                        windowMode = false;
                        drawScene(polygon);
                    }
                }



            }
        });
    }

    private void drawScene(Polygon polygonToDraw, Line... previews) {
        //smaze platno
        panel.getRaster().clear();

        if (clipMode){
            clipFiller.setPolygon(polygonClipper.getClipPolygon());
            clipFiller.fill();
        }

        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }

        if (windowMode) {
            int x1 = lineClipper.getXMin(), y1 = lineClipper.getYMin();
            int x2 = lineClipper.getXMax(), y2 = lineClipper.getYMax();
            windowRasterizer.rasterize(x1, y1, x2, y1);
            windowRasterizer.rasterize(x2, y1, x2, y2);
            windowRasterizer.rasterize(x2, y2, x1, y2);
            windowRasterizer.rasterize(x1, y2, x1, y1);
        }

        if (clipMode){
            for (Line line : polygonClipper.clipOutline(polygonClipper.getClipPolygon(), polygonToDraw)) {
                polygonLineRasterizer.rasterize(line);
            }
        }

        else if (scanLineFillMode){
            scanLineFiller.setPolygon(polygonToDraw);
            scanLineFiller.fill();
        }
        else{
            //vykresli polygon
            polygonRasterizer.rasterize(polygonToDraw);
        }


        for (Line preview : previews) {
            lineRasterizer.rasterize(preview);
        }

        seedFiller.fill();

        panel.repaint();
    }

    //nahled pri pridavani vrcholu do polygonu
    private void drawPolygonPreview(Point2D cursor){

        //kdyz ma polygon jen jeden bod, vykresli se pouze usecka od tohoto bodu ke kurzoru
        if (polygon.getSize()==0){
            Polygon temp = new Polygon();
            temp.addPoint(start);
            temp.addPoint(cursor);
            drawScene(temp);
            return;
        }
        else{
            drawScene(polygon.withNewPoint(insertIndex, cursor));
        }
    }

}