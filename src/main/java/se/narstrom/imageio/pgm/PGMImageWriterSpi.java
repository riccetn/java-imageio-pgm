package se.narstrom.imageio.pgm;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class PGMImageWriterSpi extends ImageWriterSpi {
	public PGMImageWriterSpi() {
		super("Rickard Näström", "1.1",							/* vender, version */
				new String[] { "Portable Graymap" },			/* format name */
				new String[] { "pgm" },							/* filename extensions */
				new String[] { "image/x-portable-graymap" },	/* MIME types */
				PGMImageWriter.class.getCanonicalName(),		/* writer class name */
				new Class<?>[] { ImageOutputStream.class },		/* supported output types */
				new String[] { PGMImageReaderSpi.class.getCanonicalName() },	/* reader spi names */
				false, null, null, null, null,		/* stream metadata */
				false, null, null, null, null		/* image metadata */
				);
	}

	@Override
	public boolean canEncodeImage(ImageTypeSpecifier type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageWriter createWriterInstance(Object extension) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}
}
