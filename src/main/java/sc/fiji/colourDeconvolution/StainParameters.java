package sc.fiji.colourDeconvolution;

import static sc.fiji.colourDeconvolution.StainParameters.Constants.*;

public enum StainParameters {
    H_E(H_E_DESCR, GL_HAEM_MATRIX, GL_EOS_MATRIX, ZERO_MATRIX),
    H_E2(H_E2_DESCR, new double[] { 0.49015734, 0.76897085, 0.41040173 }, new double[] { 0.04615336, 0.8420684, 0.5373925 }, ZERO_MATRIX),
    // 3,3-diamino-benzidine tetrahydrochloride
    H_DAB(H_DAB_DESCR, HAEM_MATRIX, DAB_MATRIX, ZERO_MATRIX),
    //GL Feulgen & light green
    FLG(FLG_DESCR, new double[] { 0.46420921, 0.83008335, 0.30827187 }, new double[] { 0.94705542, 0.25373821, 0.19650764 }, ZERO_MATRIX),
    // GL  Methylene Blue and Eosin
    GIEMSA(GIEMSA_DESCR, new double[] { 0.834750233, 0.513556283, 0.196330403 }, GL_EOS_MATRIX, ZERO_MATRIX),

    FR_FB_DAB(FR_FB_DAB_DESCR, new double[] { 0.21393921, 0.85112669, 0.47794022 }, new double[] { 0.74890292, 0.60624161, 0.26731082 }, DAB_MATRIX),

    MG_DAB(MG_DAB_DESCR, new double[] { 0.98003, 0.144316, 0.133146 }, DAB_MATRIX, ZERO_MATRIX),

    H_E_DAB(H_E_DAB_DESCR, HAEM_MATRIX, new double[] { 0.072, 0.990, 0.105 }, DAB_MATRIX),

    // 3-amino-9-ethylcarbazole
    H_AEC(H_AEC_DESCR, HAEM_MATRIX, new double[] { 0.2743, 0.6796, 0.6803 }, ZERO_MATRIX),

    //Azocarmine and Aniline Blue (AZAN)
    A_Z(A_Z_DESCR, new double[] { 0.853033, 0.508733, 0.112656 }, new double[] { 0.09289875, 0.8662008, 0.49098468 }, new double[] { 0.10732849, 0.36765403, 0.9237484 }),

    MAS_TRI(MAS_TRI_DESCR, new double[] { 0.7995107, 0.5913521, 0.10528667 }, new double[] { 0.09997159, 0.73738605, 0.6680326 }, ZERO_MATRIX),

    ALC_B_H(ALC_B_H_DESCR, new double[] { 0.874622, 0.457711, 0.158256 }, new double[] { 0.552556, 0.7544, 0.353744 }, ZERO_MATRIX),

    H_PAS(H_PAS_DESCR, GL_HAEM_MATRIX, new double[] { 0.175411, 0.972178, 0.154589 }, ZERO_MATRIX),
    RGB(RGB_DESCR, new double[] { 0.0, 1.0, 1.0 }, new double[] { 1.0, 0.0, 1.0 }, new double[] { 1.0, 1.0, 0.0 }),
    CMY(CMY_DESCR, new double[] { 1.0, 0.0, 0.0 }, new double[] { 0.0, 1.0, 0.0 }, new double[] { 0.0, 0.0, 1.0 });

    private final String description;
    private final double[] rgb1;
    private final double[] rgb2;
    private final double[] rgb3;

    StainParameters(String description, double[] rgb1, double[] rgb2, double[] rgb3) {
        this.description = description;
        this.rgb1 = rgb1;
        this.rgb2 = rgb2;
        this.rgb3 = rgb3;
    }

    String description() {
        return description;
    }

    double[] rgb1() {
        return rgb1;
    }

    double[] rgb2() {
        return rgb2;
    }

    double[] rgb3() {
        return rgb3;
    }

    static class Constants {

        static final double[] ZERO_MATRIX = { 0.0, 0.0, 0.0 };
        static final double[] DAB_MATRIX = { 0.268, 0.570, 0.776 };
        static final double[] HAEM_MATRIX = { 0.650, 0.704, 0.286 };
        static final double[] GL_EOS_MATRIX = { 0.092789, 0.954111, 0.283111 };
        static final double[] GL_HAEM_MATRIX = { 0.644211, 0.716556, 0.266844 };
        static final String H_E_DESCR = "H&E";
        public static final String H_E2_DESCR = "H&E 2";
        public static final String H_DAB_DESCR = "H DAB";
        public static final String FLG_DESCR = "Feulgen Light Green";
        public static final String GIEMSA_DESCR = "Giemsa";
        public static final String FR_FB_DAB_DESCR = "FastRed FastBlue DAB";
        public static final String MG_DAB_DESCR = "Methyl Green DAB";
        public static final String H_E_DAB_DESCR = "H&E DAB";
        public static final String H_AEC_DESCR = "H AEC";
        public static final String A_Z_DESCR = "Azan-Mallory";
        public static final String MAS_TRI_DESCR = "Masson Trichrome";
        public static final String ALC_B_H_DESCR = "Alcian blue & H";
        public static final String H_PAS_DESCR = "H PAS";
        public static final String RGB_DESCR = "RGB";
        public static final String CMY_DESCR = "CMY";
    }
}

