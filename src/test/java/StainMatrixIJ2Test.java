import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;
import sc.fiji.colourDeconvolution.StainMatrixIJ2;

public class StainMatrixIJ2Test {
    @Test
    public void testColourDeconvolutionFor2Colours() throws IOException {
        ImageJ ij = new ImageJ();
        DatasetIOService datasetIOService = ij.scifio().datasetIO();
        Dataset dataset = datasetIOService.open("src/test/resources/2ColourImage.tif");
        ImgPlus<? extends RealType<?>> imagePlus = dataset.getImgPlus();
        StainMatrixIJ2 stainMatrix = new StainMatrixIJ2();
        stainMatrix.init("test stain", 0.66645944, 0.6332006, 0.39355922, 0.25378, 0.737415, 0.6259511, 0, 0, 0);
        ImgPlus[] computed = stainMatrix.compute(false, false, imagePlus);
        assertEquals("We expect there to be 3 result images", 3, computed.length);
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[0]), "src/test/resources/outputConvoluted1.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[1]), "src/test/resources/outputConvoluted2.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[2]), "src/test/resources/outputConvoluted3.tif");
//        Dataset expected1 = datasetIOService.open("src/test/resources/expectedDeconvoluted1.tif");
//        new ImageCalculator().run("Difference", expected1.getImgPlus(), computed[0]);
    }
}
