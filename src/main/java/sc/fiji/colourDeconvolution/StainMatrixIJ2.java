package sc.fiji.colourDeconvolution;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class StainMatrixIJ2 extends StainMatrixBase {
    /**
     * Compute the Deconvolution images and return a Stack array of three 8-bit
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
        double log255 = Math.log(255.0);
//        // Translate ------------------
        int imageSize = width * height;

        RandomAccess<UnsignedByteType> randomAccess = img.randomAccess();

        byte[][] newpixels = new byte[3][];
        newpixels[0] = new byte[imageSize];
        newpixels[1] = new byte[imageSize];
        newpixels[2] = new byte[imageSize];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int R = randomAccess.get().getInteger();
                randomAccess.fwd(2);
                int G = randomAccess.get().getInteger();
                randomAccess.fwd(2);
                int B = randomAccess.get().getInteger();
                randomAccess.move(-2, 2);

                double Rlog = -((255.0 * Math.log(((double) R + 1) / 255.0)) / log255);
                double Glog = -((255.0 * Math.log(((double) G + 1) / 255.0)) / log255);
                double Blog = -((255.0 * Math.log(((double) B + 1) / 255.0)) / log255);

                for (int channel = 0; channel < 3; channel++) {
                    // Rescale to match original paper values
                    double Rscaled = Rlog * q[channel * 3];
                    double Gscaled = Glog * q[channel * 3 + 1];
                    double Bscaled = Blog * q[channel * 3 + 2];
                    double output = Math.exp(-((Rscaled + Gscaled + Bscaled) - 255.0) * log255 / 255.0);
                    if (output > 255) output = 255;
                    newpixels[channel][width * y + x] = (byte) (0xff & (int) (Math.floor(output + .5)));
                }
                randomAccess.fwd(0);
            }
            randomAccess.move(-width, 0);
            randomAccess.fwd(1);
        }

        @SuppressWarnings("unchecked")
        ImgPlus<UnsignedByteType>[] outputImages = new ImgPlus[3];
        outputImages[0] = new ImgPlus<>(ArrayImgs.unsignedBytes(newpixels[0], width, height));
        outputImages[1] = new ImgPlus<>(ArrayImgs.unsignedBytes(newpixels[1], width, height));
        outputImages[2] = new ImgPlus<>(ArrayImgs.unsignedBytes(newpixels[2], width, height));
        initializeColorTables(outputImages);
        return outputImages;
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
