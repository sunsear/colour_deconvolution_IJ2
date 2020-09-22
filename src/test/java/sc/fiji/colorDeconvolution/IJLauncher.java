package sc.fiji.colorDeconvolution;

import net.imagej.ImageJ;

public class IJLauncher {
    public static void main(String[] args) {
        // Launch ImageJ as usual.
        final ImageJ ij = new ImageJ();

        ij.launch(args);
    }
}
