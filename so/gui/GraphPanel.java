package so.gui;

import so.data.GraphicableData;
import so.util.ExampleFileFilter;
import so.util.SokkerWeek;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.text.DateFormat;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import static so.Constants.Labels.*;

public class GraphPanel extends JPanel implements ItemListener, MouseListener, ActionListener {
    public static final int SKILL_SCALE = 17;
    private static final String CMD_SNAPSHOT       = "SS";
    private static final String CMD_WEEKS_IN_GRAPH = "WK";
    private static final String CMD_LABEL_FILTER   = "LF";

    private static final DateFormat DFORMAT = DateFormat.getDateInstance(DateFormat.LONG);

    private String [] labelNames;
    private JCheckBox [] checkBoxes;
    private Color [] curveColors;
    private JPanel legendPanel;
    private GraphCanvas graphCanvas;
    private GraphicableData graphData;
    private boolean userChange;
    private int currentWeekAgo;
    private int graphScale;
    private int scaleStep;
    private String ttAxisX;
    private String ttAxisY;
    private GraphDateListener graphDateListener;
    private int weeksInGraph;

    public GraphPanel() {
        super(new GridBagLayout());
        graphData = null;
        userChange = true;
        currentWeekAgo = 0;
        graphScale = SKILL_SCALE;
        scaleStep = 1;
        ttAxisX = null;
        ttAxisY = null;
        graphDateListener = null;
        weeksInGraph = so.Constants.WEEKS_IN_GRAPH;

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

        graphCanvas.setBorder( BorderFactory.createLoweredBevelBorder() );
    }
    public GraphPanel(String [] labels) {
        this();
        labelNames = labels;
        buildLegendPanel();
    }

    public void useGraphPopup(String weeksMenuTitle, boolean screenshotOption) {
        JPopupMenu popupWeeksInGraph = new JPopupMenu();
        if (weeksMenuTitle != null) {
            JMenu weeksMenu = new JMenu(weeksMenuTitle);
            addWeekItemToMenu("30", weeksMenu);
            addWeekItemToMenu("40", weeksMenu);
            addWeekItemToMenu("50", weeksMenu);
            addWeekItemToMenu("60", weeksMenu);
            addWeekItemToMenu("70", weeksMenu);
            addWeekItemToMenu("80", weeksMenu);
            addWeekItemToMenu("90", weeksMenu);
            addWeekItemToMenu("100", weeksMenu);
            popupWeeksInGraph.add(weeksMenu);
        }
        if (screenshotOption) {
            JMenuItem jmi = new JMenuItem(MainFrame.getLabel(TXT_SAVE_SNAPSHOT));
            jmi.setActionCommand(CMD_SNAPSHOT);
            jmi.addActionListener(this);
            popupWeeksInGraph.add(jmi);
        }
        graphCanvas.setComponentPopupMenu(popupWeeksInGraph);
    }
    private void addWeekItemToMenu(String item, JMenu menu) {
        JMenuItem jmi = new JMenuItem(item);
        jmi.setActionCommand(CMD_WEEKS_IN_GRAPH + item);
        jmi.addActionListener(this);
        menu.add(jmi);
    }

    public void useLabelFilterPopup(String[] txts, int[] filters) {
        if (txts.length != filters.length) return;
        JPopupMenu popupLabelFilter = new JPopupMenu("Filter mode");
        int _N = txts.length;
        for (int i=0; i<_N; i++) {
            JMenuItem jmi = new JMenuItem(txts[i]);
            jmi.setActionCommand(CMD_LABEL_FILTER + Integer.toString(filters[i]));
            jmi.addActionListener(this);
            popupLabelFilter.add(jmi);
        }
        legendPanel.setComponentPopupMenu(popupLabelFilter);
    }

