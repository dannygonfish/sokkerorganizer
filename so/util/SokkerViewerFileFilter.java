package so.util;

/*
 * @(#)SokkerViewerFileFilter.java	1.16 04/07/26
 * 
 */


import java.io.File;

public class SokkerViewerFileFilter extends ExampleFileFilter implements java.io.FileFilter, java.io.FilenameFilter {

    public SokkerViewerFileFilter(String extension, String description) {
        super(extension, description);
    }


    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter#accepts
     */
    public boolean accept(File f) {
        if(super.accept(f)) {
            if(f.isDirectory()) return true;
            String name = f.getName();
            if ( name.startsWith("team_") || name.startsWith("players_") ||
                 name.startsWith("juniors_") || name.startsWith("trainers_") ) return true;
        }
        return false;
    }
    /* INTERFACE java.io.FilenameFilter */
    public boolean accept(File dir, String name) {
        if (super.accept(dir, name)) {
            if ( name.startsWith("team_") || name.startsWith("players_") ||
                 name.startsWith("juniors_") || name.startsWith("trainers_") ) return true;
        }
        return false;
    }

}
