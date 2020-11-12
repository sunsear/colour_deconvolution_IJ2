package sc.fiji.colorDeconvolution;

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
 * This class performs Color Deconvolution for ImageJ2. It is based on the excellent work done by Gabriel Landini for
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

    public static final int STAIN1 = 0;
    public static final int STAIN2 = 1;
    public static final int STAIN3 = 2;

    public static final int R = 0;
    public static final int G = 1;
    public static final int B = 2;


    private final double[] opticalDensitiesStain1 = new double[3];
    private final double[] opticalDensitiesStain2 = new double[3];
    private final double[] opticalDensitiesStain3 = new double[3];

    //These are the normalized Optical Densities for each color  
    private final double[][] normOD = new double[3][3];

    private String stainComboName;

    /**
     * This function converts pixel intensity values to their absorption counterparts, or optical density values.
     * <p>
     * It converts all values between 0 and 255 according to a logarithmic curve. See
     * testAbsorptionLookupPerformsAsComputation to see the original formula for coming up with the numbers
     *
     * @param colorValue the unsigned byte color value of a specific pixel, so between 0 and 255
     * @return logarithmically redistributed value
     */
    static double convertIntensityToAbsorption(int colorValue) {
        return intensityToAbsorptionLookup[colorValue];
    }

    /**
     * Initialize this stain matrix from a set of specified Stain and Channel values. These values represent vectors
     * of optical densities. The vectors need not be normalized as that is done by the computation.
     *
     * @param stainComboName name of the combination of the 3 stains together, such as "hematoxylin, eosin and DAB"
     * @param stain1R        Red channel optical density for stain 1
     * @param stain1G        Green channel optical density for stain 1
     * @param stain1B        Blue channel optical density for stain 1
     * @param stain2R        Red channel optical density for stain 2
     * @param stain2G        Green channel optical density for stain 2
     * @param stain2B        Blue channel optical density for stain 2
     * @param stain3R        Red channel optical density for stain 3
     * @param stain3G        Green channel optical density for stain 3
     * @param stain3B        Blue channel optical density for stain 3
     */
    public void init(String stainComboName, double stain1R, double stain1G, double stain1B,
                     double stain2R, double stain2G, double stain2B,
                     double stain3R, double stain3G, double stain3B) {
        this.stainComboName = stainComboName;
        opticalDensitiesStain1[R] = stain1R;
        opticalDensitiesStain1[G] = stain1G;
        opticalDensitiesStain1[B] = stain1B;
        opticalDensitiesStain2[R] = stain2R;
        opticalDensitiesStain2[G] = stain2G;
        opticalDensitiesStain2[B] = stain2B;
        opticalDensitiesStain3[R] = stain3R;
        opticalDensitiesStain3[G] = stain3G;
        opticalDensitiesStain3[B] = stain3B;
    }

    /**
     * This method intended to initialize the stain matrix from a configuration file
     *
     * @param line String in the format of STAIN_COMBO_NAME,R,G,B,R,G,B,R,G,B
     */
    public void init(String line) {
        String[] parts = line.split(Pattern.quote(","));
        if (parts.length == 10) {
            stainComboName = parts[0].replaceAll("\\s+$", "");
            opticalDensitiesStain1[R] = Double.parseDouble(parts[1].replaceAll("\\s+$", ""));
            opticalDensitiesStain1[G] = Double.parseDouble(parts[2].replaceAll("\\s+$", ""));
            opticalDensitiesStain1[B] = Double.parseDouble(parts[3].replaceAll("\\s+$", ""));
            opticalDensitiesStain2[R] = Double.parseDouble(parts[4].replaceAll("\\s+$", ""));
            opticalDensitiesStain2[G] = Double.parseDouble(parts[5].replaceAll("\\s+$", ""));
            opticalDensitiesStain2[B] = Double.parseDouble(parts[6].replaceAll("\\s+$", ""));
            opticalDensitiesStain3[R] = Double.parseDouble(parts[7].replaceAll("\\s+$", ""));
            opticalDensitiesStain3[G] = Double.parseDouble(parts[8].replaceAll("\\s+$", ""));
            opticalDensitiesStain3[B] = Double.parseDouble(parts[9].replaceAll("\\s+$", ""));
        }
    }

    /**
     * Compute the Deconvolution images and return an ImgPlus array of three 8-bit
     * images. If the specimen is stained with a 2 color scheme (such as H &amp;
     * E) the 3rd image represents the complimentary of the first two colors
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

    private double[] initComputation(boolean showLog) {

        normalizeVectorLengths();

        reset2ndStainWhenUnspecified();
        reset3rdStainWhenUnspecified(showLog);

        initMatrixToPreventDivisionByZero();

        return buildInvertMatrix();
    }

    /**
     * This method normalizes the vector length of all optical density vectors.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Unit_vector">Wikipedia definition of a normalized vector</a>
     */
    private void normalizeVectorLengths() {
        normalizeVectorLength(normOD[STAIN1], opticalDensitiesStain1);
        normalizeVectorLength(normOD[STAIN2], opticalDensitiesStain2);
        normalizeVectorLength(normOD[STAIN3], opticalDensitiesStain3);
    }

    /**
     * This method normalizes the vector length of an optical density vector.
     *
     * @param normODColor the normalized vector of optical densities for a color
     * @param oDColor     the vector of optical densities to normalize
     * @see <a href="https://en.wikipedia.org/wiki/Unit_vector">Wikipedia definition of a normalized vector</a>
     */
    private void normalizeVectorLength(double[] normODColor, double[] oDColor) {
        double length = Math.sqrt(oDColor[R] * oDColor[R] + oDColor[G] * oDColor[G] + oDColor[B] * oDColor[B]);
        if (length != 0.0) {
            normODColor[R] = oDColor[R] / length;
            normODColor[G] = oDColor[G] / length;
            normODColor[B] = oDColor[B] / length;
        } else {
            normODColor[0] = normODColor[1] = normODColor[2] = 0.0;
        }
    }

    private void reset2ndStainWhenUnspecified() {
        if (normOD[STAIN2][R] == 0.0 && normOD[STAIN2][G] == 0.0 && normOD[STAIN2][B] == 0.0) {
            normOD[STAIN2][R] = normOD[STAIN1][B];
            normOD[STAIN2][G] = normOD[STAIN1][R];
            normOD[STAIN2][B] = normOD[STAIN1][G];
        }
    }

    private void reset3rdStainWhenUnspecified(boolean showLog) {
        if (normOD[STAIN3][R] == 0.0 && normOD[STAIN3][G] == 0.0 && normOD[STAIN3][B] == 0.0) {
            determineChannelOfColor3(showLog, 0);
            determineChannelOfColor3(showLog, 1);
            determineChannelOfColor3(showLog, 2);

            normalizeVectorLength(normOD[STAIN3], normOD[STAIN3]);
        }
    }

    /**
     * This method determines the specified optical density for a channel when color 3 was unspecified
     *
     * @param showLog boolean value that determines whether logging should be done
     * @param channel the channel to determine the OD for
     */
    private void determineChannelOfColor3(boolean showLog, int channel) {
        if ((normOD[STAIN1][channel] * normOD[STAIN1][channel] +
                normOD[STAIN2][channel] * normOD[STAIN2][channel]) > 1) {
            if (showLog)
                IJ.log("Color_3 has a negative component in channel " + channel);
            normOD[STAIN3][channel] = 0.0;
        } else {
            normOD[STAIN3][channel] = Math.sqrt(1.0 - (normOD[STAIN1][channel] * normOD[STAIN1][channel]) - (normOD[STAIN2][channel] * normOD[STAIN2][channel]));
        }
    }

    private void initMatrixToPreventDivisionByZero() {
        for (int stain = 0; stain < 3; stain++) {
            for (int channel = 0; channel < 3; channel++) {
                if (normOD[stain][channel] == 0.0) normOD[stain][channel] = 0.001;
            }
        }
    }

    private double[] buildInvertMatrix() {
        double[] q = new double[9];
        double A, V, C;
        A = normOD[STAIN2][G] - normOD[STAIN2][R] * normOD[STAIN1][G] / normOD[STAIN1][R];
        V = normOD[STAIN2][B] - normOD[STAIN2][R] * normOD[STAIN1][B] / normOD[STAIN1][R];
        C = normOD[STAIN3][B] -
                normOD[STAIN3][G] * V / A +
                normOD[STAIN3][R] * (V / A * normOD[STAIN1][G] / normOD[STAIN1][R] - normOD[STAIN1][B] / normOD[0][R]);
        q[2] = (-normOD[STAIN3][R] / normOD[STAIN1][R] -
                normOD[STAIN3][R] / A * normOD[STAIN2][R] / normOD[STAIN1][R] * normOD[STAIN1][G] / normOD[STAIN1][R] +
                normOD[STAIN3][G] / A * normOD[STAIN2][R] / normOD[STAIN1][R]) / C;
        q[1] = -q[2] * V / A -
                normOD[STAIN2][R] / (normOD[STAIN1][R] * A);
        q[0] = 1.0 / normOD[STAIN1][R] -
                q[1] * normOD[STAIN1][G] / normOD[STAIN1][R] -
                q[2] * normOD[STAIN1][B] / normOD[STAIN1][R];
        q[5] = (-normOD[STAIN3][G] / A +
                normOD[STAIN3][R] / A * normOD[STAIN1][G] / normOD[STAIN1][R]) / C;
        q[4] = -q[5] * V / A + 1.0 / A;
        q[3] = -q[4] * normOD[STAIN1][G] / normOD[STAIN1][R] - q[5] * normOD[STAIN1][B] / normOD[STAIN1][R];
        q[8] = 1.0 / C;
        q[7] = -q[8] * V / A;
        q[6] = -q[7] * normOD[STAIN1][G] / normOD[STAIN1][R] - q[8] * normOD[STAIN1][B] / normOD[STAIN1][R];
        return q;
    }

    private void initializeColorTables(ImgPlus<UnsignedByteType>[] outputImages) {

        for (int imageNumber = 0; imageNumber < 3; imageNumber++) {
            byte[] rLUT = new byte[256];
            byte[] gLUT = new byte[256];
            byte[] bLUT = new byte[256];
            for (int j = 0; j < 256; j++) { //LUT[1]
                rLUT[255 - j] = (byte) (255.0 - (double) j * normOD[imageNumber][0]);
                gLUT[255 - j] = (byte) (255.0 - (double) j * normOD[imageNumber][1]);
                bLUT[255 - j] = (byte) (255.0 - (double) j * normOD[imageNumber][2]);
            }
            outputImages[imageNumber].initializeColorTables(1);
            final ColorTable8 colorTable8 = new ColorTable8(rLUT, gLUT, bLUT);
            outputImages[imageNumber].setColorTable(colorTable8, 0);
        }
    }
}
