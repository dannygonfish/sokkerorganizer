package so.config;

import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Labels.*;
import static so.Constants.Positions.*;
import so.text.LabelManager;
import so.data.PlayerProfile;
import so.data.PlayerRoster;
import so.data.DataPair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Box;
import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;

public class PositionManager extends so.data.AbstractData implements Serializable {
    private static final long serialVersionUID = -3653558209749606524L;

    protected static final Insets LEFT_INSETS        = new Insets(0,16,0,0);
    protected static final Insets RIGHT_INSETS       = new Insets(0,0,0,16);
    protected static final Insets SKILL_RIGHT_INSETS = new Insets(0,0,0,30);
    protected static final Insets NO_INSETS          = new Insets(0,0,0,0);

    private double formModifier;
    private HashMap<Integer, PositionFormula> formulas;

    public PositionManager() {
        super(FILENAME_FORMULAS);
        formulas = new HashMap<Integer, PositionFormula>();
        formModifier = 0.15;
    }

    public double getPositionRating(int position, PlayerProfile player) {
        PositionFormula formula = formulas.get(position);
        if (formula == null) {
            formula = getDefaultFormula(position);
            formulas.put(position, formula);
        }
        return formula.getRating(player) *  ( (player.getForm()-8.5)/8.5 * formModifier + 1 );
    }

    protected PositionFormula getDefaultFormula(int position) {
        switch (position) {
            //(int pos,  stam,  pace,  tech,  pass,  keep,  def,  play, scor)
        case GK: return new PositionFormula(GK, 5, 20,  0, 20, 55,  0,  0,  0);
        case WB: return new PositionFormula(WB, 5, 25, 20, 10,  0, 30, 10,  0);
        case CB: return new PositionFormula(CB, 5, 20, 15, 15,  0, 35, 10,  0);
        case SW: return new PositionFormula(SW, 5, 25, 20, 10,  0, 35, 10,  0);
        case DM: return new PositionFormula(DM, 5, 15, 15, 20,  0, 15, 30,  0);
        case CM: return new PositionFormula(CM, 5, 10, 15, 25,  0, 10, 35,  0);
        case AM: return new PositionFormula(AM, 5, 10, 15, 25,  0,  5, 30, 10);
        case WM: return new PositionFormula(WM, 5, 30, 15, 20,  0, 10, 20,  5);
        case FW: return new PositionFormula(FW, 5, 30, 10, 10,  0,  5, 10, 30);
        case ST: return new PositionFormula(ST, 5, 25, 15,  5,  0,  5, 10, 35);
        default: return new PositionFormula(NO_POSITION, 1,  1,  1,  1,  1,  1,  1,  1);
        }
    }

    public static PositionManager load() {
        return loadObject( new PositionManager() );
    }

