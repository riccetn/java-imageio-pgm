package se.narstrom.imageio.pgm;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import se.narstrom.util.SingletonIterator;

public class PGMImageReader extends ImageReader {
	private BufferedImage img = null;

	protected PGMImageReader(PGMImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	@Override
	public int getNumImages(boolean allowSearch) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		return 1;
	}

	@Override
	public int getWidth(int imageIndex) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(this.img == null)
			this.read(0);
		return this.img.getWidth();
	}

	@Override
	public int getHeight(int imageIndex) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(this.img == null)
			this.read(0);
		return this.img.getHeight();
	}

	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(img == null)
			read(0);
		return SingletonIterator.of(new ImageTypeSpecifier(img));
	}

	@Override
	public IIOMetadata getStreamMetadata() throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		return null;
	}

	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		return null;
	}

	private boolean isSpace(int ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
	}

	private int toDigit(int ch) throws IOException {
		if(ch < '0' || ch > '9')
			throw new IIOException("invalid format");
		return ch-'0';
	}

	private int readAsciiInt(ImageInputStream in) throws IOException {
		int ret = 0;
		int ch;
		while(isSpace(ch = in.read()));
		do {
			ret = ret * 10 + toDigit(ch);
		} while(!isSpace(ch = in.read()));
		return ret;
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(img == null) {
			ImageInputStream in = (ImageInputStream)this.input;

			if(in.read() != 'P' || in.read() != '2' || !isSpace(in.read()))
				throw new IIOException("invalid magic");

			int width = readAsciiInt(in);
			int height = readAsciiInt(in);
			int maxval = readAsciiInt(in);
			img = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
			WritableRaster raster = img.getRaster();

			for(int y = 0; y < height; ++y) {
				for(int x = 0; x < width; ++x) {
					raster.setSample(x, y, 0, readAsciiInt(in)*65536/(maxval+1));
				}
			}
		}
		return img;
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly, boolean skipMetadata) {
		super.setInput(input, seekForwardOnly, skipMetadata);
		this.img = null;
	}
}
