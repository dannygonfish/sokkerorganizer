package so.gui;

import so.text.LabelManager;
import so.config.Options;
import so.data.PlayerRoster;
import so.data.PlayerProfile;
import so.data.TableColumnData;
import so.data.LineupManager;
import so.data.DataPair;
import so.data.MatchRepository;
import so.data.TeamDetails;
import so.gui.render.DataPairCellRenderer;
import so.gui.render.PositionCellRenderer;
import so.gui.render.SquadIconCellRenderer;
import so.util.TableSorter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Font;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Labels.*;
import static so.Constants.Positions.*;

public class LineupPanel extends JPanel  {
    private static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
    private static final int SIZE = 42;
    private static final Font FONT_NUMBER = new Font("serif", Font.BOLD, 14);
    private static final Font FONT_PLAYERNAME = new Font("dialog", Font.PLAIN, 10);

    private LabelManager labelManager;
    Options options;
    private PlayerRoster roster;
    private LineupManager lineupManager;
    private MatchRepository matchRepo;
    private TeamDetails team;

    private PlayerProfileTransferHandler playerHandler;
    private Board b1, b2, b3, b4;
    private Board [] boards;
    private JTable lineupTable;
    private JTable playersTable;
    private LineupTableModel lineupModel;
    private PlayersTableModel playersModel;
    private TableSorter playersSorter;
    private LineupManager.Lineup currentLineup;

    public LineupPanel(LabelManager lm, Options opt, TeamDetails td, PlayerRoster ros,
                       LineupManager lupm, MatchRepository repo) {
        super(new java.awt.GridBagLayout());
        labelManager = lm;
        options = opt;
        roster = ros;
        lineupManager = lupm;
        matchRepo = repo;
        team = td;

        playerHandler = new PlayerProfileTransferHandler();
        b1 = new Board(lineupManager.getCurrentLineup(0));
        b2 = new Board(lineupManager.getCurrentLineup(1));
        b3 = new Board(lineupManager.getCurrentLineup(2));
        b4 = new Board(lineupManager.getCurrentLineup(3));
        boards = new Board[] { b1, b2, b3, b4 };

        currentLineup = b1.lineUp;

        lineupModel = new LineupTableModel();
        lineupTable = new JTable(lineupModel);
        initLineupTableColumns();
        lineupTable.getTableHeader().setReorderingAllowed(false);
        lineupTable.getTableHeader().setResizingAllowed(false);


        playersModel = new PlayersTableModel();
        playersSorter = new TableSorter(playersModel);
        playersTable = new JTable();
        playersTable.setAutoCreateColumnsFromModel(false);
        playersTable.setModel(playersSorter);
        playersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        playersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        initPlayersTableColumns();
        playersTable.setTransferHandler(playerHandler);
        playersTable.setDragEnabled(true);
        playersTable.setTableHeader( new ToolTipTableHeader(playersTable.getColumnModel()) );


//         DragSource dragSource = DragSource.getDefaultDragSource();
//         DragGestureListener dgListener = new DragGestureListener() {
//                 public void dragGestureRecognized(DragGestureEvent dge) { }
//             };
//         dragSource.createDefaultDragGestureRecognizer(playersTable, DnDConstants.ACTION_COPY_OR_MOVE, dgListener );

        JTableHeader tableHeader = playersTable.getTableHeader();
        playersSorter.setTableHeader(tableHeader);
        tableHeader.setToolTipText(labelManager.getLabel(TXT_TT_TABLE_HEADER));


        createGUI();
    }

    private void initLineupTableColumns() {
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TableColumnModel tcm = lineupTable.getColumnModel();
        tcm.getColumn(0).setMaxWidth(30);
        tcm.getColumn(0).setCellRenderer(dtcr);
        tcm.getColumn(1).setPreferredWidth(250);
        tcm.getColumn(1).setCellRenderer(dtcr);
        tcm.getColumn(2).setPreferredWidth(50);
        tcm.getColumn(2).setCellRenderer(new PositionCellRenderer(labelManager));
        tcm.getColumn(3).setPreferredWidth(80);
        tcm.getColumn(3).setCellRenderer(dtcr);
    }

