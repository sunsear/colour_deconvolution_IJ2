package sc.fiji.colourDeconvolution;

import net.imagej.ImgPlus;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.ColorChannelOrder;
import net.imglib2.converter.Converters;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.NumericComposite;

public class StainMatrixIJ2 extends StainMatrixBase {

    public static final double LOG_255 = Math.log(255.0);

    /**
     * Compute the Deconvolution images and return an ImgPlus array of three 8-bit
     * images. If the specimen is stained with a 2 colour scheme (such as H &amp;
     * E) the 3rd image represents the complimentary of the first two colours
     * (i.e. green).
     *
     * @param imp : The ImagePlus that will be deconvolved. RGB only.
     * @return a Stack array of three 8-bit images
     */
    public ImgPlus<UnsignedByteType>[] compute(ImgPlus<UnsignedByteType> imp) {
        double[] q = initComputation(true);

        Img<UnsignedByteType> img = imp.getImg();

        int width = (int) img.dimension(0);
        int height = (int) img.dimension(1);

        RandomAccessibleInterval<NumericComposite<UnsignedByteType>> collapseNumeric = Views.collapseNumeric(img);
        RandomAccessibleInterval<ARGBType> mergeARGB = Converters.mergeARGB(img, ColorChannelOrder.RGB);

        FinalDimensions dimensions = new FinalDimensions(width, height);
        Img<UnsignedByteType> outputImg1 = img.factory().create(dimensions);
        Img<UnsignedByteType> outputImg2 = img.factory().create(dimensions);
        Img<UnsignedByteType> outputImg3 = img.factory().create(dimensions);

        LoopBuilder.setImages(mergeARGB, outputImg1, outputImg2, outputImg3).forEachPixel(
                (input, out1, out2, out3) -> {
                    int rgba = input.get();

                    double Rlog = logify(ARGBType.red(rgba));
                    double Glog = logify(ARGBType.green(rgba));
                    double Blog = logify(ARGBType.blue(rgba));

                    // Rescale to match original paper values
                    double output = Math.exp(-((Rlog * q[0] + Glog * q[1] + Blog * q[2]) - 255.0) * LOG_255 / 255.0);
                    out1.set(output > 255 ? 255 : (int) Math.round(output));

                    output = Math.exp(-((Rlog * q[3] + Glog * q[4] + Blog * q[5]) - 255.0) * LOG_255 / 255.0);
                    out2.set(output > 255 ? 255 : (int) Math.round(output));

                    output = Math.exp(-((Rlog * q[6] + Glog * q[7] + Blog * q[8]) - 255.0) * LOG_255 / 255.0);
                    out3.set(output > 255 ? 255 : (int) Math.round(output));
                }
        );

        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType>[] outputImages = new ImgPlus[3];
        outputImages[0] = new ImgPlus<>(outputImg1);
        outputImages[1] = new ImgPlus<>(outputImg2);
        outputImages[2] = new ImgPlus<>(outputImg3);
        initializeColorTables(outputImages);
        return outputImages;
    }

    /**
     * This function converts all values between 0 and 255 according to a logarithmical curve. See testLogify to
     * see the distribution of the numbers
     *
     * @param colourValue the unsigned byte colourvalue of a specific pixel, so between 0 and 255
     * @return logarothmically redistributed value
     */
    double logify(double colourValue) {
        return -((255.0 * Math.log((colourValue + 1) / 255.0)) / LOG_255);
    }

    private ImgPlus<UnsignedByteType>[] initializeColorTables(ImgPlus<UnsignedByteType>[] outputImages) {
        byte[] rLUT = new byte[256];
        byte[] gLUT = new byte[256];
        byte[] bLUT = new byte[256];

        for (int channel = 0; channel < 3; channel++) {
            for (int j = 0; j < 256; j++) { //LUT[1]
                rLUT[255 - j] = (byte) (255.0 - (double) j * cosx[channel]);
                gLUT[255 - j] = (byte) (255.0 - (double) j * cosy[channel]);
                bLUT[255 - j] = (byte) (255.0 - (double) j * cosz[channel]);
            }
            outputImages[channel].initializeColorTables(3);
            outputImages[channel].setColorTable(new ColorTable8(rLUT), 0);
            outputImages[channel].setColorTable(new ColorTable8(gLUT), 1);
            outputImages[channel].setColorTable(new ColorTable8(bLUT), 2);
        }
        return outputImages;
    }
}
