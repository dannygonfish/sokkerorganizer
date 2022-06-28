package so.launcher;

import java.io.File;

public final class Launcher {
    private static boolean JAVAWS = false;
    private static boolean CONSOLE = false;

    public static void main(String [] args) {
        if (args.length>0) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-JAVAWS")) JAVAWS = true;
                else if (arg.equalsIgnoreCase("-CONSOLE")) CONSOLE = true;
            }
        }

        if (JAVAWS) so.So.main(args);
        else {
            try {
                updateUpdater();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            so.updater.Updater.main(args);
        }
    }

    private static void updateUpdater() {
        File folder = new File( "resources" );
        File updaterNew = new File(folder, "upd.jar.new");
        if (!folder.exists()) return;
        if (!updaterNew.exists()) return;
        File updaterJar = new File(folder, "upd.jar");
        if (updaterJar.exists()) {
            if (!updaterJar.isFile() || !updaterJar.delete()) return;
        }
        if (CONSOLE) System.out.println(updaterNew.getName() + " -> " + updaterJar.getName());
        updaterNew.renameTo(updaterJar);
    }

}
