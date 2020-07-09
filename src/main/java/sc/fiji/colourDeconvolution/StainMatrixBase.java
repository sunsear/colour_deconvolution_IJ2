package sc.fiji.colourDeconvolution;

import java.util.regex.Pattern;

import ij.IJ;

public abstract class StainMatrixBase {
    protected double[] cosx = new double[3];
    protected double[] cosy = new double[3];
    protected double[] cosz = new double[3];
    double[] MODx;
    double[] MODy;
    double[] MODz;
    String myStain;

    public StainMatrixBase() {
        MODx = new double[3];
        MODy = new double[3];
        MODz = new double[3];
    }

    public void init(String line) {
        String[] parts = line.split(Pattern.quote(","));
        if (parts.length == 10) {
            myStain = parts[0].replaceAll("\\s+$", "");
            MODx[0] = Double.parseDouble(parts[1].replaceAll("\\s+$", ""));
            MODy[0] = Double.parseDouble(parts[2].replaceAll("\\s+$", ""));
            MODz[0] = Double.parseDouble(parts[3].replaceAll("\\s+$", ""));
            MODx[1] = Double.parseDouble(parts[4].replaceAll("\\s+$", ""));
            MODy[1] = Double.parseDouble(parts[5].replaceAll("\\s+$", ""));
            MODz[1] = Double.parseDouble(parts[6].replaceAll("\\s+$", ""));
            MODx[2] = Double.parseDouble(parts[7].replaceAll("\\s+$", ""));
            MODy[2] = Double.parseDouble(parts[8].replaceAll("\\s+$", ""));
            MODz[2] = Double.parseDouble(parts[9].replaceAll("\\s+$", ""));
        }
    }

    public void init(String stainName, double x0, double y0, double z0,
                     double x1, double y1, double z1,
                     double x2, double y2, double z2) {
        myStain = stainName;
        MODx[0] = x0;
        MODy[0] = y0;
        MODz[0] = z0;
        MODx[1] = x1;
        MODy[1] = y1;
        MODz[1] = z1;
        MODx[2] = x2;
        MODy[2] = y2;
        MODz[2] = z2;
    }

    public double[] getMODx() {
        return MODx;
    }

    public void setMODx(double[] mODx) {
        MODx = mODx;
    }

    public double[] getMODy() {
        return MODy;
    }

    public void setMODy(double[] mODy) {
        MODy = mODy;
    }

    public double[] getMODz() {
        return MODz;
    }

    public void setMODz(double[] mODz) {
        MODz = mODz;
    }

    protected double[] initComputation(boolean doIshow) {
        double leng;

        normalizeVectorLength();

        reset2ndColourWhenUnspecified();
        reset3rdColourWhenUnspecified(doIshow);

        leng = Math.sqrt(cosx[2] * cosx[2] + cosy[2] * cosy[2] + cosz[2] * cosz[2]);
        cosx[2] = cosx[2] / leng;
        cosy[2] = cosy[2] / leng;
        cosz[2] = cosz[2] / leng;

        for (int i = 0; i < 3; i++) {
            if (cosx[i] == 0.0) cosx[i] = 0.001;
            if (cosy[i] == 0.0) cosy[i] = 0.001;
            if (cosz[i] == 0.0) cosz[i] = 0.001;
        }

        return buildInvertMatrix();
    }

    private double[] buildInvertMatrix() {
        double[] q = new double[9];
        double A, V, C;
        A = cosy[1] - cosx[1] * cosy[0] / cosx[0];
        V = cosz[1] - cosx[1] * cosz[0] / cosx[0];
        C = cosz[2] - cosy[2] * V / A + cosx[2] * (V / A * cosy[0] / cosx[0] - cosz[0] / cosx[0]);
        q[2] = (-cosx[2] / cosx[0] - cosx[2] / A * cosx[1] / cosx[0] * cosy[0] / cosx[0] + cosy[2] / A * cosx[1] / cosx[0]) / C;
        q[1] = -q[2] * V / A - cosx[1] / (cosx[0] * A);
        q[0] = 1.0 / cosx[0] - q[1] * cosy[0] / cosx[0] - q[2] * cosz[0] / cosx[0];
        q[5] = (-cosy[2] / A + cosx[2] / A * cosy[0] / cosx[0]) / C;
        q[4] = -q[5] * V / A + 1.0 / A;
        q[3] = -q[4] * cosy[0] / cosx[0] - q[5] * cosz[0] / cosx[0];
        q[8] = 1.0 / C;
        q[7] = -q[8] * V / A;
        q[6] = -q[7] * cosy[0] / cosx[0] - q[8] * cosz[0] / cosx[0];
        return q;
    }

    private void reset3rdColourWhenUnspecified(boolean doIshow) {
        if (cosx[2] == 0.0) { // 3rd colour is unspecified
            if (cosy[2] == 0.0) {
                if (cosz[2] == 0.0) {
                    if ((cosx[0] * cosx[0] + cosx[1] * cosx[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative R component.");
                        cosx[2] = 0.0;
                    } else
                        cosx[2] = Math.sqrt(1.0 - (cosx[0] * cosx[0]) - (cosx[1] * cosx[1]));

                    if ((cosy[0] * cosy[0] + cosy[1] * cosy[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative G component.");
                        cosy[2] = 0.0;
                    } else {
                        cosy[2] = Math.sqrt(1.0 - (cosy[0] * cosy[0]) - (cosy[1] * cosy[1]));
                    }

                    if ((cosz[0] * cosz[0] + cosz[1] * cosz[1]) > 1) {
                        if (doIshow)
                            IJ.log("Colour_3 has a negative B component.");
                        cosz[2] = 0.0;
                    } else {
                        cosz[2] = Math.sqrt(1.0 - (cosz[0] * cosz[0]) - (cosz[1] * cosz[1]));
                    }
                }
            }
        }
    }

    private void reset2ndColourWhenUnspecified() {
        if (cosx[1] == 0.0) { //2nd colour is unspecified
            if (cosy[1] == 0.0) {
                if (cosz[1] == 0.0) {
                    cosx[1] = cosz[0];
                    cosy[1] = cosx[0];
                    cosz[1] = cosy[0];
                }
            }
        }
    }

    private void normalizeVectorLength() {
        double[] len = new double[3];
        for (int i = 0; i < 3; i++) {
            // Normalise vector length
            cosx[i] = cosy[i] = cosz[i] = 0.0;
            len[i] = Math.sqrt(MODx[i] * MODx[i] + MODy[i] * MODy[i] + MODz[i] * MODz[i]);
            if (len[i] != 0.0) {
                cosx[i] = MODx[i] / len[i];
                cosy[i] = MODy[i] / len[i];
                cosz[i] = MODz[i] / len[i];
            }
        }
    }
}
