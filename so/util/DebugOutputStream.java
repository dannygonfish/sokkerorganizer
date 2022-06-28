package so.util;

import java.io.OutputStream;


public class DebugOutputStream extends OutputStream {
    private DebugFrame df;
    private boolean open;

    public DebugOutputStream(String title) {
        super();
        df = new DebugFrame(false);
        df.setTitle(title);
        open = true;
    }


    public void write(byte[] b) {
        if (open) df.append( new String(b) );
    }

    public void write(byte[] b, int off, int len) {
        if (open) df.append( new String(b, off, len) );
    }

    public void write(int b) {
        if (open) df.append( Character.toString((char)b) );
    }

    public void close() {
        open = false;
        df.dispose();
    }

}
