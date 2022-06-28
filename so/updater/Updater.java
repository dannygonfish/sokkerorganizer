package so.updater;

import java.io.File;
import java.io.FilenameFilter;

public final class Updater {
    private static boolean JAVAWS = false;
    private static boolean CONSOLE = false;
    //private static boolean DEBUG = false;

    public static void main(String [] args) {
        if (args.length>0) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-JAVAWS")) JAVAWS = true;
                else if (arg.equalsIgnoreCase("-CONSOLE")) CONSOLE = true;
                //else if (arg.equalsIgnoreCase("-DEBUG")) DEBUG = true;
            }
        }

        if (!JAVAWS) updateNewResources();

        so.So.main(args);
    }

    private static void updateNewResources() {
        updateNewResources("resources");
        updateNewResources("resources" + File.separator + "plaf");
    }

    private static void updateNewResources(String directory) {
        File folder = new File(directory);
        if (!folder.exists()) folder.mkdirs();
        final FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return "new".equals(getExtension(name));
                }
            };

        String [] newFiles = folder.list( filter );
        if (newFiles==null) return;
        for (String filename : newFiles) {
            int p = filename.lastIndexOf('.');
            File old = new File(folder, filename.substring(0, p));
            File updated = new File(folder, filename);
            if (CONSOLE) System.out.println(updated.getName() + " -> " + old.getName());
            if (old.exists()) {
                if (!old.isFile() || !old.delete()) continue;
            }
            updated.renameTo(old);
            //log here
        }

    }


    /* ************************************************************ */
    private static String getExtension(String filename) {
        if(filename != null) {
            int i = filename.lastIndexOf('.');
            if(i>0 && i<filename.length()-1) {
                return filename.substring(i+1).toLowerCase();
            }
        }
        return "";
    }

}