    public int showFormulasEditDialog(java.awt.Component parent, LabelManager labelManager, PlayerRoster roster ) {
        FormulaEditorPanel fedp = new FormulaEditorPanel(labelManager, roster);

        int dlg = JOptionPane.showConfirmDialog(parent, fedp, labelManager.getLabel(TXT_FORMULAS),
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        int ret = OPT_NO_CHANGE;

        if (dlg == JOptionPane.OK_OPTION) {
            formulas = fedp.getEditingFormulas();
            formModifier = fedp.getFormModifier();
            ret = OPT_FORMULAS;
        }
        return ret;
    }

    // ###############################################################
    // ###############################################################
    protected static class PositionFormula implements Serializable {
        private static final long serialVersionUID = -6164932633944777686L;

        private int position;
        private double staminaFactor;
        private double paceFactor;
        private double techniqueFactor;
        private double passingFactor;
        private double keeperFactor;
        private double defenderFactor;
        private double playmakerFactor;
        private double scorerFactor;
        private double standardTotal;

        public PositionFormula(int pos, double stam, double pace, double tech, double pass,
                               double keep, double def, double play, double scor) {
            position = pos;
            staminaFactor = stam;
            paceFactor = pace;
            techniqueFactor = tech;
            passingFactor = pass;
            keeperFactor = keep;
            defenderFactor = def;
            playmakerFactor = play;
            scorerFactor = scor;
            standardTotal = staminaFactor + paceFactor + techniqueFactor + passingFactor
                + keeperFactor + defenderFactor + playmakerFactor + scorerFactor;
        }

        public double getRating(PlayerProfile player) {
            double rating = staminaFactor * player.getStamina() + paceFactor * player.getPace()
                + techniqueFactor * player.getTechnique() + passingFactor * player.getPassing()
                + keeperFactor * player.getKeeper() + defenderFactor * player.getDefender()
                + playmakerFactor * player.getPlaymaker() + scorerFactor * player.getScorer();
            return (rating / standardTotal) * 100.0/17.0;
        }

        public String toString() {
            return so.gui.MainFrame.getPositionShortName(position);
        }

    }

    // #############################################################################################
    // #############################################################################################

    protected class FormulaEditorPanel extends JPanel implements ChangeListener, ActionListener, ItemListener {
        private LabelManager labelManager;
        private PlayerRoster roster;
        private java.text.NumberFormat decFormat;

        private JButton setButton;
        private JButton undoButton;
        private JButton resetButton;
        private JComboBox positionCombo;
        private JComboBox playerCombo;
        private JSlider formSlider;
        private JSlider stamSlider;
        private JSlider paceSlider;
        private JSlider techSlider;
        private JSlider passSlider;
        private JSlider keepSlider;
        private JSlider defeSlider;
        private JSlider playSlider;
        private JSlider scorSlider;

        /* labels with variable texts/values */
        private JLabel formPLabel;
        private JLabel stamPLabel;
        private JLabel pacePLabel;
        private JLabel techPLabel;
        private JLabel passPLabel;
        private JLabel keepPLabel;
        private JLabel defePLabel;
        private JLabel playPLabel;
        private JLabel scorPLabel;
        private JLabel ratingWithFormLabel;
        private JLabel ratingWithoutFormLabel;
        private JLabel factorSumLabel;

        /* temporary Editable values */
        private HashMap<Integer, PositionFormula> editingFormulas;
        private int currentPosition;
        private PositionFormula currentFormula;
        private double tmpFormModifier;
        private double tmpStaminaFactor;
        private double tmpPaceFactor;
        private double tmpTechniqueFactor;
        private double tmpPassingFactor;
        private double tmpKeeperFactor;
        private double tmpDefenderFactor;
        private double tmpPlaymakerFactor;
        private double tmpScorerFactor;
        //private double tmpStandardTotal;
        private PlayerProfile selectedPlayer;
        private boolean currentFormulaIsModified;

        @SuppressWarnings("unchecked")
        public FormulaEditorPanel(LabelManager lm, PlayerRoster pr) {
            super(new GridBagLayout());
            labelManager = lm;
            roster = pr;
            decFormat = java.text.NumberFormat.getInstance();
            decFormat.setMaximumFractionDigits(1);
            decFormat.setMinimumFractionDigits(1);
            decFormat.setMinimumIntegerDigits(2);

            currentPosition = RATEABLE_POSITIONS[0]; // GK
            tmpFormModifier = formModifier;
            editingFormulas = (HashMap<Integer, PositionFormula>)formulas.clone();
            currentFormula = editingFormulas.get( currentPosition );
            currentFormulaIsModified = false;
            getFactorValuesFromFormula();
            java.util.List<PlayerProfile> _players = roster.getPlayersList();
            //% IMPLEMENTAR TAL VEZ A FUTURO SI ES NECESARIO
            if (_players==null || _players.size()==0) {
                playerCombo = new JComboBox();
                playerCombo.addItem(PlayerProfile.getRandomPlayer());
                playerCombo.addItem(PlayerProfile.getRandomPlayer());
                playerCombo.addItem(PlayerProfile.getRandomPlayer());
            }
            else playerCombo = new JComboBox( new Vector<PlayerProfile>(_players) );
            playerCombo.addItemListener(this);
            selectedPlayer = (PlayerProfile)playerCombo.getSelectedItem();

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridheight = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(5,5,5,5);
            JPanel formSubPanel = createFormSubPanel();
            add(formSubPanel, gbc);

            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 2.0;
            JPanel formulaEditSubPanel = createFormulaEditSubPanel();
            add(formulaEditSubPanel, gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = GridBagConstraints.RELATIVE;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            JPanel playerViewSubPanel = createPlayerViewSubPanel();
            add(playerViewSubPanel, gbc);

            gbc.weighty = 0.0;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.ipadx = 8;
            gbc.ipady = 12;
            JPanel ratingViewSubPanel = createRatingViewSubPanel();
            add(ratingViewSubPanel, gbc);

            updateFactorSumLabel();
            updateRatingLabels(false);
            enableButtons(false);
        }

        protected HashMap<Integer, PositionFormula> getEditingFormulas() { return editingFormulas; }
        protected double getFormModifier() { return tmpFormModifier; }

        private JPanel createFormSubPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel jp = new JPanel(gbl);
            //JPanel jp = new JPanel();

            formSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(formModifier*100));
            JSpinner formSpinner = new JSpinner( new SpinnerNumberModel((int)(formModifier*100), 0, 100, 1) );
            formSlider.setMajorTickSpacing(10);
            formSlider.setMinorTickSpacing(1);
            formSlider.setPaintTicks(true);
            formSlider.setPaintLabels(true);
            //formSlider.setSnapToTicks(true);
            SliderAndSpinnerConnector sascForm = new SliderAndSpinnerConnector(formSlider, formSpinner);
            formSlider.addChangeListener(sascForm);
            formSpinner.addChangeListener(sascForm);
            formSlider.addChangeListener(this);

            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            addLabelToPanel(TXT_FORM_MODIFIER, jp, gbc);

            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            jp.add(formSlider, gbc);

            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add(formSpinner, gbc);

            //jp.setBorder( BorderFactory.createLineBorder( Color.ORANGE , 1 ) );
            return jp;
        }

        private JPanel createFormulaEditSubPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel jp = new JPanel(gbl);

            factorSumLabel = new JLabel("800", JLabel.CENTER);
            factorSumLabel.setBackground(CELLCOLOR_PREF_POS);
            factorSumLabel.setOpaque(true);
            factorSumLabel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder() ,
                                                              BorderFactory.createLoweredBevelBorder() ) );
            positionCombo = new JComboBox();
            positionCombo.setBackground( Color.WHITE );
            for (int _pos : RATEABLE_POSITIONS)
                positionCombo.addItem(new DataPair(DATA_POSITION_LONGNAME, _pos,
                                                   labelManager.getPositionShortName(_pos),
                                                   labelManager.getPositionLongName(_pos) ));
            positionCombo.addItemListener(this);

            gbc.insets = new Insets(2,2,2,2);
            gbc.gridwidth = 1; //GridBagConstraints.RELATIVE;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            //addLabelToPanel(TXT_POSITION, jp, gbc);

            jp.add(positionCombo, gbc);

            //jp.add(Box.createHorizontalGlue(), gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.ipadx = 12;
            jp.add(factorSumLabel, gbc);

            //staminaPanel
            stamSlider = new JSlider(0, 100, (int)tmpStaminaFactor);
            paceSlider = new JSlider(0, 100, (int)tmpPaceFactor);
            techSlider = new JSlider(0, 100, (int)tmpTechniqueFactor);
            passSlider = new JSlider(0, 100, (int)tmpPassingFactor);
            keepSlider = new JSlider(0, 100, (int)tmpKeeperFactor);
            defeSlider = new JSlider(0, 100, (int)tmpDefenderFactor);
            playSlider = new JSlider(0, 100, (int)tmpPlaymakerFactor);
            scorSlider = new JSlider(0, 100, (int)tmpScorerFactor);
            gbc.gridwidth = 2;
            jp.add( createSkillMiniPanel(TXT_STAMINA,   stamSlider) , gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( createSkillMiniPanel(TXT_KEEPER,    keepSlider) , gbc);

            gbc.gridwidth = 2;
            jp.add( createSkillMiniPanel(TXT_PACE,      paceSlider) , gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( createSkillMiniPanel(TXT_DEFENDER,  defeSlider) , gbc);

            gbc.gridwidth = 2;
            jp.add( createSkillMiniPanel(TXT_TECHNIQUE, techSlider) , gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( createSkillMiniPanel(TXT_PLAYMAKER, playSlider) , gbc);

            gbc.gridwidth = 2;
            jp.add( createSkillMiniPanel(TXT_PASSING,   passSlider) , gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( createSkillMiniPanel(TXT_SCORER,    scorSlider) , gbc);

            gbc.gridheight = GridBagConstraints.REMAINDER;
            JPanel buttonsMiniPanel = new JPanel(new java.awt.GridLayout(1,3,12,10));
            buttonsMiniPanel.add( setButton   = createButton(TXT_SET,   TT+TXT_SET) );
            buttonsMiniPanel.add( undoButton  = createButton(TXT_UNDO,  TT+TXT_UNDO) );
            buttonsMiniPanel.add( resetButton = createButton(TXT_RESET, TT+TXT_RESET) );
            jp.add( buttonsMiniPanel, gbc );

            jp.setBorder( BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED) );
            return jp;
        }
        private JButton createButton(String label, String toolTip) {
            JButton button = new JButton(labelManager.getLabel(label));
            button.setActionCommand(label);
            button.setToolTipText(labelManager.getLabel(toolTip));
            button.addActionListener(this);
            return button;
       }

        private JPanel createSkillMiniPanel(String skillName, JSlider slider) {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel panel = new JPanel(gbl);
            JLabel skillLabel = new JLabel( labelManager.getLabel(skillName) );
            slider.setMajorTickSpacing(10);
            //slider.setMinorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            //slider.setSnapToTicks(true);
            JSpinner spinner = new JSpinner( new SpinnerNumberModel(slider.getValue(), 0, 100, 1) );
            SliderAndSpinnerConnector sasc = new SliderAndSpinnerConnector(slider, spinner);
            slider.addChangeListener(sasc);
            spinner.addChangeListener(sasc);
            slider.addChangeListener(this);

            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = LEFT_INSETS;
            gbc.anchor = GridBagConstraints.LINE_START;
            panel.add(skillLabel, gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = RIGHT_INSETS;
            gbc.anchor = GridBagConstraints.LINE_END;
            panel.add(spinner, gbc);

            gbc.insets = NO_INSETS;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(slider, gbc);

            panel.setBorder( BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED) );
            return panel;
        }

        private JPanel createPlayerViewSubPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel jp = new JPanel(gbl);

            playerCombo.setBackground( Color.WHITE );

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            jp.add(new JLabel(labelManager.getLabel(TXT_PLAYER)), gbc);
            jp.add(playerCombo, gbc);

            //gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( Box.createVerticalStrut(10) , gbc );

            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            addSkillNameLabelToPanel(TXT_FORM, jp, gbc, CELLCOLOR_FORM);
            addSkillValueLabelToPanel( formPLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_STAMINA, jp, gbc, CELLCOLOR_SECONDARIES);
            addSkillValueLabelToPanel( stamPLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_PACE, jp, gbc, CELLCOLOR_SECONDARIES);
            addSkillValueLabelToPanel( pacePLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_TECHNIQUE, jp, gbc, CELLCOLOR_SECONDARIES);
            addSkillValueLabelToPanel( techPLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_PASSING, jp, gbc, CELLCOLOR_SECONDARIES);
            addSkillValueLabelToPanel( passPLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_KEEPER, jp, gbc, CELLCOLOR_PRIMARIES);
            addSkillValueLabelToPanel( keepPLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_DEFENDER, jp, gbc, CELLCOLOR_PRIMARIES);
            addSkillValueLabelToPanel( defePLabel = createSkillLabel() , jp , gbc );
            addSkillNameLabelToPanel(TXT_PLAYMAKER, jp, gbc, CELLCOLOR_PRIMARIES);
            addSkillValueLabelToPanel( playPLabel = createSkillLabel() , jp , gbc );
            gbc.gridheight = GridBagConstraints.REMAINDER;
            addSkillNameLabelToPanel(TXT_SCORER, jp, gbc, CELLCOLOR_PRIMARIES);
            addSkillValueLabelToPanel( scorPLabel = createSkillLabel() , jp , gbc );

            setPlayerSkillsToLabels();
            jp.setBorder( BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED) );
            return jp;
        }
        private JLabel createSkillLabel() {
            JLabel lbl = new JLabel("88", JLabel.RIGHT);
            //lbl.setBackground(Color.WHITE);
            lbl.setBackground( CELLCOLOR_SECONDARIES.brighter() );
            lbl.setOpaque(true);
            //lbl.setPreferredSize( new java.awt.Dimension(30,10) );
            return lbl;
        }
        private void addSkillNameLabelToPanel(String labelTextKey, JPanel panel, GridBagConstraints gbc, Color bgc) {
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridwidth = 1;
            gbc.insets = LEFT_INSETS;
            JLabel lbl = addLabelToPanel(labelTextKey, panel, gbc);
            lbl.setBackground(bgc);
            lbl.setOpaque(true);
        }
        private void addSkillValueLabelToPanel(JLabel skillLabel, JPanel panel, GridBagConstraints gbc) {
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = SKILL_RIGHT_INSETS;
            panel.add(skillLabel, gbc);
        }

        private JLabel addLabelToPanel(String labelTextKey, JPanel panel, GridBagConstraints gbc) {
            JLabel lbl = new JLabel( labelManager.getLabel(labelTextKey) );
            //lbl.setBorder( BorderFactory.createLineBorder( Color.ORANGE , 1 ) );
            panel.add(lbl, gbc);
            return lbl;
        }

        private JPanel createRatingViewSubPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel jp = new JPanel(gbl);
            Font ratingsFont = new Font("Times New Roman", Font.BOLD, 28);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            jp.add( new JLabel(labelManager.getLabel(TXT_RATING_FOR_POS)) , gbc );

            gbc.gridwidth = 1;
            gbc.insets = new Insets(0,4,0,4);
            jp.add( new JLabel(labelManager.getLabel(TXT_WITH_FORM)) , gbc );
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            jp.add( new JLabel(labelManager.getLabel(TXT_WITHOUT_FORM)) , gbc );

            gbc.gridwidth = 1;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.ipadx = 12;
            gbc.ipady = 2;
            gbc.insets = new Insets(3,2,5,2);
            ratingWithFormLabel = new JLabel("88,8", JLabel.CENTER);
            ratingWithFormLabel.setFont(ratingsFont);
            ratingWithFormLabel.setOpaque(true);
            ratingWithFormLabel.setForeground( Color.BLUE );
            ratingWithFormLabel.setBackground( COLOR_LIGHT_YELLOW );
            jp.add(ratingWithFormLabel, gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            ratingWithoutFormLabel = new JLabel("88,8", JLabel.CENTER);
            ratingWithoutFormLabel.setFont(ratingsFont);
            ratingWithoutFormLabel.setOpaque(true);
            ratingWithoutFormLabel.setForeground( Color.GREEN.darker() );
            ratingWithoutFormLabel.setBackground( COLOR_LIGHT_YELLOW );
            jp.add(ratingWithoutFormLabel, gbc);

            ratingWithFormLabel.setBorder( BorderFactory.createRaisedBevelBorder() );
            ratingWithoutFormLabel.setBorder( BorderFactory.createRaisedBevelBorder() );
            jp.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder() ,
                                                              BorderFactory.createLoweredBevelBorder() ) );
            return jp;
        }

        private void getFactorValuesFromFormula() {
            if (currentFormula == null) currentFormula = getDefaultFormula( currentPosition );
            tmpStaminaFactor   = currentFormula.staminaFactor;
            tmpPaceFactor      = currentFormula.paceFactor;
            tmpTechniqueFactor = currentFormula.techniqueFactor;
            tmpPassingFactor   = currentFormula.passingFactor;
            tmpKeeperFactor    = currentFormula.keeperFactor;
            tmpDefenderFactor  = currentFormula.defenderFactor;
            tmpPlaymakerFactor = currentFormula.playmakerFactor;
            tmpScorerFactor    = currentFormula.scorerFactor;

        }
        private void setFactorValuesToSliders() {
            stamSlider.setValue((int)tmpStaminaFactor);
            paceSlider.setValue((int)tmpPaceFactor);
            techSlider.setValue((int)tmpTechniqueFactor);
            passSlider.setValue((int)tmpPassingFactor);
            keepSlider.setValue((int)tmpKeeperFactor);
            defeSlider.setValue((int)tmpDefenderFactor);
            playSlider.setValue((int)tmpPlaymakerFactor);
            scorSlider.setValue((int)tmpScorerFactor);
        }

        private void setPlayerSkillsToLabels() {
            formPLabel.setText( Integer.toString(selectedPlayer.getForm()) );
            stamPLabel.setText( Integer.toString(selectedPlayer.getStamina()) );
            pacePLabel.setText( Integer.toString(selectedPlayer.getPace()) );
            techPLabel.setText( Integer.toString(selectedPlayer.getTechnique()) );
            passPLabel.setText( Integer.toString(selectedPlayer.getPassing()) );
            keepPLabel.setText( Integer.toString(selectedPlayer.getKeeper()) );
            defePLabel.setText( Integer.toString(selectedPlayer.getDefender()) );
            playPLabel.setText( Integer.toString(selectedPlayer.getPlaymaker()) );
            scorPLabel.setText( Integer.toString(selectedPlayer.getScorer()) );
        }

        private void updateFactorSumLabel() {
            int sum = (int)(tmpStaminaFactor + tmpPaceFactor + tmpTechniqueFactor + tmpPassingFactor +
                            tmpKeeperFactor + tmpDefenderFactor + tmpPlaymakerFactor + tmpScorerFactor);
            factorSumLabel.setText( Integer.toString(sum) );
        }
        private void updateRatingLabels(boolean formulaChanged) {
            if (formulaChanged) currentFormula = new PositionFormula(currentPosition, tmpStaminaFactor, tmpPaceFactor,
                                                                  tmpTechniqueFactor, tmpPassingFactor, tmpKeeperFactor,
                                                                  tmpDefenderFactor, tmpPlaymakerFactor, tmpScorerFactor);
            double _rating = currentFormula.getRating(selectedPlayer);
            double _rating_form = _rating * ( (selectedPlayer.getForm()-8.5)/8.5 * tmpFormModifier + 1 );
            ratingWithoutFormLabel.setText( decFormat.format(_rating) );
            ratingWithFormLabel.setText( decFormat.format(_rating_form) );
        }

        private void enableButtons(boolean enable) {
            setButton.setEnabled(enable);
            undoButton.setEnabled(enable);
            currentFormulaIsModified = enable;
        }

        // **************************************************************************
        /* interface ChangeListener */
        public void stateChanged(ChangeEvent e) {
            /* the form Slider has changed */
            if (e.getSource().equals( formSlider )) {
                int _formMod = formSlider.getValue();
                tmpFormModifier = _formMod / 100.0;
                /* update rating labels */
                updateRatingLabels(false);
            }
            else {
                if (e.getSource().equals( stamSlider )) tmpStaminaFactor = stamSlider.getValue();
                else if (e.getSource().equals( paceSlider )) tmpPaceFactor = paceSlider.getValue();
                else if (e.getSource().equals( techSlider )) tmpTechniqueFactor = techSlider.getValue();
                else if (e.getSource().equals( passSlider )) tmpPassingFactor = passSlider.getValue();
                else if (e.getSource().equals( keepSlider )) tmpKeeperFactor = keepSlider.getValue();
                else if (e.getSource().equals( defeSlider )) tmpDefenderFactor = defeSlider.getValue();
                else if (e.getSource().equals( playSlider )) tmpPlaymakerFactor = playSlider.getValue();
                else if (e.getSource().equals( scorSlider )) tmpScorerFactor = scorSlider.getValue();
                updateFactorSumLabel();
                updateRatingLabels(true);
                if (!currentFormulaIsModified) enableButtons(true);
            }
        }
        /* interface ItemListener */
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                if (ie.getSource().equals(positionCombo)) {
                    // if not saved formula, maybe a warning
                    int pos = ((DataPair)positionCombo.getSelectedItem()).getValue();
                    currentPosition = pos;
                    currentFormula = editingFormulas.get( currentPosition );
                    /* load formula values into tmp variables */
                    getFactorValuesFromFormula();
                    /* put formula values in sliders */
                    setFactorValuesToSliders();
                    /* update rating labels */
                    updateRatingLabels(true);
                    if (currentFormulaIsModified) enableButtons(false);
                }
                else if (ie.getSource().equals(playerCombo)) {
                    selectedPlayer = (PlayerProfile)playerCombo.getSelectedItem();
                    /* load player skills into player view panel */
                    setPlayerSkillsToLabels();
                    /* update rating labels */
                    updateRatingLabels(false);
                }
            }
        }
        /* interface ActionListener */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals(TXT_SET)) {
                editingFormulas.put(currentPosition, currentFormula);
                if (currentFormulaIsModified) enableButtons(false);
            }
            else if (ae.getActionCommand().equals(TXT_UNDO)) {
                /* reload saved formula */
                currentFormula = editingFormulas.get( currentPosition );
                /* load formula values into tmp variables */
                getFactorValuesFromFormula();
                /* put formula values in sliders */
                setFactorValuesToSliders();
                /* update rating labels */
                updateRatingLabels(true);
                if (currentFormulaIsModified) enableButtons(false);
            }
            else if (ae.getActionCommand().equals(TXT_RESET)) {
                /* regenerate default formula */
                currentFormula = getDefaultFormula( currentPosition );
                editingFormulas.put(currentPosition, currentFormula);
                /* load formula values into tmp variables */
                getFactorValuesFromFormula();
                /* put formula values in sliders */
                setFactorValuesToSliders();
                /* update rating labels */
                updateRatingLabels(true);
                if (currentFormulaIsModified) enableButtons(false);
            }
        }

    } // end class FormulaEditorPanel

    // -------------------------------
    public static class SliderAndSpinnerConnector implements ChangeListener {
        private JSlider slider;
        private JSpinner spinner;
        private boolean sliderChanging;

        public SliderAndSpinnerConnector(JSlider slider, JSpinner spinner) {
            this.slider = slider;
            this.spinner = spinner;
            sliderChanging = false;
        }

        public void stateChanged(ChangeEvent e) {
            /* the Slider has changed */
            if (e.getSource().equals( slider )) {
                if (!sliderChanging) {
                    sliderChanging = true;
                    //spinner.setValue(new Integer( slider.getValue() ));
                    spinner.setValue( slider.getValue() );
                }
                sliderChanging = false;
            }
            /* the Spinner has changed */
            else if (e.getSource().equals( spinner )) {
                if (!sliderChanging) {
                    sliderChanging = true;
                    slider.setValue( (Integer)spinner.getValue() );
                }
            }
        }
    } // end class S&SConnector

} //end class PositionManager
