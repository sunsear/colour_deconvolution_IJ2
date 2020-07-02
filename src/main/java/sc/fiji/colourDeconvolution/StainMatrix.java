package sc.fiji.colourDeconvolution;

import java.awt.*;
import java.awt.image.IndexColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
 * This abstract class contains all the library methods to perform image deconvolution based on custom vector.
 * <code>
 * boolean showMatrix=false;//To not display the Matrix used to do the deconvolution in a popup at the end of the process
 * boolean hideLegend=true; //To not display the deconvolution legend in a popup at the end of the process
 * mt = new StainMatrix();  // Create a new Matrix_Custom
 * //Populate the Transformation Matrix
 * mt.getMODx()[0]=0.650;
 * mt.getMODy()[0]=0.704;
 * mt.getMODz()[0]=0.286;
 * mt.getMODx()[1]=0.072;
 * mt.getMODy()[1]=0.990;
 * mt.getMODz()[1]=0.105;
 * mt.getMODx()[2]=0.268;
 * mt.getMODy()[2]=0.570;
 * mt.getMODz()[2]=0.776; // Create a new MatrixTransformation corresponding to your stain.
 * //Compute the Deconvolution images and return a Stack array of three 8-bit images.
 * ImageStack[] stacks = mt.compute(showMatrix, hideLegend, imp);
 * //Then if you want to display them:
 * new ImagePlus(title+"-(Colour_1)",stack[0]).show();
 * new ImagePlus(title+"-(Colour_2)",stack[1]).show();
 * new ImagePlus(title+"-(Colour_3)",stack[2]).show();
 * </code>
 * If you want to perform it from a ROIs, you need to have 3 ROIs, one for each Colour:
 * boolean showMatrix=false; //To not display the Matrix used to do the deconvolution in a popup at the end of the process
 * boolean hideLegend=true; //To not display the deconvolution legend in a popup at the end of the process
 * // Create a new Custom Matrix Transformation and give 3 ROIs, one for each Colour
 * mt = new Matrix_Custom(rois, imp.getProcessor);// Create a new Matrix_Custom and populate the Transformation Matrix from the ROIs
 * //Compute the Deconvolution images and return a Stack array of three 8-bit images.
 * ImageStack[] stacks = mt.compute(showMatrix, hideLegend, imp);
 * Then if you want to display them:
 * new ImagePlus(title+"-(Colour_1)",stack[0]).show();
 * new ImagePlus(title+"-(Colour_2)",stack[1]).show();
 * new ImagePlus(title+"-(Colour_3)",stack[2]).show();
 *
 * @author Benjamin Pavie
 */

public class StainMatrix extends StainMatrixBase {

    public StainMatrix() {
        super();
    }

    /**
     * Compute the Deconvolution images and display them
     *
     * @param doIshow:    Show or not the matrix in a popup
     * @param hideLegend: Hide or not the legend in a popup
     * @param imp         : The ImagePlus that will be deconvolved. RGB only.
     */
    public void computeAndShow(boolean doIshow, boolean hideLegend, ImagePlus imp) {
        String title = imp.getTitle();
        ImageStack[] stack = compute(doIshow, hideLegend, imp);
        new ImagePlus(title + "-(Colour_1)", stack[0]).show();
        new ImagePlus(title + "-(Colour_2)", stack[1]).show();
        new ImagePlus(title + "-(Colour_3)", stack[2]).show();
    }

    /**
     * Compute the Deconvolution images and return a Stack array of three 8-bit
     * images. If the specimen is stained with a 2 colour scheme (such as H &amp;
     * E) the 3rd image represents the complimentary of the first two colours
     * (i.e. green).
     *
     * @param doIshow:    Show or not the matrix in a popup
     * @param hideLegend: Hide or not the legend in a popup
     * @param imp         : The ImagePlus that will be deconvolved. RGB only.
     * @return a Stack array of three 8-bit images
     */
    public ImageStack[] compute(boolean doIshow, boolean hideLegend, ImagePlus imp) {
        double[] q = initComputation(doIshow);

        if (!hideLegend) {
            showLegend(myStain);
        }
        if (doIshow) {
            showMatrix(myStain);
        }

        ImageStack stack = imp.getStack();

        ImageStack[] outputstack = initialize3OutputColourStacks(stack.getWidth(), stack.getHeight());

        double log255 = Math.log(255.0);
        // Translate ------------------
        int imageSize = stack.getWidth() * stack.getHeight();
        int modulo = imageSize / 60;
        for (int imageNum = 1; imageNum <= stack.getSize(); imageNum++) {
            int[] pixels = (int[]) stack.getPixels(imageNum);
            String label = stack.getSliceLabel(imageNum);
            byte[][] newpixels = new byte[3][];
            newpixels[0] = new byte[imageSize];
            newpixels[1] = new byte[imageSize];
            newpixels[2] = new byte[imageSize];

            for (int j = 0; j < imageSize; j++) {
                if (j % modulo == 0)
                    IJ.showProgress(j, imageSize);    //show progress bar, quicker than calling it every time.
                // Log transform the RGB data
                int R = (pixels[j] & 0xff0000) >> 16;
                int G = (pixels[j] & 0x00ff00) >> 8;
                int B = (pixels[j] & 0x0000ff);
//                if (j >  < 20 )
                double Rlog = -((255.0 * Math.log(((double) R + 1) / 255.0)) / log255);
                double Glog = -((255.0 * Math.log(((double) G + 1) / 255.0)) / log255);
                double Blog = -((255.0 * Math.log(((double) B + 1) / 255.0)) / log255);
                for (int i = 0; i < 3; i++) {
                    // Rescale to match original paper values
                    double Rscaled = Rlog * q[i * 3];
                    double Gscaled = Glog * q[i * 3 + 1];
                    double Bscaled = Blog * q[i * 3 + 2];
                    double output = Math.exp(-((Rscaled + Gscaled + Bscaled) - 255.0) * log255 / 255.0);
                    if (output > 255) output = 255;
                    newpixels[i][j] = (byte) (0xff & (int) (Math.floor(output + .5)));
                }
            }
            // Add new values to output images
            outputstack[0].addSlice(label, newpixels[0]);
            outputstack[1].addSlice(label, newpixels[1]);
            outputstack[2].addSlice(label, newpixels[2]);
        }
        IJ.showProgress(1);

        return outputstack;
    }

