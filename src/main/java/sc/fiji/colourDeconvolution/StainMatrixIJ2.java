package sc.fiji.colourDeconvolution;

import java.util.regex.Pattern;

import ij.IJ;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.ColorChannelOrder;
import net.imglib2.converter.Converters;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * This class performs Colour Deconvolution for ImageJ2. It is based on the excellent work done by Gabriel Landini for
 * ImageJ1.
 * <p>
 * For example usage see StainMatrixIJ2Test
 */
public class StainMatrixIJ2 {

    public static final double LOG_255 = Math.log(255.0);

    //This lookup outperforms doing an actual calculation by a lot. Since we only have 256 possible values, this is
    //acceptable
    private static final double[] intensityToAbsorptionLookup = {
            255.0,
            223.10248608420363,
            204.44363657723153,
            191.2049721684073,
            180.9362662818518,
            172.54612266143513,
            165.45235730708814,
            159.30745825261087,
            153.887273154463,
            149.0387523660554,
            144.65273180377085,
            140.64860874563877,
            136.9651725960289,
            133.55484339129174,
            130.3799028590833,
            127.4099443368145,
            124.62009714091671,
            121.98975923866662,
            119.50167302663894,
            117.14123845025907,
            114.89599388431964,
            112.75521788797445,
            110.70961955770437,
            108.75109482984237,
            106.87253256370359,
            105.06765868023253,
            103.3309097316945,
            101.6573294754954,
            100.04248360524396,
            98.48238894328692,
            96.9734542478476,
            95.51243042101814,
            94.09636838100234,
            92.72258322512033,
            91.38862358893991,
            90.09224532287026,
            88.8313887761583,
            87.60415911084256,
            86.4088091732604,
            85.24372453446267,
            84.10741037809191,
            82.99847996852326,
            81.91564447609443,
            80.85770397217809,
            79.82353943631479,
            78.81210564190799,
            77.82242480708516,
            76.853580914046,
            75.90471461417624,
            74.9750186479072,
            74.06373371814821,
            73.17014476443615,
            72.29357759199566,
            71.43339581589812,
            70.58899808562263,
            69.759815559699,
            68.94530960387044,
            68.14496968944758,
            67.35831147131786,
            66.58487502749054,
            65.8242232441547,
            65.07594033205122,
            64.33963046155112,
            63.61491650522176,
            62.90143887788071,
            62.198854465205955,
            61.50683563291124,
            60.825069309323965,
            60.153256134935866,
            59.49110967314354,
            58.838355676970735,
            58.19473140707388,
            57.55998499678649,
            56.93387486036193,
            56.31616914093508,
            55.70664519504619,
            55.10508911085896,
            54.51129525746401,
            53.925065862891586,
            53.3462106186663,
            52.774546308926006,
            52.209896462295525,
            51.65209102486086,
            51.10096605272688,
            50.55636342276851,
            50.018130560298054,
            49.48612018247544,
            48.960190056381705,
            48.44020277076153,
            47.926025520518415,
            47.41752990311702,
            46.91459172611162,
            46.41709082507909,
            45.924910891288775,
            45.43793930849073,
            44.956066998249625,
            44.47918827329289,
            44.007200698379876,
            43.54000495823383,
            43.07750473211084,
            42.61960657460941,
            42.16621980235183,
            41.7172563861935,
            41.27263084863978,
            40.83226016617142,
            40.39606367619928,
            39.963962988387564,
            39.53588190010175,
            39.1117463157531,
            38.691484169826246,
            38.275025353389815,
            37.862301643902626,
            37.453246638139866,
            37.04779568807406,
            36.64588583955616,
            36.2474557736512,
            35.852445750491896,
            35.46079755552147,
            35.072454448004834,
            34.68736111169417,
            34.30546360754167,
            33.92670932835832,
            33.55104695532341,
            33.178426416254844,
            32.80879884555539,
            32.44211654575474,
            32.078332950571806,
            31.717402589425376,
            31.35928105332593,
            31.00392496208432,
            30.65129193277688,
            30.30134054940958,
            29.95403033372706,
            29.609321717114863,
            29.26717601354629,
            28.927555393527587,
            28.590422858997474,
            28.255742219139485,
            27.923478067067553,
            27.59359575734717,
            27.266061384316654,
            26.940841761174354,
            26.617904399799727,
            26.2972174912775,
            25.97874988709574,
            25.66247108099012,
            25.348351191407755,
            25.036360944565555,
            24.72647165807907,
            24.418655225138707,
            24.11288409921186,
            23.809131279249815,
            23.50737029537971,
            23.20757519506258,
            22.90972052969939,
            22.613781341667643,
            22.31973315177235,
            22.027551947095198,
            21.737214169227155,
            21.448696702869917,
            21.16197686479249,
            20.877032393129625,
            20.593841437009598,
            20.312382546499155,
            20.032634662854125,
            19.75457710906448,
            19.47818958068349,
            19.203452136930498,
            18.930345192057793,
            18.658849506972132,
            18.38894618110194,
            18.120616644501677,
            17.853842650185033,
            17.58860626667907,
            17.32488987079171,
            17.06267614058533,
            16.801948048549345,
            16.54268885496515,
            16.284882101456922,
            16.028511604722038,
            15.773561450435247,
            15.520015987320646,
            15.267859821386189,
            15.017077810315236,
            14.767655058010108,
            14.519576909282716,
            14.272828944687545,
            14.027396975492403,
            13.783267038782617,
            13.540425392694354,
            13.298858511773089,
            13.05855308245325,
            12.819495998655288,
            12.581674357496517,
            12.345075455112193,
            12.109686782583498,
            11.875496021969157,
            11.642491042437452,
            11.410659896495748,
            11.179990816314461,
            10.950472210142735,
            10.722092658813038,
            10.494840912332073,
            10.268705886555455,
            10.043676659943703,
            9.819742470397118,
            9.596892712167358,
            9.375116932843396,
            9.154404830409772,
            8.934746250375044,
            8.716131182968462,
            8.498549760402902,
            8.281992254202226,
            8.066449072591187,
            7.85191075794622,
            7.638367984305371,
            7.425811554935716,
            7.2142323999567255,
            7.003621574017993,
            6.7939702540298725,
            6.5852697369456035,
            6.377511437593434,
            6.170686886557529,
            5.96478772810625,
            5.759805718166585,
            5.555732722343493,
            5.352560713982973,
            5.150281772277683,
            4.948888080414071,
            4.748371923759779,
            4.548725688090454,
            4.34994185785482,
            4.152013014477052,
            3.9549318346955205,
            3.7586910889369496,
            3.563283639725093,
            3.3687024401230707,
            3.174940532208457,
            2.981991045580385,
            2.7898471958977913,
            2.5985022834480582,
            2.4079496917452907,
            2.218182886157494,
            2.029195412561938,
            1.840980896028041,
            1.6535330395270318,
            1.4668456226678344,
            1.280912500458465,
            1.0957276020923565,
            0.9112849297590061,
            0.72757855747836,
            0.5446026299583688,
            0.3623513614751912,
            0.18081903477543204,
            -0.0,
            -0.1801113263710012};
    protected double[] cosOfcoFactorx = new double[3];
    protected double[] cosOfcoFactory = new double[3];
    protected double[] cosOfcoFactorz = new double[3];

