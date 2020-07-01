package sc.fiji.colourDeconvolution;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

public class StainMatrixIJ2 extends StainMatrix {
    private final ImageJ ij = new ImageJ();

    /**
     * Compute the Deconvolution images and return a Stack array of three 8-bit
     * images. If the specimen is stained with a 2 colour scheme (such as H &amp;
     * E) the 3rd image represents the complimentary of the first two colours
     * (i.e. green).
     *
     * @param doIshow    :    Show or not the matrix in a popup
     * @param hideLegend : Hide or not the legend in a popup
     * @param imp        : The ImagePlus that will be deconvolved. RGB only.
     * @return a Stack array of three 8-bit images
     */
    public ImgPlus[] compute(boolean doIshow, boolean hideLegend, ImgPlus imp) {
        double[] q = initComputation(doIshow, hideLegend);

        Img img = imp.getImg();

        int width = (int) img.dimension(0);
        int height = (int) img.dimension(1);
        ImgPlus[] outputImages = initialize3OutputColourStacks(width, height);
        double log255 = Math.log(255.0);
//        // Translate ------------------
        int imageSize = width * height;

        RandomAccess randomAccess = img.randomAccess();

        byte[][] newpixels = new byte[3][];
        newpixels[0] = new byte[imageSize];
        newpixels[1] = new byte[imageSize];
        newpixels[2] = new byte[imageSize];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                UnsignedByteType R = (UnsignedByteType) randomAccess.get();
                randomAccess.fwd(2);
                UnsignedByteType G = (UnsignedByteType) randomAccess.get();
                randomAccess.fwd(2);
                UnsignedByteType B = (UnsignedByteType) randomAccess.get();
                randomAccess.move(-2, 2);

//                double Rlog = -((255.0 * Math.log(((double) R.getByte() + 1) / 255.0)) / log255);
//                double Glog = -((255.0 * Math.log(((double) G.getByte() + 1) / 255.0)) / log255);
//                double Blog = -((255.0 * Math.log(((double) B.getByte() + 1) / 255.0)) / log255);
//
//                for (int i = 0; i < 3; i++) {
//                    // Rescale to match original paper values
//                    double Rscaled = Rlog * q[i * 3];
//                    double Gscaled = Glog * q[i * 3 + 1];
//                    double Bscaled = Blog * q[i * 3 + 2];
//                    double output = Math.exp(-((Rscaled + Gscaled + Bscaled) - 255.0) * log255 / 255.0);
//                    if (output > 255) output = 255;
//                    newpixels[i][j] = (byte) (0xff & (int) (Math.floor(output + .5)));
//                }

                newpixels[0][width * j + i] = R.getByte();
                newpixels[1][width * j + i] = G.getByte();
                newpixels[2][width * j + i] = B.getByte();
                randomAccess.fwd(1);
            }
            randomAccess.move(-height, 1);
            randomAccess.fwd(0);
        }
        outputImages[0] = new ImgPlus(ArrayImgs.unsignedBytes((byte[]) newpixels[0], width, height));
        outputImages[1] = new ImgPlus(ArrayImgs.unsignedBytes((byte[]) newpixels[1], width, height));
        outputImages[2] = new ImgPlus(ArrayImgs.unsignedBytes((byte[]) newpixels[2], width, height));
        // Add new values to output images
//        outputImages[0].addSlice(label, newpixels[0]);
//        outputImages[1].addSlice(label, newpixels[1]);
//        outputImages[2].addSlice(label, newpixels[2]);
//
        return outputImages;
    }

    private ImgPlus[] initialize3OutputColourStacks(long width, long height) {
        byte[] rLUT = new byte[256];
        byte[] gLUT = new byte[256];
        byte[] bLUT = new byte[256];
        ImgPlus[] outputImages = new ImgPlus[3];

        DoubleType outType = new DoubleType();
        Dimensions dims = new FinalDimensions(width, height);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 256; j++) { //LUT[1]
                rLUT[255 - j] = (byte) (255.0 - (double) j * cosx[i]);
                gLUT[255 - j] = (byte) (255.0 - (double) j * cosy[i]);
                bLUT[255 - j] = (byte) (255.0 - (double) j * cosz[i]);
            }
            outputImages[i] = new ImgPlus(ij.op().create().img(dims, outType));
            outputImages[i].initializeColorTables(3);
            outputImages[i].setColorTable(new ColorTable8(rLUT), 0);
            outputImages[i].setColorTable(new ColorTable8(gLUT), 1);
            outputImages[i].setColorTable(new ColorTable8(bLUT), 2);
        }
        return outputImages;
    }
}
