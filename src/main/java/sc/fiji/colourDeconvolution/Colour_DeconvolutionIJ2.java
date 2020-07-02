package sc.fiji.colourDeconvolution;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, headless = true, menuPath = "Histo>Colour Deconvolution")
public class Colour_DeconvolutionIJ2 implements Command {

    @Parameter(type = ItemIO.INPUT)
    private final Double R1 = 0.66645944;
    @Parameter(type = ItemIO.INPUT)
    private final Double G1 = 0.6332006;
    @Parameter(type = ItemIO.INPUT)
    private final Double B1 = 0.39355922;

    @Parameter(type = ItemIO.INPUT)
    private final Double R2 = 0.25378;
    @Parameter(type = ItemIO.INPUT)
    private final Double G2 = 0.737415;
    @Parameter(type = ItemIO.INPUT)
    private final Double B2 = 0.6259511;

    @Parameter(type = ItemIO.INPUT)
    private Dataset dataset;

    @Parameter(type = ItemIO.INPUT)
    private LogService log;

    @Parameter(type = ItemIO.OUTPUT)
    private ImgPlus<UnsignedByteType> deconvolutedImage1;
    @Parameter(type = ItemIO.OUTPUT)
    private ImgPlus<UnsignedByteType> deconvolutedImage2;
    @Parameter(type = ItemIO.OUTPUT)
    private ImgPlus<UnsignedByteType> deconvolutedImage3;

//    public static void main(String[] args) {
//        // Launch ImageJ as usual.
//        final ImageJ ij = new ImageJ();
//
//        ij.launch(args);
//    }

    /**
     * Produces an output with the well-known "Hello, World!" message. The
     * {@code run()} method of every {@link Command} is the entry point for
     * ImageJ: this is what will be called when the user clicks the menu entry,
     * after the inputs are populated.
     */
    @Override
    public void run() {
        StainMatrixIJ2 sm = new StainMatrixIJ2();
        sm.init("Our stain", R1, G1, B1, R2, G2, B2, 0, 0, 0);
        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType>[] imageStacks = sm.compute((ImgPlus<UnsignedByteType>) dataset.getImgPlus());
        deconvolutedImage1 = imageStacks[0];
        deconvolutedImage2 = imageStacks[1];
        deconvolutedImage3 = imageStacks[2];
    }
}
