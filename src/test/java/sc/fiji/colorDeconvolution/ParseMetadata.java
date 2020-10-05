package sc.fiji.colorDeconvolution;

import io.scif.FieldPrinter;
import io.scif.Format;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import net.imagej.ImageJ;

public class ParseMetadata {

    public static void main(final String... args) throws Exception {
        ImageJ ij = new ImageJ();
        final String filePath = "testImage.tif";
        Format format = ij.scifio().format().getFormat(filePath);
        Metadata metadata = format.createParser().parse(filePath);
        System.out.println(new FieldPrinter(metadata));
        ImageMetadata imageMeta = metadata.get(0);
        System.out.println(imageMeta);
    }

}