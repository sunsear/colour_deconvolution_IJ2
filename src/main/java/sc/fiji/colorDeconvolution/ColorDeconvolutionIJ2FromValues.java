package sc.fiji.colorDeconvolution;

import static org.scijava.ItemIO.INPUT;
import static org.scijava.ItemIO.OUTPUT;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, headless = true, label = "Color Deconvolution",
        description = "This plugin assumes an RGB image as it's input and does a color deconvolution on it.")
public class ColorDeconvolutionIJ2FromValues implements Command {

    @Parameter(type = INPUT, label = "Color 1 Red mean",
            description = "The mean value for Red pixels in color 1, should be between 0 and 1", max = "1", min = "0")
    private Double r1 = 0.66645944;
    @Parameter(type = INPUT, label = "Color 1 Green mean",
            description = "The mean value for Green pixels in color 1, should be between 0 and 1", max = "1", min = "0")
    private Double g1 = 0.6332006;
    @Parameter(type = INPUT, label = "Color 1 Blue mean",
            description = "The mean value for Blue pixels in color 1, should be between 0 and 1", max = "1", min = "0")
    private Double b1 = 0.39355922;

    @Parameter(type = INPUT, label = "Color 2 Red mean",
            description = "The mean value for Red pixels in color 2, should be between 0 and 1", max = "1", min = "0")
    private Double r2 = 0.25378;
    @Parameter(type = INPUT, label = "Color 2 Green mean",
            description = "The mean value for Green pixels in color 2, should be between 0 and 1", max = "1", min = "0")
    private Double g2 = 0.737415;
    @Parameter(type = INPUT, label = "Color 2 Blue mean",
            description = "The mean value for Blue pixels in color 2, should be between 0 and 1", max = "1", min = "0")
    private Double b2 = 0.6259511;

    @Parameter(type = INPUT, label = "Color 3 Red mean",
            description = "The mean value for Red pixels in color 3, should be between 0 and 1", max = "1", min = "0")
    private Double r3 = 0.0;
    @Parameter(type = INPUT, label = "Color 3 Green mean",
            description = "The mean value for Green pixels in color 3, should be between 0 and 1", max = "1", min = "0")
    private Double g3 = 0.0;
    @Parameter(type = INPUT, label = "Color 3 Blue mean",
            description = "The mean value for Blue pixels in color 3, should be between 0 and 1", max = "1", min = "0")
    private Double b3 = 0.0;

    @Parameter(type = INPUT, label = "Image to color deconvolve",
            description = "The image that you would like to apply color deconvolution on. Should be an RGB image!")
    private Dataset dataset;

    @Parameter(type = OUTPUT, label = "Color 1 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage1;
    @Parameter(type = OUTPUT, label = "Color 2 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage2;
    @Parameter(type = OUTPUT, label = "Remainder or color 3",
            description = "Color 3 deconvolved image or remainder after the other 2 colors have been subtracted. Should be close to empty if color 3 values are left to 0")
    private ImgPlus<UnsignedByteType> deconvolutedImage3;

    public ColorDeconvolutionIJ2FromValues() {
    }

    /**
     * Perform the color deconvolution with user provided values.
     */
    @Override
    public void run() {
        StainMatrixIJ2 sm = new StainMatrixIJ2();
        sm.init("User defined stain", r1, g1, b1, r2, g2, b2, r3, g3, b3);

        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType>[] imageStacks = sm.compute((ImgPlus<UnsignedByteType>) dataset.getImgPlus());
        deconvolutedImage1 = imageStacks[0];
        deconvolutedImage2 = imageStacks[1];
        deconvolutedImage3 = imageStacks[2];
    }
}
