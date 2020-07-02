import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import sc.fiji.colourDeconvolution.StainMatrixIJ2;

public class StainMatrixIJ2Test {
    @Test
    public void testColourDeconvolutionFor2Colours() throws IOException {
        ImageJ ij = new ImageJ();
        DatasetIOService datasetIOService = ij.scifio().datasetIO();
        Dataset dataset = datasetIOService.open("src/test/resources/2ColourImage.tif");
        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType> imagePlus = (ImgPlus<UnsignedByteType>) dataset.getImgPlus();
        StainMatrixIJ2 stainMatrix = new StainMatrixIJ2();
        stainMatrix.init("test stain", 0.66645944, 0.6332006, 0.39355922, 0.25378, 0.737415, 0.6259511, 0, 0, 0);
        ImgPlus<UnsignedByteType>[] computed = stainMatrix.compute(false, true, imagePlus);
        assertEquals("We expect there to be 3 result images", 3, computed.length);
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[0]), "target/ij2-outputDeconvoluted1.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[1]), "target/ij2-outputDeconvoluted2.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[2]), "target/ij2-outputDeconvoluted3.tif");
        ImagePlus expected1 = new ImagePlus("src/test/resources/expectedDeconvoluted1.tif");
        new ImageCalculator().run("Difference", expected1, new ImagePlus("target/ij2-outputDeconvoluted1.tif"));
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(15, 15)[0]);
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(1500, 150)[0]);
        assertEquals("Difference at first sample point should be 0", 0, expected1.getPixel(150, 1500)[0]);
    }
}
