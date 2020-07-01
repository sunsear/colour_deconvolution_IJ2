import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.ImageCalculator;
import sc.fiji.colourDeconvolution.StainMatrix;

public class StainMatrixTest {
    @Test
    public void testColourDeconvolutionFor2Colours() {
        ImagePlus imagePlus = new ImagePlus("src/test/resources/2ColourImage.tif");
        StainMatrix stainMatrix = new StainMatrix();
        stainMatrix.init("test stain", 0.66645944, 0.6332006, 0.39355922, 0.25378, 0.737415, 0.6259511, 0, 0, 0);
        ImageStack[] computed = stainMatrix.compute(false, false, imagePlus);
        assertEquals("We expect there to be 3 result images", 3, computed.length);
        assertEquals("We expect each stack to have only 1 slice", 1, computed[0].getSize());
        ImagePlus expected1 = new ImagePlus("src/test/resources/expectedDeconvoluted1.tif");
        new ImageCalculator().run("Difference", expected1, new ImagePlus("computed1", computed[0]));
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(15, 15)[0]);
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(1500, 150)[0]);
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(150, 1500)[0]);
    }
}
