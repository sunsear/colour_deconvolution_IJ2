package sc.fiji.colourDeconvolution;

import static org.scijava.ItemIO.INPUT;
import static org.scijava.ItemIO.OUTPUT;
import static sc.fiji.colourDeconvolution.StainParameters.*;
import static sc.fiji.colourDeconvolution.StainParameters.Constants.*;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, headless = true, menuPath = "Image>Color>Color Deconvolution Select", label = "Color Deconvolution Select",
        description = "This plugin assumes an RGB image as it's input and does a 3-color deconvolution on it with the values selected from the pulldown.")
public class ColorDeconvolutionIJ2Select implements Command {

    @Parameter(type = INPUT, label = " select deconvolution type",
               description = "values used for the deconvolution",
               choices = {H_E_DESCR, H_E2_DESCR, FLG_DESCR, GIEMSA_DESCR, FR_FB_DAB_DESCR, MG_DAB_DESCR, H_E_DAB_DESCR,
                       H_AEC_DESCR, A_Z_DESCR, MAS_TRI_DESCR, ALC_B_H_DESCR, H_PAS_DESCR, RGB_DESCR, CMY_DESCR },
               style = "listBox",
               initializer = "default")
    private String selection = H_E_DESCR;

    @Parameter(type = INPUT, label = "Image to color deconvolve",
            description = "The image that you would like to apply colour deconvolution on. Should be an RGB image!")
    private Dataset dataset;

    @Parameter(type = OUTPUT, label = "Colour 1 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage1;
    @Parameter(type = OUTPUT, label = "Colour 2 deconvolved Image")
    private ImgPlus<UnsignedByteType> deconvolutedImage2;
    @Parameter(type = OUTPUT, label = "Remainder",
            description = "Remainder after the other 2 colours have been subtracted. Should be close to empty")
    private ImgPlus<UnsignedByteType> deconvolutedImage3;

    public ColorDeconvolutionIJ2Select() {
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
        StainParameters values = fromString(selection);
        sm.init("Our stain", values.rgb1()[0], values.rgb1()[1], values.rgb1()[2], values.rgb2()[0], values.rgb2()[1], values.rgb2()[2], values.rgb3()[0], values.rgb3()[1], values.rgb3()[2]);
        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType>[] imageStacks = sm.compute((ImgPlus<UnsignedByteType>) dataset.getImgPlus());
        deconvolutedImage1 = imageStacks[0];
        deconvolutedImage2 = imageStacks[1];
        deconvolutedImage3 = imageStacks[2];
    }

    public static StainParameters fromString(String text) {
        for (StainParameters stainParameter : values()) {
            if (stainParameter.description().equalsIgnoreCase(text)) {
                return stainParameter;
            }
        }
        return H_E;
    }
    public static String getAllDescriptions() {
        StringBuilder values = new StringBuilder("{");
        for (StainParameters stainParameter : values()) {
            values.append(stainParameter.description());
            values.append(", ");
        }
        values.append("}");
        return values.toString();
    }

}
