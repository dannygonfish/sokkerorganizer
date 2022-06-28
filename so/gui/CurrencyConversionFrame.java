package so.gui;

import so.config.Options;
import so.config.CountryUtils;
import so.data.CountryDataUnit;
import so.text.LabelManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;

public class CurrencyConversionFrame extends JFrame implements ItemListener, PropertyChangeListener {
    private static final String VALUE = "value";

    //private Options options;
    private LabelManager labelManager;

    private JComboBox countryCombo1;
    private JComboBox countryCombo2;
    private JFormattedTextField field1;
    private JFormattedTextField field2;
    private JLabel symbol1;
    private JLabel symbol2;

    private double rate1;
    private double rate2;

    private boolean conversionInProcess;

    public CurrencyConversionFrame(LabelManager lm, Options opt, JFrame frame) {
        super(lm.getLabel(so.Constants.Labels.TXT_CCONVERT));
        //options = opt;
        labelManager = lm;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        conversionInProcess = false;

        countryCombo1 = new JComboBox( labelManager.getCountriesVector() );
        countryCombo1.setRenderer(new so.gui.render.CountryListCellRenderer());
        countryCombo2 = new JComboBox( labelManager.getCountriesVector() );
        countryCombo2.setRenderer(new so.gui.render.CountryListCellRenderer());
        countryCombo1.setSelectedItem(new CountryDataUnit(1));
        countryCombo2.setSelectedItem(new CountryDataUnit(opt.getCountry()));

        symbol1 = new JLabel(CountryUtils.getCurrencySymbolForCountry(1));
        symbol2 = new JLabel(CountryUtils.getCurrencySymbolForCountry(opt.getCountry()));
        rate1 = 1.0;
        rate2 = CountryUtils.getCurrencyConversionRateForCountry(opt.getCountry());

        field1 = new JFormattedTextField(new Double(0.0));
        field2 = new JFormattedTextField(new Double(0.0));
        field1.setHorizontalAlignment(JFormattedTextField.RIGHT);
        field2.setHorizontalAlignment(JFormattedTextField.RIGHT);

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(4,8,4,8);
        gbc2.insets = new java.awt.Insets(4,0,4,8);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = gbc2.gridy = 0;
        gbc.gridwidth = gbc2.gridwidth = 1;
        gbc.gridheight = gbc2.gridheight = 1;
        gbc.weightx = 1.0;
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.weightx = 0.0;
        add(field1, gbc);
        add(symbol1, gbc2);
        add(new JLabel(" = "), gbc);
        add(field2, gbc);
        add(symbol2, gbc2);

        gbc.gridy = gbc2.gridy = 1;
        gbc.gridwidth = 2;
        add(countryCombo1, gbc);
        add(javax.swing.Box.createHorizontalGlue() , gbc2);
        add(countryCombo2, gbc);

        countryCombo1.addItemListener(this);
        countryCombo2.addItemListener(this);
        field1.addPropertyChangeListener(VALUE, this);
        field2.addPropertyChangeListener(VALUE, this);
        setIconImage(so.So.loadSoImage());
        pack();
        setLocationRelativeTo(frame);
        setResizable(false);
    }


    /* interface ItemListener */
    public void itemStateChanged(java.awt.event.ItemEvent ie) {
        if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (ie.getSource().equals(countryCombo1)) {
                CountryDataUnit cdu = (CountryDataUnit)countryCombo1.getSelectedItem();
                symbol1.setText(CountryUtils.getCurrencySymbolForCountry( cdu.getId() ));
                rate1 = CountryUtils.getCurrencyConversionRateForCountry( cdu.getId() );
                conversionInProcess = true;
                double val = (Double)field2.getValue();
                field1.setValue(new Double(val / rate2 * rate1));
            }
            else if (ie.getSource().equals(countryCombo2)) {
                CountryDataUnit cdu = (CountryDataUnit)countryCombo2.getSelectedItem();
                symbol2.setText(CountryUtils.getCurrencySymbolForCountry( cdu.getId() ));
                rate2 = CountryUtils.getCurrencyConversionRateForCountry( cdu.getId() );
                conversionInProcess = true;
                double val = (Double)field1.getValue();
                field2.setValue(new Double(val / rate1 * rate2));
            }
            conversionInProcess = false;
        }
    }

    /* interface PropertyChangeListener */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(VALUE)) {
            if (evt.getSource().equals(field1)) {
                if (conversionInProcess) return;
                conversionInProcess = true;
                double val = (Double)field1.getValue();
                field2.setValue(new Double(val / rate1 * rate2));
            }
            else if (evt.getSource().equals(field2)) {
                if (conversionInProcess) return;
                conversionInProcess = true;
                double val = (Double)field2.getValue();
                field1.setValue(new Double(val / rate2 * rate1));
            }
            conversionInProcess = false;
        }
    }

}
