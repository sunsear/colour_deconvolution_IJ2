package sc.fiji.colourDeconvolution;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.ByteType;

@Plugin(type = Command.class, headless = true, menuPath = "Histo>Colour Deconvolution")
public class Colour_DeconvolutionIJ2 implements Command {

    @Parameter(type = ItemIO.INPUT)
    private Double R1 = 0.66645944, G1 = 0.6332006, B1 = 0.39355922;

    @Parameter(type = ItemIO.INPUT)
    private Double R2 = 0.25378, G2 = 0.737415, B2 = 0.6259511;

    @Parameter(type = ItemIO.INPUT)
    private Dataset dataset;

    @Parameter(type = ItemIO.INPUT)
    private LogService log;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<ByteType> deconvolutedImage1;
    @Parameter(type = ItemIO.OUTPUT)
    private Img<ByteType> deconvolutedImage2;
    @Parameter(type = ItemIO.OUTPUT)
    private Img<ByteType> deconvolutedImage3;

    public static void main(String[] args) throws Exception {
        // Launch ImageJ as usual.
        final ImageJ ij = new ImageJ();

        ij.launch(args);
    }

    /**
     * Produces an output with the well-known "Hello, World!" message. The
     * {@code run()} method of every {@link Command} is the entry point for
     * ImageJ: this is what will be called when the user clicks the menu entry,
     * after the inputs are populated.
     */
    @Override
    public void run() {
        StainMatrix sm = new StainMatrix();
        sm.init("Our stain", R1, G1, B1, R2, G2, B2, 0, 0, 0);
        Img<ByteType> img = (Img<ByteType>) dataset.getImgPlus().getImg();
        ImageStack[] imageStacks = sm.compute(false, true, ImageJFunctions.wrap(img, dataset.getName()));
        ImagePlus imp = new ImagePlus("Image " + 0, imageStacks[0]);
        imp.show();
        deconvolutedImage1 = ImageJFunctions.wrap(imp);
        deconvolutedImage2 = ImageJFunctions.wrap(new ImagePlus("Image " + 1, imageStacks[1]));
        deconvolutedImage3 = ImageJFunctions.wrap(new ImagePlus("Image " + 2, imageStacks[2]));
    }
}