    double[] opticalDensitiesColour1 = new double[3];
    double[] opticalDensitiesColour2 = new double[3];
    double[] opticalDensitiesColour3 = new double[3];
    String myStain;

    /**
     * This function converts pixel intensity values to their absorption counterparts, or optical density values.
     * <p>
     * It converts all values between 0 and 255 according to a logarithmical curve. See testLogify to
     * see the distribution of the numbers
     *
     * @param colourValue the unsigned byte colourvalue of a specific pixel, so between 0 and 255
     * @return logarothmically redistributed value
     */
    static double convertIntensityToAbsorption(int colourValue) {
        return intensityToAbsorptionLookup[colourValue];
    }

    private void initializeColorTables(ImgPlus<UnsignedByteType>[] outputImages) {

        for (int imageNumber = 0; imageNumber < 3; imageNumber++) {
            byte[] rLUT = new byte[256];
            byte[] gLUT = new byte[256];
            byte[] bLUT = new byte[256];
            for (int j = 0; j < 256; j++) { //LUT[1]
                rLUT[255 - j] = (byte) (255.0 - (double) j * cosOfcoFactorx[imageNumber]);
                gLUT[255 - j] = (byte) (255.0 - (double) j * cosOfcoFactory[imageNumber]);
                bLUT[255 - j] = (byte) (255.0 - (double) j * cosOfcoFactorz[imageNumber]);
            }
            outputImages[imageNumber].initializeColorTables(1);
            final ColorTable8 colorTable8 = new ColorTable8(rLUT, gLUT, bLUT);
            outputImages[imageNumber].setColorTable(colorTable8, 0);
        }
    }

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

