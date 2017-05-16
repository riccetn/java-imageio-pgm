package se.narstrom.imageio.pgm;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class PGMImageReaderSpi extends ImageReaderSpi {
	public PGMImageReaderSpi() {
		super("Rickard Närström", "0.0.1-SNAPSHOT",				/* vender, version */
				new String[] { "Portable Graymap" },			/* names */
				new String[] { "pgm" },							/* filename extensions */
				new String[] { "image/x-portable-graymap" },	/* MIME types */
				"se.narstrom.imageio.pgm.PGMImageReader",		/* reader class name */
				new Class<?>[] { ImageInputStream.class },		/* supported input types */
				null,		/* writer spi class name */
				false,		/* supports standard stream metadata format */
				null, null, /* Native stream metadata: name, class name */
				null, null, /* Extra stream metadata: name, class name */
				false,		/* supports standard image metadata format */
				null, null, /* Native image metadata: name, class name */
				null, null	/* Extra image metadata: name, class name */
				);
	}

	@Override
	public boolean canDecodeInput(Object source) throws IOException {
		if(!(source instanceof ImageInputStream))
			return false;
		ImageInputStream in = (ImageInputStream)source;
		in.mark();
		boolean ret = in.read() == 'P' && in.read() == '2'; // Magic number
		in.reset();
		return ret;
	}

	@Override
	public ImageReader createReaderInstance(Object extension) throws IOException {
		return new PGMImageReader(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "Portable Graymap image reader";
	}
}