    public void setScale(int scale) { if (scale>9) graphScale = scale; }
    public void setStep(int step) { if (step>0) scaleStep = step; }
    public void setToolTips(String ttX, String ttY) {
        ttAxisX = ttX;
        ttAxisY = ttY;
        graphCanvas.setToolTipText("");
    }
    public void setWeeksInGraph(int wig) {
        if (20<wig) weeksInGraph = wig;
        if (currentWeekAgo>=weeksInGraph) currentWeekAgo = weeksInGraph-1;
        graphCanvas.drawGrid();
        graphCanvas.drawGraph();
        graphCanvas.repaint();
    }
    public int getWeeksInGraph() { return weeksInGraph; }

    public void addGraphDateListener(GraphDateListener gdl) { graphDateListener = gdl; }

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
            checkBoxes[i].setInheritsPopupMenu(true);
            legendPanel.add( checkBoxes[i] , gbc );
            i++;
        }
    }

    public void setLabels(String [] labels) {
        labelNames = labels;
        buildLegendPanel();
    }

    public void setColors(Color [] colors) {
        curveColors = colors;
        for (int i=0; i<colors.length && i<checkBoxes.length; i++) {
            checkBoxes[i].setOpaque(true);
            checkBoxes[i].setBackground(colors[i]);
        }
        graphCanvas.repaint();
    }

    public Color getColor(int idx) {
        if (idx<0 || curveColors==null || idx>=curveColors.length || curveColors[idx]==null) return Color.BLACK;
        return curveColors[idx];
    }

    public void setData(GraphicableData wd) {
        if (graphData!=null && graphData.equals(wd)) return;
        graphData = wd;
        graphCanvas.drawGraph();
        graphCanvas.repaint();
    }

    public void setCurrentWeekAgo(int week) {
        if (week<0) return;
        if (week>=weeksInGraph) week = weeksInGraph-1;
        currentWeekAgo = week;
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
        if (me.getButton()==MouseEvent.BUTTON1 && me.getClickCount()==1) {
            if (graphData==null || graphDateListener==null) return;
            double pad = graphCanvas.Y*0.07;
            double hgap = (graphCanvas.X - 2*pad)/(weeksInGraph+1.0);
            if (!isEventInRectangle(me, pad+hgap/2, pad, graphCanvas.X-pad-hgap/2, graphCanvas.Y-pad)) return;
            int x = me.getX();
            int week = (int)Math.round(weeksInGraph - (x - pad)/hgap);
            if (week>weeksInGraph || week==currentWeekAgo) return;
            java.util.Date d = graphData.getDate(week);
            if (d!=null) graphDateListener.dateChanged(d);
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    /* interface ActionListener */
    public void actionPerformed(ActionEvent e) {
        String w = e.getActionCommand();
        if (w.equals(CMD_SNAPSHOT)) {
            /* save the graph as an image File */
            JFileChooser chooser = new JFileChooser();
            ExampleFileFilter filter = new ExampleFileFilter();
            filter.addExtension("png");
            filter.setDescription("Portable Network Graphics");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showSaveDialog(this);
            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            java.io.File dest = chooser.getSelectedFile();
            String extension = filter.getExtension(dest);
            if (extension == null) {
                extension = "png";
                dest = new java.io.File( dest.getPath() + ".png" );
            }
            if (!filter.accept(dest)) {
                //INVALID FILE
                JOptionPane.showMessageDialog(this, dest.getName(),MainFrame.getLabel(TXT_ERROR),
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
            /* save */
            try{
                javax.imageio.ImageIO.write(graphCanvas.getSnapShot(), extension, dest);
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }
        else if (w.startsWith(CMD_LABEL_FILTER)) {
            int filter = Integer.parseInt(w.substring(CMD_LABEL_FILTER.length()));
            int _N = checkBoxes.length;
            for (int i=0; i<_N; i++) {
                userChange = false;
                if ((filter & 1) == 1) checkBoxes[i].setSelected(true);
                else checkBoxes[i].setSelected(false);
                filter = filter>>1;
            }
            graphCanvas.drawGraph();
            graphCanvas.repaint();
        }
        else if (w.startsWith(CMD_WEEKS_IN_GRAPH)) {
            /* Change the number of weeks shown in the Graph, +1 because we must add week 0 */
            setWeeksInGraph(Integer.parseInt(w.substring(CMD_WEEKS_IN_GRAPH.length()))+1);
        }
    }

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
            drawCurrentWeekBar(g);
            /* optional? */
//             Graphics2D g2d = (Graphics2D)g;
//             g2d.setColor(Color.RED.darker());
//             Rectangle2D rect = g2d.getFont().getStringBounds(graphData.getTitle(), g2d.getFontRenderContext());
//             int txtWidth = (int)rect.getWidth();
//             int txtHeight = (int)rect.getHeight();
//             g2d.drawString(graphData.getTitle(), (int)((X-txtWidth)/2), (int)((Y*0.07+txtHeight)/2));
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
            double pad = Y*0.07;
            double vgap = (Y-pad*2)/graphScale;
            double hgap = (X - 2*pad)/(weeksInGraph+1.0);
            g2d.setColor(Color.LIGHT_GRAY);
            /* horizontal grid lines */
            for (int i=scaleStep; i<=graphScale; i+=scaleStep) drawLine(g2d, pad, Y-pad-vgap*i, X-pad, Y-pad-vgap*i);
            /* vertical grid lines */
            for (int i=1; i<=weeksInGraph; i++) drawLine(g2d, pad+hgap*i, Y-pad, pad+hgap*i, Y-pad-vgap*graphScale);
            /* frame */
            g2d.setColor(Color.BLACK);
            drawLine(g2d, pad, vgap, pad, Y-pad);
            drawLine(g2d, pad, Y-pad, X-pad, Y-pad);
            drawLine(g2d, X-pad, Y-pad, X-pad, vgap);
            /* vertical scale numbers */
            g2d.setColor(Color.DARK_GRAY);
            for (int i=0; i<=graphScale; i+=scaleStep) {
                g2d.drawString(Integer.toString(i), (int)(pad*0.3), (int)(Y-pad*0.9-vgap*i));
                g2d.drawString(Integer.toString(i), (int)(X-pad*0.8), (int)(Y-pad*0.9-vgap*i));
            }
            /* horizontal week numbers */
            for (int i=1; i<=weeksInGraph; i++) {
                if (i > (weeksInGraph-10))
                    g2d.drawString(Integer.toString(weeksInGraph-i), (int)(pad*0.9+hgap*i), (int)(Y-pad*0.3) );
                else if ((weeksInGraph-i)%5 == 0)
                    g2d.drawString(Integer.toString(weeksInGraph-i), (int)(pad*0.7+hgap*i), (int)(Y-pad*0.3) );
            }
        }

        private void drawGraphCurve(Graphics2D g2d, Color c, int idx) {
            if ((checkBoxes[idx]==null) || (!checkBoxes[idx].isSelected())) return;
            double pad = Y*0.07;
            double vgap = (Y-pad*2)/graphScale;
            double hgap = (X - 2*pad)/(weeksInGraph+1.0);

            boolean scaledCurve = false;
            int maxValue = 0;
            if (graphData.getFieldScale(idx)>graphScale) {
                scaledCurve = true;
                int _a = 0;
                for (int i=0; i<weeksInGraph; i++) {
                    _a = graphData.getData(i, idx);
                    if (_a == so.Constants.NO_DATA) break;
                    maxValue = (maxValue>_a) ? maxValue : _a;
                }
                vgap = vgap * graphScale / maxValue;
            }

            g2d.setColor(c);
            int x0, x1, y0, y1;
            int dot = 3; //graphData.getFieldsCount() - idx + 2;
            for (int i=0; i<weeksInGraph-1; i++) {
                int a = graphData.getData(i, idx);
                int b = graphData.getData(i+1, idx);
                x0 = (int)(pad+hgap*(weeksInGraph-i));
                y0 = (int)(Y-pad-vgap*a);
                if (!scaledCurve && idx>0) y0 -= getOverlappingOffset(i, idx);
                g2d.fillOval(x0-dot,y0-dot,dot*2,dot*2);
                if (b == so.Constants.NO_DATA) break;
                x1 = (int)(pad+hgap*(weeksInGraph-i-1));
                y1 = (int)(Y-pad-vgap*b);
                if (!scaledCurve) y1 -= getOverlappingOffset(i+1, idx);
                g2d.drawLine(x0,y0, x1,y1);
            }
        }

        private int getOverlappingOffset(int week, int idx) {
            int off = 0;
            int original = graphData.getData(week, idx);
            for (int i=idx-1; i>=0; i--)
                if (original==graphData.getData(week, i) && checkBoxes[i]!=null && checkBoxes[i].isSelected()) off++;
            return off*2;
        }

        private void drawCurrentWeekBar(Graphics g) {
            g.setColor(new Color(255, 255, 192, 96));
            double pad = Y*0.07;
            double vgap = (Y-pad*2)/graphScale;
            double hgap = (X - 2*pad)/(weeksInGraph+1.0);
            int w = 7;
            int h = (int)(vgap*graphScale) + 1;
            int x = (int)(pad+hgap*(weeksInGraph-currentWeekAgo)) - w;
            int y = (int)(Y - pad - h);
            g.fillRect(x, y, w*2, h);
        }

        private void drawLine(Graphics g, double x1, double y1, double x2, double y2) {
            g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
        }

        public String getToolTipText(MouseEvent me) {
            double pad = Y*0.07;
            double hgap = (X - 2*pad)/(weeksInGraph+1.0);
            if (isEventInRectangle(me, pad,Y-pad,X-pad,Y)) return ttAxisX;
            else if (isEventInRectangle(me, X-pad,0,X,Y-pad)) return ttAxisY;
            else if (isEventInRectangle(me, 0,0,pad,Y-pad)) return ttAxisY;
            else if (isEventInRectangle(me, pad+hgap/2, pad, X-pad-hgap/2, Y-pad)) {
                int x = me.getX();
                int week = (int)Math.round(weeksInGraph - (x - pad)/hgap);
                if (week>weeksInGraph) return null;
                SokkerWeek skWeek = new SokkerWeek( so.util.SokkerCalendar.getDateOfWeeksAgo(week) );
                return DFORMAT.format(skWeek.getBeginDate()) + " \u2192 " + DFORMAT.format(skWeek.getEndDateInclusive());
            }
            return null;
        }

        public BufferedImage getSnapShot() {
            BufferedImage snapshot = new BufferedImage(X, Y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = snapshot.createGraphics();
            g2d.setColor( getBackground() );
            g2d.fillRect(0, 0, X, Y);
            g2d.drawImage(grid, 0, 0, null);
            if (graphData != null) {
                g2d.drawImage(graph, 0, 0, null);
                //drawCurrentWeekBar(g2d);
                g2d.setColor(Color.RED.darker());
                Rectangle2D rect = g2d.getFont().getStringBounds(graphData.getTitle(), g2d.getFontRenderContext());
                int txtWidth = (int)rect.getWidth();
                int txtHeight = (int)rect.getHeight();
                g2d.drawString(graphData.getTitle(), (int)((X-txtWidth)/2), (int)((Y*0.07+txtHeight)/2));
            }
            return snapshot;
        }

    }

    private static boolean isEventInRectangle(MouseEvent me, double x1, double y1, double x2, double y2) {
        return ( x1<=me.getX() && me.getX()<=x2 && y1<=me.getY() && me.getY()<=y2 );
    }

    /* =============================================== */
    public interface GraphDateListener {
        public void dateChanged(java.util.Date newDate);
    }

}