    private void initPlayersTableColumns() {
        JTable t = playersTable;
        // Remove any current columns
        TableColumnModel cm = t.getColumnModel();
        while (cm.getColumnCount() > 0) cm.removeColumn(cm.getColumn(0));
        // Create new columns from the options info
        ArrayList<TableColumnData> ptc = options.getLineupTableColumns();
        TableColumn tc = null;
//         JComboBox posEditorCombo = new JComboBox();
//         posEditorCombo.addItem(new DataPair(DATA_POSITION, NO_POSITION));
//         for (int _pos : SELECTABLE_POSITIONS) posEditorCombo.addItem(new DataPair(DATA_POSITION, _pos));
//         positionRenderer.setTable(t);
//         posEditorCombo.setRenderer(positionRenderer);
//         JComboBox squadEditorCombo = new JComboBox(new Character[]{ ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' });
//         squadEditorCombo.setMaximumRowCount( squadEditorCombo.getItemCount() );
//         squadEditorCombo.setRenderer(squadCellRenderer);
        SkillCellRenderer skcr = new SkillCellRenderer(labelManager);
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PositionCellRenderer ratingRenderer = new PositionCellRenderer(labelManager);
        DataPairCellRenderer dpcr = new DataPairCellRenderer(options);
        SquadIconCellRenderer squadCellRenderer = new SquadIconCellRenderer();
        for (TableColumnData data : ptc) {
            tc = new TableColumn( data.getModelIndex(), data.getWidth() );
            tc.setIdentifier(data);
            if (data.isActive()) t.addColumn( tc );
            switch (data.getModelIndex()) {
            case 0: /* name */
                break;
            case 7: /* age */
                tc.setCellRenderer(dtcr);
                break;
            case 1: /* boards appearance */
            case 2:
            case 3:
            case 4:
            case 6: /* squad */
//                 tc.setCellEditor(new DefaultCellEditor( squadEditorCombo ));
            case 8: /* PiLM */
                tc.setCellRenderer(squadCellRenderer);
                break;
            case 9: /* form */
            case 10: /* skills */
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                tc.setCellRenderer(skcr);
                break;
            case 19: /* Status */
                tc.setCellRenderer(dpcr);
                break;
            case 5: /* Best Position */
//                 tc.setCellRenderer(positionRenderer);
//                 tc.setCellEditor(new DefaultCellEditor( posEditorCombo ));
//                 break;
            case 20: /* position ratings*/
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                tc.setCellRenderer(ratingRenderer);
                break;
            default:
            }
        }
    }