        RandomAccessibleInterval<ARGBType> mergeARGB = Converters.mergeARGB(img, ColorChannelOrder.RGB);

        Img<UnsignedByteType> outputImg1 = img.factory().create(mergeARGB);
        Img<UnsignedByteType> outputImg2 = img.factory().create(mergeARGB);
        Img<UnsignedByteType> outputImg3 = img.factory().create(mergeARGB);

        LoopBuilder.setImages(mergeARGB, outputImg1, outputImg2, outputImg3).forEachPixel(
                (input, out1, out2, out3) -> {
                    int rgba = input.get();

                    double absorbedR = convertIntensityToAbsorption(ARGBType.red(rgba));
                    double absorbedG = convertIntensityToAbsorption(ARGBType.green(rgba));
                    double absorbedB = convertIntensityToAbsorption(ARGBType.blue(rgba));

                    // Rescale to match original paper values
                    double intensityColor1 = Math.exp(-((absorbedR * q[0] + absorbedG * q[1] + absorbedB * q[2]) - 255.0) * LOG_255 / 255.0);
                    out1.set(intensityColor1 > 255 ? 255 : (int) Math.round(intensityColor1));

                    double intensityColor2 = Math.exp(-((absorbedR * q[3] + absorbedG * q[4] + absorbedB * q[5]) - 255.0) * LOG_255 / 255.0);
                    out2.set(intensityColor2 > 255 ? 255 : (int) Math.round(intensityColor2));

                    double intensityColor3 = Math.exp(-((absorbedR * q[6] + absorbedG * q[7] + absorbedB * q[8]) - 255.0) * LOG_255 / 255.0);
                    out3.set(intensityColor3 > 255 ? 255 : (int) Math.round(intensityColor3));
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

    public void init(String line) {
        String[] parts = line.split(Pattern.quote(","));
        if (parts.length == 10) {
            myStain = parts[0].replaceAll("\\s+$", "");
            opticalDensitiesColour1[0] = Double.parseDouble(parts[1].replaceAll("\\s+$", ""));
            opticalDensitiesColour1[1] = Double.parseDouble(parts[2].replaceAll("\\s+$", ""));
            opticalDensitiesColour1[2] = Double.parseDouble(parts[3].replaceAll("\\s+$", ""));
            opticalDensitiesColour2[0] = Double.parseDouble(parts[4].replaceAll("\\s+$", ""));
            opticalDensitiesColour2[1] = Double.parseDouble(parts[5].replaceAll("\\s+$", ""));
            opticalDensitiesColour2[2] = Double.parseDouble(parts[6].replaceAll("\\s+$", ""));
            opticalDensitiesColour3[0] = Double.parseDouble(parts[7].replaceAll("\\s+$", ""));
            opticalDensitiesColour3[1] = Double.parseDouble(parts[8].replaceAll("\\s+$", ""));
            opticalDensitiesColour3[2] = Double.parseDouble(parts[9].replaceAll("\\s+$", ""));
        }
    }

    public void init(String stainName, double x0, double y0, double z0,
                     double x1, double y1, double z1,
                     double x2, double y2, double z2) {
        myStain = stainName;
        opticalDensitiesColour1[0] = x0;
        opticalDensitiesColour1[1] = y0;
        opticalDensitiesColour1[2] = z0;
        opticalDensitiesColour2[0] = x1;
        opticalDensitiesColour2[1] = y1;
        opticalDensitiesColour2[2] = z1;
        opticalDensitiesColour3[0] = x2;
        opticalDensitiesColour3[1] = y2;
        opticalDensitiesColour3[2] = z2;
    }

    protected double[] initComputation(boolean doIshow) {

        normalizeVectorLength();

        reset2ndColourWhenUnspecified();
        reset3rdColourWhenUnspecified(doIshow);

        initMatrixToPreventDivisionByZero();

        return buildInvertMatrix();
    }

    private void initMatrixToPreventDivisionByZero() {
        for (int i = 0; i < 3; i++) {
            if (cosOfcoFactorx[i] == 0.0) cosOfcoFactorx[i] = 0.001;
            if (cosOfcoFactory[i] == 0.0) cosOfcoFactory[i] = 0.001;
            if (cosOfcoFactorz[i] == 0.0) cosOfcoFactorz[i] = 0.001;
        }
    }

    private double[] buildInvertMatrix() {
        double[] q = new double[9];
        double A, V, C;
        A = cosOfcoFactory[1] - cosOfcoFactorx[1] * cosOfcoFactory[0] / cosOfcoFactorx[0];
        V = cosOfcoFactorz[1] - cosOfcoFactorx[1] * cosOfcoFactorz[0] / cosOfcoFactorx[0];
        C = cosOfcoFactorz[2] - cosOfcoFactory[2] * V / A + cosOfcoFactorx[2] * (V / A * cosOfcoFactory[0] / cosOfcoFactorx[0] - cosOfcoFactorz[0] / cosOfcoFactorx[0]);
        q[2] = (-cosOfcoFactorx[2] / cosOfcoFactorx[0] - cosOfcoFactorx[2] / A * cosOfcoFactorx[1] / cosOfcoFactorx[0] * cosOfcoFactory[0] / cosOfcoFactorx[0] + cosOfcoFactory[2] / A * cosOfcoFactorx[1] / cosOfcoFactorx[0]) / C;
        q[1] = -q[2] * V / A - cosOfcoFactorx[1] / (cosOfcoFactorx[0] * A);
        q[0] = 1.0 / cosOfcoFactorx[0] - q[1] * cosOfcoFactory[0] / cosOfcoFactorx[0] - q[2] * cosOfcoFactorz[0] / cosOfcoFactorx[0];
        q[5] = (-cosOfcoFactory[2] / A + cosOfcoFactorx[2] / A * cosOfcoFactory[0] / cosOfcoFactorx[0]) / C;
        q[4] = -q[5] * V / A + 1.0 / A;
        q[3] = -q[4] * cosOfcoFactory[0] / cosOfcoFactorx[0] - q[5] * cosOfcoFactorz[0] / cosOfcoFactorx[0];
        q[8] = 1.0 / C;
        q[7] = -q[8] * V / A;
        q[6] = -q[7] * cosOfcoFactory[0] / cosOfcoFactorx[0] - q[8] * cosOfcoFactorz[0] / cosOfcoFactorx[0];
        return q;
    }

    private void reset3rdColourWhenUnspecified(boolean doIshow) {
        if (cosOfcoFactorx[2] == 0.0) { // 3rd colour is unspecified
            if (cosOfcoFactory[2] == 0.0) {
                if (cosOfcoFactorz[2] == 0.0) {
                    if ((cosOfcoFactorx[0] * cosOfcoFactorx[0] + cosOfcoFactorx[1] * cosOfcoFactorx[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative R component.");
                        cosOfcoFactorx[2] = 0.0;
                    } else
                        cosOfcoFactorx[2] = Math.sqrt(1.0 - (cosOfcoFactorx[0] * cosOfcoFactorx[0]) - (cosOfcoFactorx[1] * cosOfcoFactorx[1]));

                    if ((cosOfcoFactory[0] * cosOfcoFactory[0] + cosOfcoFactory[1] * cosOfcoFactory[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative G component.");
                        cosOfcoFactory[2] = 0.0;
                    } else {
                        cosOfcoFactory[2] = Math.sqrt(1.0 - (cosOfcoFactory[0] * cosOfcoFactory[0]) - (cosOfcoFactory[1] * cosOfcoFactory[1]));
                    }

                    if ((cosOfcoFactorz[0] * cosOfcoFactorz[0] + cosOfcoFactorz[1] * cosOfcoFactorz[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative B component.");
                        cosOfcoFactorz[2] = 0.0;
                    } else {
                        cosOfcoFactorz[2] = Math.sqrt(1.0 - (cosOfcoFactorz[0] * cosOfcoFactorz[0]) - (cosOfcoFactorz[1] * cosOfcoFactorz[1]));
                    }
                }
            }
        }
        //is dit dubbel op
        double leng = Math.sqrt(cosOfcoFactorx[2] * cosOfcoFactorx[2] + cosOfcoFactory[2] * cosOfcoFactory[2] + cosOfcoFactorz[2] * cosOfcoFactorz[2]);
        cosOfcoFactorx[2] = cosOfcoFactorx[2] / leng;
        cosOfcoFactory[2] = cosOfcoFactory[2] / leng;
        cosOfcoFactorz[2] = cosOfcoFactorz[2] / leng;
    }

    private void reset2ndColourWhenUnspecified() {
        if (cosOfcoFactorx[1] == 0.0) { //2nd colour is unspecified
            if (cosOfcoFactory[1] == 0.0) {
                if (cosOfcoFactorz[1] == 0.0) {
                    cosOfcoFactorx[1] = cosOfcoFactorz[0];
                    cosOfcoFactory[1] = cosOfcoFactorx[0];
                    cosOfcoFactorz[1] = cosOfcoFactory[0];
                }
            }
        }
    }

    private void normalizeVectorLength() {
        cosOfcoFactorx[0] = cosOfcoFactory[0] = cosOfcoFactorz[0] = 0.0;
        double len = Math.sqrt(opticalDensitiesColour1[0] * opticalDensitiesColour1[0] +
                opticalDensitiesColour1[1] * opticalDensitiesColour1[1] +
                opticalDensitiesColour1[2] * opticalDensitiesColour1[2]);
        if (len != 0.0) {
            cosOfcoFactorx[0] = opticalDensitiesColour1[0] / len;
            cosOfcoFactory[0] = opticalDensitiesColour1[1] / len;
            cosOfcoFactorz[0] = opticalDensitiesColour1[2] / len;
        }

        cosOfcoFactorx[1] = cosOfcoFactory[1] = cosOfcoFactorz[1] = 0.0;
        len = Math.sqrt(opticalDensitiesColour2[0] * opticalDensitiesColour2[0] +
                opticalDensitiesColour2[1] * opticalDensitiesColour2[1] +
                opticalDensitiesColour2[2] * opticalDensitiesColour2[2]);
        if (len != 0.0) {
            cosOfcoFactorx[1] = opticalDensitiesColour2[0] / len;
            cosOfcoFactory[1] = opticalDensitiesColour2[1] / len;
            cosOfcoFactorz[1] = opticalDensitiesColour2[2] / len;
        }

        cosOfcoFactorx[2] = cosOfcoFactory[2] = cosOfcoFactorz[2] = 0.0;
        len = Math.sqrt(opticalDensitiesColour3[0] * opticalDensitiesColour3[0] +
                opticalDensitiesColour3[1] * opticalDensitiesColour3[1] +
                opticalDensitiesColour3[2] * opticalDensitiesColour3[2]);
        if (len != 0.0) {
            cosOfcoFactorx[2] = opticalDensitiesColour3[0] / len;
            cosOfcoFactory[2] = opticalDensitiesColour3[1] / len;
            cosOfcoFactorz[2] = opticalDensitiesColour3[2] / len;
        }
    }
}
