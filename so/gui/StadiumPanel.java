package so.gui;

import so.data.*;
import so.text.LabelManager;
import so.config.Options;
import javax.swing.*;


public class StadiumPanel extends JPanel  {
    private LabelManager labelManager;
    private Options options;
    private Stadium stadium;

    public StadiumPanel(LabelManager lm, Options opt, Stadium sta) {
        super();
        labelManager = lm;
        options = opt;
        stadium = sta;
    }

}
