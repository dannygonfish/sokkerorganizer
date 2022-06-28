package so.gui;

import static so.Constants.*;
import so.data.GraphicableData;
import javax.swing.JPanel;
//import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class LeagueGraphPanel extends JPanel implements ItemListener, MouseListener {

    private String [] labelNames;
    private JCheckBox [] checkBoxes;
    private Color [] curveColors;
    private JPanel legendPanel;
    private GraphCanvas graphCanvas;
    private GraphicableData graphData;
    private boolean userChange;
    private int currentRound;
    private int graphScale;
    private String ttAxisX;
    private String ttAxisY;
    private RoundChangeListener roundChangeListener;
    private int selectedTeam;

    public LeagueGraphPanel() {
        super(new GridBagLayout());
        graphData = null;
        userChange = true;
        currentRound = 0;
        graphScale = 8;
        selectedTeam = -1;
        ttAxisX = null;
        ttAxisY = null;
        roundChangeListener = null;

        GridBagConstraints gbc = new GridBagConstraints();
        legendPanel = new JPanel(new GridBagLayout());
        graphCanvas = new GraphCanvas();
        graphCanvas.addMouseListener(this);

        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(legendPanel, gbc);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new java.awt.Insets(0,5,0,0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(graphCanvas, gbc);

        //legendPanel.setBorder( BorderFactory.createRaisedBevelBorder() );
        //legendPanel.setBorder( BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED) );
        graphCanvas.setBorder( BorderFactory.createLoweredBevelBorder() );
    }
    public LeagueGraphPanel(String [] labels) {
        this();
        labelNames = labels;
        buildLegendPanel();
    }

    //public void setScale(int scale) { if (scale>7) graphScale = scale; }
    public void setToolTips(String ttX, String ttY) {
        ttAxisX = ttX;
        ttAxisY = ttY;
        graphCanvas.setToolTipText("");
    }
    public void setRoundChangeListener(RoundChangeListener rcl) { roundChangeListener = rcl; }

    protected void buildLegendPanel() {
        legendPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;
        gbc.insets = new java.awt.Insets(2,4,2,4);
        checkBoxes = new JCheckBox[labelNames.length];
        int i = 0;
        for (String s : labelNames) {
            checkBoxes[i] = new JCheckBox(s, true);
            checkBoxes[i].addItemListener(this);
            checkBoxes[i].setOpaque(true);
            checkBoxes[i].setBackground( getColor(i) );
            legendPanel.add( checkBoxes[i] , gbc );
            i++;
        }
        legendPanel.revalidate();
    }

    public void setLabels(String [] labels) {
        labelNames = labels;
        buildLegendPanel();
    }

    public void setColors(Color [] colors) {
        curveColors = colors;
        // checkboxes may not be initialized yet
//         for (int i=0; i<colors.length && i<checkBoxes.length; i++) {
//             checkBoxes[i].setOpaque(true);
//             checkBoxes[i].setBackground(colors[i]);
//         }
        graphCanvas.repaint();
    }

    public Color getColor(int idx) {
        if (idx<0 || curveColors==null || idx>=curveColors.length || curveColors[idx]==null) return Color.BLACK;
        return curveColors[idx];
    }

    public void setData(GraphicableData wd) {
        graphData = wd;
        graphCanvas.drawGraph();
        graphCanvas.repaint();
    }

    public void setCurrentRound(int round) {
        if (round<0 || round>ROUNDS_IN_SEASON) return;
        currentRound = round;
        //graphCanvas.repaint();
    }
    public void setSelectedTeam(int idx) {
        selectedTeam = idx;
        graphCanvas.drawGraph();
        graphCanvas.repaint();
    }

    /* interface ItemListener */
    public void itemStateChanged(java.awt.event.ItemEvent ie) {
        if (userChange) { // for future use
            graphCanvas.drawGraph();
            graphCanvas.repaint();
        }
        else userChange = true;
    }
    /* interface MouseListener */
    public void mouseClicked(MouseEvent me) {
        if (graphData==null || roundChangeListener==null) return;
        double pad = graphCanvas.Y*0.1;
        double hgap = (graphCanvas.X - 2*pad)/(ROUNDS_IN_SEASON+2.0);
        if (!isEventInRectangle(me, pad+hgap/2, pad, graphCanvas.X-pad-hgap/2, graphCanvas.Y-pad)) return;
        int x = me.getX();
        int week = (int)Math.round((x - pad)/hgap - 1);
        //System.out.println( "" + week);
        roundChangeListener.roundChanged(week);
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    /**
     * class GraphCanvas
     *
     */
    protected class GraphCanvas extends JPanel {
        private BufferedImage grid;
        private BufferedImage graph;

        private int X, Y;

        public GraphCanvas() {
            super();
            setBackground(Color.WHITE);
            graph = null;
            grid = null;
            X = Y = 1;
            //javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (X!=getSize().width || Y!=getSize().height ) {
                X = getSize().width;
                Y = getSize().height;
                drawGrid();
                drawGraph();
            }
            g.drawImage(grid, 0, 0, null);
            if (graphData == null) return;
            g.drawImage(graph, 0, 0, null);
            drawCurrentRoundBar(g);
        }

        protected void drawGraph() {
            if (graphData == null) return;
            graph = new BufferedImage(X, Y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = graph.createGraphics();
            for (int i=0; i<graphData.getFieldsCount(); i++) drawGraphCurve(g2d, getColor(i), i);
        }

        private void drawGrid() {
            grid = new BufferedImage(X, Y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = grid.createGraphics();
            double pad = Y*0.1;
            double vgap = (Y-pad*2.1)/graphScale;
            double hgap = (X - 2*pad)/(ROUNDS_IN_SEASON+2.0);
            g2d.setColor(Color.LIGHT_GRAY);
            /* horizontal grid lines */
            for (int i=1; i<=graphScale; i++) drawLine(g2d, pad, Y-pad-vgap*i, X-pad, Y-pad-vgap*i);
            /* vertical grid lines */
            for (int i=1; i<=ROUNDS_IN_SEASON+1; i++) drawLine(g2d, pad+hgap*i, Y-pad, pad+hgap*i, Y-pad-vgap*graphScale);
            /* frame */
            g2d.setColor(Color.BLACK);
            drawLine(g2d, pad, vgap, pad, Y-pad);
            drawLine(g2d, pad, Y-pad, X-pad, Y-pad);
            drawLine(g2d, X-pad, Y-pad, X-pad, vgap);
            /* vertical scale numbers */
            g2d.setColor(Color.DARK_GRAY);
            for (int i=1; i<=graphScale; i++) {
                g2d.drawString(Integer.toString(9-i), (int)(pad*0.3), (int)(Y-pad*0.8-vgap*i));
                g2d.drawString(Integer.toString(9-i), (int)(X-pad*0.8), (int)(Y-pad*0.8-vgap*i));
            }
            /* horizontal week numbers */
            for (int i=1; i<=ROUNDS_IN_SEASON+1; i++) {
                if (i < 11) g2d.drawString(Integer.toString(i-1), (int)(pad*0.9+hgap*i), (int)(Y-pad*0.3) );
                else g2d.drawString(Integer.toString(i-1), (int)(pad*0.8+hgap*i), (int)(Y-pad*0.3) );
            }
        }

        private void drawGraphCurve(Graphics2D g2d, Color c, int idx) {
            if ((checkBoxes[idx]==null) || (!checkBoxes[idx].isSelected())) return;
            double pad = Y*0.1;
            double vgap = (Y-pad*2.1)/graphScale;
            double hgap = (X - 2*pad)/(ROUNDS_IN_SEASON+2.0);

            g2d.setColor(c);
            if (idx==selectedTeam) g2d.setStroke( new java.awt.BasicStroke(4) );
            else g2d.setStroke( new java.awt.BasicStroke(2) );
            int x0, x1, y0, y1;
            int dot = 3; //graphData.getFieldsCount() - idx + 2;
            for (int i=0; i<ROUNDS_IN_SEASON; i++) {
                int a = graphData.getData(i, idx);
                int b = graphData.getData(i+1, idx);
                x0 = (int)(pad+hgap*(i+1));
                y0 = (int)(Y-pad-vgap*(9-a));
                g2d.fillOval(x0-dot,y0-dot,dot*2,dot*2);
                if (b == NO_DATA) break;
                x1 = (int)(pad+hgap*(i+2));
                y1 = (int)(Y-pad-vgap*(9-b));
                g2d.drawLine(x0,y0, x1,y1);
                // dots for last round
                if (i==ROUNDS_IN_SEASON-1) g2d.fillOval(x1-dot,y1-dot,dot*2,dot*2);
            }
        }

        private void drawCurrentRoundBar(Graphics g) {
            g.setColor(new Color(255, 255, 192, 96));
            double pad = Y*0.1;
            double vgap = (Y-pad*2.1)/graphScale;
            double hgap = (X - 2*pad)/(ROUNDS_IN_SEASON+2.0);
            int w = 10;
            int h = (int)(vgap*graphScale) + 1;
            int x = (int)(pad+hgap*(currentRound+1)) - w;
            int y = (int)(Y - pad - h);
            g.fillRect(x, y, w*2, h);
        }

        private void drawLine(Graphics g, double x1, double y1, double x2, double y2) {
            g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
        }

        public String getToolTipText(MouseEvent me) {
            double pad = Y*0.1;
            if (isEventInRectangle(me, pad,Y-pad,X-pad,Y)) return ttAxisX;
            else if (isEventInRectangle(me, X-pad,0,X,Y-pad)) return ttAxisY;
            else if (isEventInRectangle(me, 0,0,pad,Y-pad)) return ttAxisY;
            return null;
        }
    }

    private static boolean isEventInRectangle(MouseEvent me, double x1, double y1, double x2, double y2) {
        return ( x1<=me.getX() && me.getX()<=x2 && y1<=me.getY() && me.getY()<=y2 );
    }

    public interface RoundChangeListener {

        public void roundChanged(int newRound);

    }

}