    private ImageStack[] initialize3OutputColourStacks(int width, int height) {
        byte[] rLUT = new byte[256];
        byte[] gLUT = new byte[256];
        byte[] bLUT = new byte[256];
        ImageStack[] outputstack = new ImageStack[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 256; j++) { //LUT[1]
                rLUT[255 - j] = (byte) (255.0 - (double) j * cosx[i]);
                gLUT[255 - j] = (byte) (255.0 - (double) j * cosy[i]);
                bLUT[255 - j] = (byte) (255.0 - (double) j * cosz[i]);
            }
            IndexColorModel cm = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);
            outputstack[i] = new ImageStack(width, height, cm);
        }
        return outputstack;
    }

    protected void showLegend(String myStain) {

        ImagePlus imp0 = NewImage.createRGBImage("Colour Deconvolution", 350, 65, 1, 0);
        ImageProcessor ip0 = imp0.getProcessor();
        ip0.setFont(new Font("Monospaced", Font.BOLD, 11));
        ip0.setAntialiasedText(true);
        ip0.setColor(Color.black);
        ip0.moveTo(10, 15);
        ip0.drawString("Colour deconvolution: " + myStain);
        ip0.setFont(new Font("Monospaced", Font.PLAIN, 10));

        for (int i = 0; i < 3; i++) {
            ip0.setRoi(10, 18 + i * 15, 14, 14);
            ip0.setColor(
                    (((255 - (int) (255.0 * cosx[i])) & 0xff) << 16) +
                            (((255 - (int) (255.0 * cosy[i])) & 0xff) << 8) +
                            (((255 - (int) (255.0 * cosz[i])) & 0xff)));
            ip0.fill();
            ip0.setFont(new Font("Monospaced", Font.PLAIN, 10));
            ip0.setAntialiasedText(true);
            ip0.setColor(Color.black);
            ip0.moveTo(27, 32 + i * 15);
            ip0.drawString("Colour_" + (i + 1) + " R:" + (float) cosx[i] + ", G:" + (float) cosy[i] + ", B:" + (float) cosz[i]);
        }
        imp0.show();
        imp0.updateAndDraw();
    }

    protected void showMatrix(String myStain) {
        IJ.log(myStain + " Vector Matrix ---");
        for (int i = 0; i < 3; i++) {
            IJ.log("Colour[" + (i + 1) + "]:\n" +
                    "  R" + (i + 1) + ": " + (float) MODx[i] + "\n" +
                    "  G" + (i + 1) + ": " + (float) MODy[i] + "\n" +
                    "  B" + (i + 1) + ": " + (float) MODz[i] + "\n \n");
        }

        IJ.log(myStain + " Java code ---");
        IJ.log("\t\tif (myStain.equals(\"New_Stain\")){");
        IJ.log("\t\t// This is the New_Stain");
        for (int i = 0; i < 3; i++) {
            IJ.log("\t\t\tMODx[" + i + "]=" + (float) cosx[i] + ";\n" +
                    "\t\t\tMODy[" + i + "]=" + (float) cosy[i] + ";\n" +
                    "\t\t\tMODz[" + i + "]=" + (float) cosz[i] + ";\n\n");
        }
        IJ.log("}");
    }

    public void init(Roi[] rois, ImageProcessor ip, String stainName) {
        myStain = stainName;
        int p;
        double log255 = Math.log(255.0);
        double[] rgbOD = new double[3];

        rgbOD[0] = 0;
        rgbOD[1] = 0;
        rgbOD[2] = 0;

        for (int c = 0; c < 3; c++) {
            Roi roi = rois[c];
            if (roi instanceof PolygonRoi) {
                PolygonRoi polygon = (PolygonRoi) roi;
                Rectangle bounds = roi.getBounds();
                int w = roi.getBounds().width;
                int h = roi.getBounds().height;
                int n = polygon.getNCoordinates();
                int[] x = polygon.getXCoordinates();
                int[] y = polygon.getYCoordinates();

                for (int i = 0; i < n; i++) {
                    p = ip.getPixel(bounds.x + x[i], bounds.y + y[i]);
                    rgbOD[0] = rgbOD[0] + (-((255.0 * Math.log(((double) ((p & 0xff0000) >> 16) + 1) / 255.0)) / log255));
                    rgbOD[1] = rgbOD[1] + (-((255.0 * Math.log(((double) ((p & 0x00ff00) >> 8) + 1) / 255.0)) / log255));
                    rgbOD[2] = rgbOD[2] + (-((255.0 * Math.log(((double) ((p & 0x0000ff)) + 1) / 255.0)) / log255));
                }
                rgbOD[0] = rgbOD[0] / (w * h);
                rgbOD[1] = rgbOD[1] / (w * h);
                rgbOD[2] = rgbOD[2] / (w * h);


                MODx[c] = rgbOD[0];
                MODy[c] = rgbOD[1];
                MODz[c] = rgbOD[2];
            }
        }
    }

}
