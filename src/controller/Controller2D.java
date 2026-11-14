package controller;

import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.Panel;
import model.Line;
import fill.SeedFiller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {
    private final Panel panel;

    // Rasterizers
    private LineRasterizer lineRasterizer;
    private LineRasterizer polygonLineRasterizer;
    private PolygonRasterizer polygonRasterizer;

    //Fillers
    private SeedFiller seedFiller;

    private Polygon polygon;
    private final List<Line> lines=new ArrayList<>();

    private Point2D start; //misto kliknuti

    private boolean polygonMode=false; //true=kreslime polygon
    private boolean automaticLines = false;
    private boolean seedFillMode = false;
    private int insertIndex; //index, na ktery se v seznamu vlozi novy vrchol

    public Controller2D(Panel panel) {
        this.panel = panel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());

        polygonLineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonLineRasterizer.setColor(0x00ffff);
        polygonRasterizer = new PolygonRasterizer(polygonLineRasterizer);

        seedFiller = new SeedFiller(panel.getRaster());
        seedFiller.setBoundaryColor(polygonLineRasterizer.getColor());
        polygon = new Polygon();

        initListeners();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                start = new Point2D(e.getX(), e.getY());
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
                Point2D end = new Point2D(e.getX(), e.getY());
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
                }
                if (e.getKeyCode() == KeyEvent.VK_P){
                    polygonMode = true;
                    automaticLines = false;
                    seedFillMode = false;
                }

                if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                    polygonMode = false;
                    automaticLines = true;
                    seedFillMode = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygon.clear();
                    seedFiller.clearSeed();
                    drawScene(polygon);
                    seedFillMode = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_Q){
                    seedFillMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    seedFiller.setBoundaryMode(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_W){
                    seedFillMode = true;
                    polygonMode = false;
                    automaticLines = false;
                    seedFiller.setBoundaryMode(true);
                }


            }
        });
    }

    private void drawScene(Polygon polygonToDraw, Line... previews) {
        //smaze platno
        panel.getRaster().clear();

        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }
        //vykresli polygon
        polygonRasterizer.rasterize(polygonToDraw);

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