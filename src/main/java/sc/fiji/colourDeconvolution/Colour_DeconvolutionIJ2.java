package sc.fiji.colourDeconvolution;

import static org.scijava.ItemIO.INPUT;
import static org.scijava.ItemIO.OUTPUT;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, headless = true, menuPath = "Histo>Colour Deconvolution", label = "Colour Deconvolution",
        description = "This plugin assumes an RGB image as it's input and does a 2-colour deconvolution on it.")
public class Colour_DeconvolutionIJ2 implements Command {

    @Parameter(type = INPUT, label = "Colour 1 Red mean",
            description = "The mean value for Red pixels in colour 1, should be between 0 and 1", max = "1", min = "0")
    private Double R1 = 0.66645944;
    @Parameter(type = INPUT, label = "Colour 1 Green mean",
            description = "The mean value for Green pixels in colour 1, should be between 0 and 1", max = "1", min = "0")
    private Double G1 = 0.6332006;
    @Parameter(type = INPUT, label = "Colour 1 Blue mean",
            description = "The mean value for Blue pixels in colour 1, should be between 0 and 1", max = "1", min = "0")
    private Double B1 = 0.39355922;

    @Parameter(type = INPUT, label = "Colour 2 Red mean",
            description = "The mean value for Red pixels in colour 2, should be between 0 and 1", max = "1", min = "0")
    private Double R2 = 0.25378;
    @Parameter(type = INPUT, label = "Colour 2 Green mean",
            description = "The mean value for Green pixels in colour 2, should be between 0 and 1", max = "1", min = "0")
    private Double G2 = 0.737415;
    @Parameter(type = INPUT, label = "Colour 2 Blue mean",
            description = "The mean value for Blue pixels in colour 2, should be between 0 and 1", max = "1", min = "0")
    private Double B2 = 0.6259511;

    @Parameter(type = INPUT, label = "Image to colour deconvolve",
            description = "The image that you would like to apply colour deconvolution on. Should be an RGB image!")
    private Dataset dataset;

    @Parameter(type = OUTPUT, label = "Colour 1 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage1;
    @Parameter(type = OUTPUT, label = "Colour 2 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage2;
    @Parameter(type = OUTPUT, label = "Remainder",
            description = "Remainder after the other 2 colours have been subtracted. Should be close to empty")
    private ImgPlus<UnsignedByteType> deconvolutedImage3;

    public Colour_DeconvolutionIJ2() {
    }

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