    private void createGUI() {
        removeAll();
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("1", null, b1, null);
        tabbedPane.addTab("2", null, b2, null);
        tabbedPane.addTab("3", null, b3, null);
        tabbedPane.addTab("4", null, b4, null);
        //tabbedPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(tabbedPane, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(lineupTable.getTableHeader(), gbc);
        add(lineupTable, gbc);

        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(playersTable);
        //scrollPane.setRowHeaderView(rowHeader);
        //scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader() );
        add(scrollPane, gbc);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent ce) {
                    Component comp = ((JTabbedPane)ce.getSource()).getSelectedComponent();
                    if (!(comp instanceof Board)) return;
                    Board b = (Board)comp;
                    currentLineup = b.lineUp;
                    lineupModel.fireTableDataChanged();
                }
            } );
    }

    public void refreshData() {
        playersModel.fireTableDataChanged();
    }

    public void storeColumnSettings() {
        if (playersTable == null) return;
        TableColumnModel cm = playersTable.getColumnModel();
        if (cm == null) return;
        ArrayList<TableColumnData> ptc = options.getLineupTableColumns();
        //int columnsInView = cm.getColumnCount(); // playersTable.getColumnCount(); 

        TableColumnData [] tempArray = new TableColumnData[ptc.size()];
        int notUsedIndex = ptc.size() - 1;
        int indexInModel = 0;
        int indexInView = 0;
        int width = 80;
        for (TableColumnData data : ptc) {
            try {
                indexInView = cm.getColumnIndex(data);
            } catch (IllegalArgumentException iae) {
                //no está
                data.setActive(false);
                tempArray[notUsedIndex] = data;
                notUsedIndex--;
                continue;
            }
            indexInModel = playersTable.convertColumnIndexToModel(indexInView);
            width = cm.getColumn(indexInView).getWidth();
            data.setWidth(width);
            data.setActive(true);
            tempArray[indexInView] = data;
        }
        ArrayList<TableColumnData> newPtc = new ArrayList<TableColumnData>(ptc.size());
        for (int i=0; i<tempArray.length; i++) {
            newPtc.add( tempArray[i] );
        }
        options.setLineupTableColumns(newPtc);
    }

    /* ================================================================================ */
    /* ================================================================================ */

    private class Board extends JPanel implements MouseListener, MouseMotionListener {
        private LineupManager.Lineup lineUp;
        private ArrayList<PlayerToken> tokens;
        private Dimension dim;
        private Rectangle fieldBoundary;
        private Shape areaGK;
        private Shape areaWBL;
        private Shape areaWBR;
        private Shape areaCB;
        private Shape areaSW;
        private Shape areaDM;
        private Shape areaCM;
        private Shape areaAM;
        private Shape areaWML;
        private Shape areaWMR;
        private Shape areaFW;
        private Shape areaST;

        private PlayerToken draggedPlayerToken;
        private PlayerToken dropTargetPlayerToken;

        private DropTarget dropTarget;
        private DTListener dtListener;

        public Board(LineupManager.Lineup lup)  {
            super();
            int W = 360;
            int H = 520;
            dim = new Dimension(W, H);
            setMinimumSize(dim);
            setMaximumSize(dim);
            setPreferredSize(dim);
            setSize(dim);
            fieldBoundary = new Rectangle(20, 20, W-40, H-40);
            areaGK  = new Rectangle(80, 480, 200, 40);
            areaCB  = new Rectangle(80, 400, 200, 80);
            areaSW  = new Rectangle(80, 360, 200, 40);
            areaDM  = new Rectangle(80, 300, 200, 60);
            areaCM  = new Rectangle(80, 220, 200, 80);
            areaAM  = new Rectangle(80, 160, 200, 60);
            areaFW  = new Rectangle(80,  80, 200, 80);
            areaWBL = new Rectangle(  0, 260, 80, 260);
            areaWBR = new Rectangle(280, 260, 80, 260);
            areaWML = new Polygon( new int []{0,80,80,0}, new int []{0,80,260,260}, 4 );
            areaWMR = new Polygon( new int []{360,280,280,360}, new int []{0,80,260,260}, 4 );
            areaST  = new Polygon( new int []{0,360,280,80}, new int []{0,0,80,80}, 4 );

            setBackground(new Color(73, 128, 9));
            tokens = new ArrayList<PlayerToken>();
            setLineup(lup);
            draggedPlayerToken = null;
            dropTargetPlayerToken = null;

            dtListener = new DTListener();
            dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY, dtListener, true);
            //setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void removePlayer(int pid) {
            for (PlayerToken pt : tokens) {
                if (pt.lineupPos.getPlayerId() == pid) {
                    pt.player = null;
                    pt.lineupPos.setPlayerId(0);
                }
            }
        }

        public void setLineup(LineupManager.Lineup lup) {
            lineUp = lup;
            tokens.clear();
            for (LineupManager.LineupPosition lp : lup.getLineup()) {
                tokens.add(new PlayerToken(lp));
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            paintLines(g2d);
            //g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            for (PlayerToken pt : tokens) {
                pt.paintToken(g2d);
            }
        }

        private void paintLines(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.drawRect(2,2,355,515);
            g2d.drawLine(2,259,355,259);
            g2d.fillOval(179,258,2,3);
            g2d.drawOval(139,219,80,80);
            g2d.drawRect(79,2,200,78);
            g2d.drawRect(79,439,200,78);
        }

        private void checkPosition(Point p) {
            if      (areaGK.contains(p))  draggedPlayerToken.setPosition(GK);
            else if (areaCB.contains(p))  draggedPlayerToken.setPosition(CB);
            else if (areaSW.contains(p))  draggedPlayerToken.setPosition(SW);
            else if (areaWBL.contains(p)) draggedPlayerToken.setPosition(WB);
            else if (areaWBR.contains(p)) draggedPlayerToken.setPosition(WB);
            else if (areaDM.contains(p))  draggedPlayerToken.setPosition(DM);
            else if (areaCM.contains(p))  draggedPlayerToken.setPosition(CM);
            else if (areaAM.contains(p))  draggedPlayerToken.setPosition(AM);
            else if (areaWML.contains(p)) draggedPlayerToken.setPosition(WM);
            else if (areaWMR.contains(p)) draggedPlayerToken.setPosition(WM);
            else if (areaFW.contains(p))  draggedPlayerToken.setPosition(FW);
            else if (areaST.contains(p))  draggedPlayerToken.setPosition(ST);
        }

        /* interface MouseMotionListener */
        public void mouseDragged(MouseEvent e) {
            if (draggedPlayerToken!=null) {
                Point p = e.getPoint();
                if (!fieldBoundary.contains(p)) return;
                Rectangle2D oldPos = draggedPlayerToken.getBounds2D();
                draggedPlayerToken.setCenter(p);
                checkPosition(p);
                Rectangle2D r2d = oldPos.createUnion( draggedPlayerToken.getBounds2D() );
                repaint(0L, (int)r2d.getX(), (int)r2d.getY(), (int)r2d.getWidth(), (int)r2d.getHeight());
                //repaint();
            }
        }
        public void mouseMoved(MouseEvent e) {
            for (PlayerToken pt : tokens) {
                if (pt.contains(e.getPoint())) {
                    setCursor(CURSOR_HAND);
                    return;
                }
            }
            setCursor(null);
        }
        /* interface MouseListener */
        public void mouseClicked(MouseEvent e) {
            /* if mouse3 clicked once, remove player from token */
            if (e.getButton()==MouseEvent.BUTTON3 && e.getClickCount()==1) {
                for (PlayerToken pt : tokens) {
                    if (pt.contains(e.getPoint())) {
                        pt.setPlayer(null);
                        break;
                    }
                }
            }
        }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mousePressed(MouseEvent e) {
            for (PlayerToken pt : tokens) {
                if (pt.contains(e.getPoint())) {
                    draggedPlayerToken = pt;
                    break;
                }
            }
        }
        public void mouseReleased(MouseEvent e) {
            draggedPlayerToken = null;
        }

        /* ================================================================================ */
        private class PlayerToken {
            //private BufferedImage token;
            private LineupManager.LineupPosition lineupPos;
            private ImageIcon icon;
            private Ellipse2D.Double area;
            private PlayerProfile player;
            transient private Rectangle textFrame;

            public PlayerToken(LineupManager.LineupPosition lPos) {
                lineupPos = lPos;
                area = new Ellipse2D.Double(lineupPos.getX()-SIZE/2.0, lineupPos.getY()-SIZE/2.0, SIZE,SIZE);
                icon = null;
                textFrame = null;
                setPosition(lineupPos.getPlayingPosition());
                if (roster!=null) {
                    /* if not found then = null */
                    player = roster.getPlayer(lPos.getPlayerId());
                    // notify table ?
                }
            }

            public void setPlayer(PlayerProfile pp) {
                if (player == pp) return;
                if (pp==null) lineupPos.setPlayerId(0);
                else {
                    /* remove the player from any other token in the board */
                    removePlayer(pp.getId());
                    /* set the player to this token */
                    lineupPos.setPlayerId(pp.getId());
                }
                player = pp;

                // notify ...?
                lineupModel.fireTableDataChanged();
                playersModel.fireTableDataChanged();
                /* repaint the whole thing in case the previous name was bigger, and refresh previous token */
                textFrame = null;
                repaint();
            }

            public boolean contains(java.awt.geom.Point2D p) { return area.contains(p); }
            public Rectangle2D getBounds2D() {
                if (player == null) return area.getBounds();
                else {
                    Rectangle rect = null;
                    if (textFrame == null) {
                        int len = player.getFullName().length();
                        rect = new Rectangle((int)(lineupPos.getX() - (len*9)/2.0 - 1),
                                                       (int)(lineupPos.getY() + SIZE/2.0) + 3, len*9, 20);
                    }
                    else {
                        rect = new Rectangle((int)(lineupPos.getX() - textFrame.getWidth()/2.0 - 1),
                                                       (int)(lineupPos.getY() + SIZE/2.0) + 3,
                                             (int)textFrame.getWidth(), (int)textFrame.getHeight()*2);
                    }
                    return area.getBounds().createUnion(rect);
                }
            }
            public void setCenter(Point p) {
                area.x = p.getX()-area.width/2.0;
                area.y = p.getY()-area.height/2.0;
                lineupPos.setCenter(p);
            }

            public void paintToken(Graphics2D g2d) {
                g2d.drawImage(icon.getImage(), (int)area.x, (int)area.y, SIZE, SIZE, null);
                g2d.setColor(Color.WHITE);
                g2d.setFont(FONT_NUMBER);
                String txt = Integer.toString(lineupPos.getNumber());
                TextLayout layout = new TextLayout(txt, FONT_NUMBER, g2d.getFontRenderContext());
                Rectangle2D r2d = layout.getBounds();
                g2d.drawString(txt, (float)(lineupPos.getX() - r2d.getWidth()/2.0 - 1),
                               (float)(lineupPos.getY() + r2d.getHeight()/2.0 + 1));
                /* draw player name */
                if (player != null) {
                    g2d.setFont(FONT_PLAYERNAME);
                    txt = player.getFullName();
                    if (textFrame == null) {
                        layout = new TextLayout(txt, FONT_PLAYERNAME, g2d.getFontRenderContext());
                        r2d = layout.getBounds();
                        textFrame = r2d.getBounds();
                    }
                    else r2d = textFrame;
                    g2d.drawString(txt, (float)(lineupPos.getX() - r2d.getWidth()/2.0 - 1),
                                   (float)(lineupPos.getY() + SIZE/2 + r2d.getHeight()/2.0 + 3));
                }
                else textFrame = null;
            }

            public void setPosition(int pos) {
                if (lineupPos.getPlayingPosition()==pos && icon!=null) return;
                lineupPos.setPlayingPosition(pos);
                if (lineupModel != null) lineupModel.fireTableDataChanged();
                switch (pos) {
                case GK:
                    icon = MainFrame.getImageIcon(FILENAME_IMG_SHIRT_GK);
                    break;
                case WB:
                case CB:
                case SW:
                    icon = MainFrame.getImageIcon(FILENAME_IMG_SHIRT_DEF);
                    break;
                case DM:
                case CM:
                case AM:
                case WM:
                    icon = MainFrame.getImageIcon(FILENAME_IMG_SHIRT_MID);
                    break;
                case FW:
                case ST:
                    icon = MainFrame.getImageIcon(FILENAME_IMG_SHIRT_ATT);
                    break;
                }
            }
        } // end class PlayerToken
        /* ================================================================================ */
        private class DTListener implements DropTargetListener {

            public DTListener() {
            }

            public void drop(DropTargetDropEvent e) {
                /* validate */
                if (!e.isDataFlavorSupported(playerHandler.localPlayerProfileFlavor)) {
                    e.rejectDrop();
                    return;
                }
                // the actions that the source has specified with DragGestureRecognizer
                int sa = e.getSourceActions();
                if ( ( sa & DnDConstants.ACTION_COPY ) == 0 ) {
                    e.rejectDrop();
                    return;
                }
                /* accept the drop */
                Object data=null;
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    data = e.getTransferable().getTransferData(playerHandler.localPlayerProfileFlavor);
                    if (data == null) throw new NullPointerException();
                } catch ( Throwable t ) {
                    t.printStackTrace();
                    e.dropComplete(false);
                    return;
                }
                /* retrieve the data */
                if (data instanceof PlayerProfile) {
                    PlayerProfile pp = (PlayerProfile) data;
                    //System.out.println("DROP OK!!!: " + pp.toString());
                    if (dropTargetPlayerToken!=null) dropTargetPlayerToken.setPlayer(pp);
                }
                else {
                    //System.out.println( "drop: rejecting " + data.getClass().getName());
                    e.dropComplete(false);
                    return;
                }
                e.dropComplete(true);
            }

            public void dragEnter(DropTargetDragEvent e) {
                if(isDragOk(e) == false) {
                    e.rejectDrag();
                    return;
                }
                /* hacer algo cuando el cursor entra */
                e.acceptDrag(DnDConstants.ACTION_COPY);
            }
            public void dragOver(DropTargetDragEvent e) {
                if(isDragOk(e) == false) {
                    e.rejectDrag();
                    return;
                }
                boolean overToken = false;
                for (PlayerToken pt : tokens) {
                    if (pt.contains(e.getLocation())) {
                        dropTargetPlayerToken = pt;
                        overToken = true;
                        break;
                    }
                }
                if (overToken) e.acceptDrag(DnDConstants.ACTION_COPY);
                else e.rejectDrag();
            }
            public void dropActionChanged(DropTargetDragEvent e) {
                if(isDragOk(e) == false) {
                    e.rejectDrag();      
                    return;
                }
                e.acceptDrag(DnDConstants.ACTION_COPY);      
            }
            public void dragExit(DropTargetEvent e) {
                dropTargetPlayerToken = null;
            }

            /* ---- */
            private boolean isDragOk(DropTargetDragEvent e) {
                if (!e.isDataFlavorSupported(playerHandler.localPlayerProfileFlavor)) return false;
                // the actions specified when the source created the DragGestureRecognizer
                int sa = e.getSourceActions();
                // we're saying that these actions are necessary      
                if ((sa & DnDConstants.ACTION_COPY) == 0) return false;
                return true;
            }


        } // end class DTListener

    } // end class Board

    /* ================================================================================ */
    /* ================================================================================ */
    private class PlayersTableModel extends AbstractTableModel {

        public PlayersTableModel() {
            super();
        }

        public int getRowCount() {
            if (roster==null) return 0;
            return roster.getActivePlayerCount();
        }
        public int getColumnCount() { return LINEUPCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            if (roster == null) return " ";
            PlayerProfile pp = roster.getPlayersList().get(row);
            if (pp == null) return " ";
            switch(column) {
            case  0: return pp.getFullName();
            case  1: // board 1
            case  2: // board 2
            case  3: // board 3
            case  4: // board 4
                if (boards[column-1]==null || boards[column-1].lineUp==null) return NO_POSITION;
                return boards[column-1].lineUp.getPositionForPlayer(pp.getId());
            case  5: return pp.getData(3); // B.Pos.
            case  6: return pp.getSquadAssignment();
            case  7: return pp.getAge();
            case  8: /* Played In Last Match */
                int mid = matchRepo.getLatestMatchId(team.getId());
                if (mid==0) return MainFrame.getImageIcon(FILENAME_IMG_BLANK);
                if (roster.getPlayersList().size() <= row) return Integer.valueOf(-1);
                MatchRepository.PlayerProfile mpp = matchRepo.getPlayer(roster.getPlayersList().get(row).getId());
                if (mpp!=null && mpp.hasPlayedInMatch(mid)) return Integer.valueOf(mpp.getOrderInMatch(mid));
                return Integer.valueOf(-1);
            case  9: return Integer.valueOf(pp.getForm());
            case 10: return Integer.valueOf(pp.getTacticalDiscipline());
            case 11: return Integer.valueOf(pp.getStamina());
            case 12: return Integer.valueOf(pp.getPace());
            case 13: return Integer.valueOf(pp.getTechnique());
            case 14: return Integer.valueOf(pp.getPassing());
            case 15: return Integer.valueOf(pp.getKeeper());
            case 16: return Integer.valueOf(pp.getDefender());
            case 17: return Integer.valueOf(pp.getPlaymaker());
            case 18: return Integer.valueOf(pp.getScorer());
            case 19: return pp.getData(19); //status;
            case 20: return new DataPair(DATA_RATING, GK, pp.getBestPosition(), pp.getPositionRating(GK));
            case 21: return new DataPair(DATA_RATING, WB, pp.getBestPosition(), pp.getPositionRating(WB));
            case 22: return new DataPair(DATA_RATING, CB, pp.getBestPosition(), pp.getPositionRating(CB));
            case 23: return new DataPair(DATA_RATING, SW, pp.getBestPosition(), pp.getPositionRating(SW));
            case 24: return new DataPair(DATA_RATING, DM, pp.getBestPosition(), pp.getPositionRating(DM));
            case 25: return new DataPair(DATA_RATING, CM, pp.getBestPosition(), pp.getPositionRating(CM));
            case 26: return new DataPair(DATA_RATING, AM, pp.getBestPosition(), pp.getPositionRating(AM));
            case 27: return new DataPair(DATA_RATING, WM, pp.getBestPosition(), pp.getPositionRating(WM));
            case 28: return new DataPair(DATA_RATING, FW, pp.getBestPosition(), pp.getPositionRating(FW));
            case 29: return new DataPair(DATA_RATING, ST, pp.getBestPosition(), pp.getPositionRating(ST));
            default:
                return " ";
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_CH_NAME);
            case  1: return "1";
            case  2: return "2";
            case  3: return "3";
            case  4: return "4";
            case  5: return labelManager.getLabel(TXT_CH_BEST_POSITION);
            case  6: return labelManager.getLabel(TXT_CH_SQUAD);
            case  7: return labelManager.getLabel(TXT_CH_AGE);
            case  8: return labelManager.getLabel(TXT_CH_PLAYEDLASTMATCH);
            case  9: return labelManager.getLabel(TXT_CH_FORM);
            case 10: return labelManager.getLabel(TXT_CH_TACTDISCIP);
            case 11: return labelManager.getLabel(TXT_CH_STAMINA);
            case 12: return labelManager.getLabel(TXT_CH_PACE);
            case 13: return labelManager.getLabel(TXT_CH_TECHNIQUE);
            case 14: return labelManager.getLabel(TXT_CH_PASSING);
            case 15: return labelManager.getLabel(TXT_CH_KEEPER);
            case 16: return labelManager.getLabel(TXT_CH_DEFENDER);
            case 17: return labelManager.getLabel(TXT_CH_PLAYMAKER);
            case 18: return labelManager.getLabel(TXT_CH_SCORER);
            case 19: return labelManager.getLabel(TXT_CH_STATE);
            case 20: return labelManager.getPositionShortName(GK);
            case 21: return labelManager.getPositionShortName(WB);
            case 22: return labelManager.getPositionShortName(CB);
            case 23: return labelManager.getPositionShortName(SW);
            case 24: return labelManager.getPositionShortName(DM);
            case 25: return labelManager.getPositionShortName(CM);
            case 26: return labelManager.getPositionShortName(AM);
            case 27: return labelManager.getPositionShortName(WM);
            case 28: return labelManager.getPositionShortName(FW);
            case 29: return labelManager.getPositionShortName(ST);
            default:
                return " ";
            }
        }

    } // end class PlayersTableModel

    /* ================================================================================ */
    private class LineupTableModel extends AbstractTableModel {
        private NumberFormat numberFormat;

        public LineupTableModel() {
            super();
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
        }

        public int getRowCount() { return 11; }
        public int getColumnCount() { return 4; }

        public Object getValueAt(int row, int column) {
            if (currentLineup == null) return " ";
            LineupManager.LineupPosition lp = currentLineup.getLineup().get(row);
            if (lp == null) return " ";
            PlayerProfile pp;
            switch(column) {
            case  0: return row+1;
            case  1: /* player name */
                pp = roster.getPlayer(lp.getPlayerId());
                if (pp==null) return " ";
                return pp.getFullName();
            case  2: return lp.getPlayingPosition();
                //case  2: return labelManager.getPositionShortName(lp.getPlayingPosition());
            case  3: /* rating */
                pp = roster.getPlayer(lp.getPlayerId());
                if (pp==null) return "";
                double rat = pp.getPositionRating(lp.getPlayingPosition());
                if (rat==0d) return "";
                return numberFormat.format(rat);
            default:
                return " ";
            }
        }

        public String getColumnName(int col) {
            //return columnNames[col];
            switch(col) {
            case  0: return " ";
            case  1: return labelManager.getLabel(TXT_CH_NAME);
            case  2: return labelManager.getLabel(TXT_CH_POSITION);
            case  3: return labelManager.getLabel(TXT_CH_RATING);
            default:
                return " ";
            }
        }

    } // end class LineupTableModel

    /* ================================================================================ */
    private static class SkillCellRenderer extends DefaultTableCellRenderer {
        private LabelManager labelManager;

        public SkillCellRenderer(LabelManager lm) {
            super();
            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            labelManager = lm;
        }

        public void setValue(Object value) {
            if (value==null) return;
            if (value instanceof Integer) {
                Integer ival = (Integer)value;
                setText(ival.toString());
                setToolTipText(labelManager.getSkillLevelName(ival.intValue()));
            }
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            switch (column) {
            case 9:
                setBackground(CELLCOLOR_FORM);
                break;
            case 10:
                setBackground(CELLCOLOR_NAME);
                break;
            case 11:
            case 12:
            case 13:
            case 14:
                setBackground(CELLCOLOR_SECONDARIES);
                break;
            case 15:
            case 16:
            case 17:
            case 18:
                setBackground(CELLCOLOR_PRIMARIES);
                break;
            default:
            }
            if (isSelected) setBackground(getBackground().darker());
            return comp;
        }
    }

    /* ================================================================================ */
    /* ================================================================================ */
    private class PlayerProfileTransferHandler extends TransferHandler {
        String localPlayerProfileType = DataFlavor.javaJVMLocalObjectMimeType + ";class=so.data.PlayerProfile";
        DataFlavor localPlayerProfileFlavor = null;
        JTable source = null;

        public PlayerProfileTransferHandler() {
            try {
                localPlayerProfileFlavor = new DataFlavor(localPlayerProfileType);
            } catch (ClassNotFoundException e) {
                //System.out.println( "ArrayListTransferHandler: unable to create data flavor");
            }
        }


        public boolean importData(JComponent c, Transferable t) {
            if (!(c instanceof Board)) return false;
            Board target = null;
            PlayerProfile pp = null; // alist
            if (!canImport(c, t.getTransferDataFlavors())) {
                return false;
            }
            try {
                target = (Board)c;
                if (hasLocalPlayerProfileFlavor(t.getTransferDataFlavors())) {
                    pp = (PlayerProfile)t.getTransferData(localPlayerProfileFlavor);
                }
                else {
                    return false;
                }
            } catch (java.awt.datatransfer.UnsupportedFlavorException ufe) {
                //System.out.println("importData: unsupported data flavor");
                return false;
            } catch (java.io.IOException ioe) {
                //System.out.println("importData: I/O exception");
                return false;
            }
            /* target.setPlayer(pp); ??? NOTHING to do here */
            return true;
        }

        /* OK */
        private boolean hasLocalPlayerProfileFlavor(DataFlavor[] flavors) {
            if (localPlayerProfileFlavor == null) return false;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(localPlayerProfileFlavor)) return true;
            }
            return false;
        }
        /* OK */
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            if (hasLocalPlayerProfileFlavor(flavors))  { return true; }
            return false;
        }

        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JTable) {
                source = (JTable)c;
                int row = source.getSelectedRow();
                if (row == -1) return null;
                PlayerProfile pp = roster.getPlayersList().get( playersSorter.modelIndex(row) );
                if (pp==null) return null;
                return new PlayerProfileTransferable(pp);
            }
            return null;
        }

        public int getSourceActions(JComponent c) {
            if (c instanceof JTable) return COPY;
            else return NONE;
        }

        /* =============================================================== */
        public class PlayerProfileTransferable implements Transferable {
            PlayerProfile data;

            public PlayerProfileTransferable(PlayerProfile pp) {
                data = pp;
            }

            public Object getTransferData(DataFlavor flavor)
                throws java.awt.datatransfer.UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
                }
                return data;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { localPlayerProfileFlavor };
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                if (localPlayerProfileFlavor.equals(flavor)) return true;
                return false;
            }
        }

    } // end class PlayerProfileTransferHandler

}
