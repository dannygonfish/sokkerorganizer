package so.util;

/*
 * @(#)ApolloFileFilter.java	1.16 04/07/26
 * 
 */


import java.io.File;

public class ApolloFileFilter extends ExampleFileFilter implements java.io.FileFilter, java.io.FilenameFilter {

    private int apolloVersion;

    public ApolloFileFilter(String extension, String description, int version) {
        super(extension, description);
        apolloVersion = version;
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
            if (apolloVersion == 1) {
                if (! Character.isDigit( name.charAt(0) )) return false;
                if (! Character.isDigit( name.charAt(1) )) return false;
                if (! Character.isDigit( name.charAt(2) )) return false;
                if (! Character.isDigit( name.charAt(3) )) return false;
                if (name.charAt(4) != '-') return false;
                if (! Character.isDigit( name.charAt(5) )) return false;
                if (! Character.isDigit( name.charAt(6) )) return false;
                if (name.charAt(7) != '-') return false;
                if (! Character.isDigit( name.charAt(8) )) return false;
                if (! Character.isDigit( name.charAt(9) )) return false;
                if (name.charAt(10)!= '-') return false;
                if (! Character.isDigit( name.charAt(11) )) return false;
                if (! Character.isDigit( name.charAt(12) )) return false;
                if (name.charAt(13) != '-') return false;
                if (! Character.isDigit( name.charAt(14) )) return false;
                if (! Character.isDigit( name.charAt(15) )) return false;
                return true;
            }
            else if ( name.startsWith("team-") || name.startsWith("players-") ||
                 name.startsWith("juniors-") || name.startsWith("trainers-") ) return true;
        }
        return false;
    }
    /* INTERFACE java.io.FilenameFilter */
    public boolean accept(File dir, String name) {
        if (super.accept(dir, name)) {
            if (apolloVersion == 1) {
                if (! Character.isDigit( name.charAt(0) )) return false;
                if (! Character.isDigit( name.charAt(1) )) return false;
                if (! Character.isDigit( name.charAt(2) )) return false;
                if (! Character.isDigit( name.charAt(3) )) return false;
                if (name.charAt(4) != '-') return false;
                if (! Character.isDigit( name.charAt(5) )) return false;
                if (! Character.isDigit( name.charAt(6) )) return false;
                if (name.charAt(7) != '-') return false;
                if (! Character.isDigit( name.charAt(8) )) return false;
                if (! Character.isDigit( name.charAt(9) )) return false;
                if (name.charAt(10)!= '-') return false;
                if (! Character.isDigit( name.charAt(11) )) return false;
                if (! Character.isDigit( name.charAt(12) )) return false;
                if (name.charAt(13) != '-') return false;
                if (! Character.isDigit( name.charAt(14) )) return false;
                if (! Character.isDigit( name.charAt(15) )) return false;
                return true;
            }
            else if ( name.startsWith("team-") || name.startsWith("players-") ||
                 name.startsWith("juniors-") || name.startsWith("trainers-") ) return true;
        }
        return false;
    }

}
