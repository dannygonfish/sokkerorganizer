package so.data;
/**
 * AbstractData.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import java.io.*;

public abstract class AbstractData implements java.io.Serializable {
    private static final long serialVersionUID = 938462197575353082L;

    protected String FILENAME;

    protected double soVersion;

    public AbstractData(String filename) {
        FILENAME = filename;
        soVersion = so.So.getVersion();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends AbstractData> T loadObject(T obj) {
        T loadedObject = null;
        String nombreArch = so.So.getBaseDirName() + so.So.getDataDirName() + File.separator + obj.FILENAME;
        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(nombreArch);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            loadedObject = (T)ois.readObject();
            ois.close();
            fis.close();
        } catch(InvalidClassException ice) { return obj;
        } catch(FileNotFoundException e) { return obj;
        } catch(IOException e) {
            if (so.So.DEBUG) e.printStackTrace();
            return obj;
        } catch(ClassNotFoundException e) { return obj;
        } catch(ClassCastException e) { return obj;
        } catch(NullPointerException e) { return obj;
        } catch(Exception e) {
            if (so.So.DEBUG) e.printStackTrace();
            return obj;
        }
        // if (loadedObject.soVersion != SO_VERSION) ...;
        return loadedObject;
    }

    public boolean save() {
        File dataDir = new File(so.So.getBaseDirName() + so.So.getDataDirName());
        File arch = null;
        File backupArch = null;
        soVersion = so.So.getVersion();
        /* save it to disk */
        try {
            if (!dataDir.exists()) dataDir.mkdir();
            arch = new File(dataDir, FILENAME);
            backupArch = new File(dataDir, FILENAME + ".bak");

            if (arch.exists()) {
                if (backupArch.exists()) backupArch.delete();
                arch.renameTo(backupArch);
            }
        } catch(SecurityException e) {
            if (so.So.DEBUG) e.printStackTrace();
            return false;
        } catch(NullPointerException e) {
            if (so.So.DEBUG) e.printStackTrace();
            return false;
        }
        FileOutputStream fos;
        ObjectOutputStream oos;
        try {
            fos = new FileOutputStream(arch);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch(IOException e) {
            if (so.So.DEBUG) e.printStackTrace();
            return false;
        }
        return true;
    }

}
