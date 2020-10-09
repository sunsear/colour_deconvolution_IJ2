package sc.fiji.colorDeconvolution;

import static java.lang.Math.exp;
import static java.lang.Math.round;
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
    private static Double r1 = 0.5737953;
    private static Double g1 = 0.6956962;
    private static Double b1 = 0.4322119;

    private static Double r2 = 0.24696793;
    private static Double g2 = 0.84164965;
    private static Double b2 = 0.48024228;

    public static void main(String[] args) throws IOException {
        final ImageJ ij = new ImageJ();
        final ImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
        final long[] dimensions = new long[]{2000, 2200, 3};

        final Img<UnsignedByteType> img = factory.create(dimensions);
        Cursor<UnsignedByteType> unsignedByteTypeCursor = img.localizingCursor();
        int[] position = new int[3];
        unsignedByteTypeCursor.forEachRemaining((x) -> {
            unsignedByteTypeCursor.localize(position);

            if (position[1] < 2000) {
                double factorX = position[0] / 1000D;
                double factorY = position[1] / 1000D;
                x.set(new int[]{convertToIntensity(r1 * factorX + r2 * factorY), convertToIntensity(g1 * factorX + g2 * factorY), convertToIntensity(b1 * factorX + b2 * factorY)}[position[2]]);
            } else if (position[1] >= 2100) {
                double factorX = position[0] / 500D;
                x.set(new int[]{convertToIntensity(r2 * factorX), convertToIntensity(g2 * factorX), convertToIntensity(b2 * factorX)}[position[2]]);
            } else {
                double factorX = position[0] / 500D;
                x.set(new int[]{convertToIntensity(r1 * factorX), convertToIntensity(g1 * factorX), convertToIntensity(b1 * factorX)}[position[2]]);
            }
        });
        DatasetIOService datasetIOService = ij.scifio().datasetIO();
        Dataset dataset = ij.dataset().create(img);
        dataset.axis(2).setType(Axes.CHANNEL);
        dataset.initializeColorTables(1);
        dataset.setColorTable(ColorTables.RGB332, 0);
        datasetIOService.save(dataset, "generated.tif");
    }

    private static int convertToIntensity(double absorption) {
        int intensity = (int) round((255 + 1) * exp(-absorption) - 1);
        if (intensity < 0) {
            System.out.println("Value < 0:" + intensity);
            return 0;
        } else if (intensity > 255) {
            System.out.println("Value >255:" + intensity);
            return 255;
        }
        return intensity;
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
