package sc.fiji.colourDeconvolution;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

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

        Instant start = Instant.now();
        ImgPlus<UnsignedByteType>[] computed = stainMatrix.compute(imagePlus);
        System.out.println("Compute took " + Duration.between(start, Instant.now()));

        assertEquals("We expect there to be 3 result images", 3, computed.length);
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[0]), "target/ij2-outputDeconvoluted1.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[1]), "target/ij2-outputDeconvoluted2.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[2]), "target/ij2-outputDeconvoluted3.tif");
        ImagePlus expected1 = new ImagePlus("src/test/resources/expectedDeconvoluted1.tif");
        new ImageCalculator().run("Difference", expected1, new ImagePlus("target/ij2-outputDeconvoluted1.tif"));
        assertArrayEquals("Difference between 2 images should be 0 at every point.",
                new byte[6980 * 1646], (byte[]) expected1.getImageStack().getPixels(1));
    }

    @Test
    public void testColourDeconvolutionOnSmallImage() throws IOException {
        ImageJ ij = new ImageJ();
        DatasetIOService datasetIOService = ij.scifio().datasetIO();
        Dataset dataset = datasetIOService.open("src/test/resources/small2ColourImage.tif");
        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType> imagePlus = (ImgPlus<UnsignedByteType>) dataset.getImgPlus();
        StainMatrixIJ2 stainMatrix = new StainMatrixIJ2();
        stainMatrix.init("test stain", 0.66645944, 0.6332006, 0.39355922, 0.25378, 0.737415, 0.6259511, 0, 0, 0);
        ImgPlus<UnsignedByteType>[] computed = stainMatrix.compute(imagePlus);

        assertEquals("We expect there to be 3 result images", 3, computed.length);

        ImagePlus expected1 = new ImagePlus("src/test/resources/expectedSmallDeconvoluted1.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[0]), "target/ij2-outputSmallDeconvoluted1.tif");
        new ImageCalculator().run("Difference", expected1, new ImagePlus("target/ij2-outputSmallDeconvoluted1.tif"));
        assertArrayEquals(new byte[30 * 30], (byte[]) expected1.getImageStack().getPixels(1));

        ImagePlus expected2 = new ImagePlus("src/test/resources/expectedSmallDeconvoluted2.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[1]), "target/ij2-outputSmallDeconvoluted2.tif");
        new ImageCalculator().run("Difference", expected2, new ImagePlus("target/ij2-outputSmallDeconvoluted2.tif"));
        assertArrayEquals(new byte[30 * 30], (byte[]) expected2.getImageStack().getPixels(1));

        ImagePlus expected3 = new ImagePlus("src/test/resources/expectedSmallDeconvoluted3.tif");
        datasetIOService.save(new DefaultDataset(datasetIOService.context(), computed[2]), "target/ij2-outputSmallDeconvoluted3.tif");
        new ImageCalculator().run("Difference", expected3, new ImagePlus("target/ij2-outputSmallDeconvoluted3.tif"));
        assertArrayEquals(new byte[30 * 30], (byte[]) expected3.getImageStack().getPixels(1));
    }

    /**
     * The original code did calculations, which were way slower than a simple lookup. This method ensures the logify
     * lookup still does what the original calculation intended.
     */
    @Test
    public void testLogifyLookupPerformsAsComputation() {
        for (int i = 0; i < 256; i++) {
            assertEquals(-((255.0 * Math.log((i + 1) / 255.0)) / StainMatrixIJ2.LOG_255), StainMatrixIJ2.logify(i), 0.00001);
        }
    }
}
