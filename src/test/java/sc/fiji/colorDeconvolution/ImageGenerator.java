package sc.fiji.colorDeconvolution;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.display.ColorTables;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class ImageGenerator {
    public static void main(String[] args) throws IOException {
        final ImageJ ij = new ImageJ();
        final ImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
        final long[] dimensions = new long[]{256, 400, 3};

        final Img<UnsignedByteType> img = factory.create(dimensions);
        Cursor<UnsignedByteType> unsignedByteTypeCursor = img.localizingCursor();
        int[] position = new int[3];
        unsignedByteTypeCursor.forEachRemaining((x) -> {
            unsignedByteTypeCursor.localize(position);
            if (position[1] < 100) {
                x.set(position[0]);
            }
            if (position[1] > 299) {
                x.set(255 - position[0]);
            }
        });
        DatasetIOService datasetIOService = ij.scifio().datasetIO();
        Dataset dataset = ij.dataset().create(img);
        dataset.axis(2).setType(Axes.CHANNEL);
        dataset.initializeColorTables(1);
        dataset.setColorTable(ColorTables.RGB332, 0);
        datasetIOService.save(dataset, "testImage.tif");
    }

    /**
     * The original code did calculations, which were way slower than a simple lookup. This method ensures the logify
     * lookup still does what the original calculation intended.
     */
    public void testAbsorptionLookupPerformsAsComputation() {
        for (int i = 0; i < 256; i++) {
            assertEquals(-((255.0 * Math.log((i + 1) / 255.0)) / Math.log(255.0)), StainMatrixIJ2.convertIntensityToAbsorption(i), 0.00001);
        }
    }
}
