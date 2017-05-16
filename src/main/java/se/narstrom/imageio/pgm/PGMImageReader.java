package se.narstrom.imageio.pgm;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
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
	private ImageInputStream stream = null;
	private ByteBuffer buffer = ByteBuffer.allocate(4096);

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
		return SingletonIterator.of(ImageTypeSpecifier.createGrayscale(16, DataBuffer.TYPE_USHORT, false));
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

	private byte read() throws IOException {
		if(!this.buffer.hasRemaining()) {
			this.buffer.clear();
			int read = this.stream.read(this.buffer.array(), this.buffer.arrayOffset(), this.buffer.capacity());
			if(read == -1)
				throw new EOFException();
			this.buffer.limit(read);
		}
		return this.buffer.get();
	}

	private boolean isSpace(int ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
	}

	private int toDigit(int ch) throws IOException {
		if(ch < '0' || ch > '9')
			throw new IIOException("invalid format");
		return ch-'0';
	}

	private int readAsciiInt() throws IOException {
		int ret = 0;
		byte ch;
		while(isSpace(ch = read()));
		do {
			ret = ret * 10 + toDigit(ch);
		} while(!isSpace(ch = read()));
		return ret;
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(img == null) {
			if(read() != 'P' || read() != '2' || !isSpace(read()))
				throw new IIOException("invalid magic");

			int width = readAsciiInt();
			int height = readAsciiInt();
			int maxval = readAsciiInt();
			img = ImageReader.getDestination(param, this.getImageTypes(0), width, height);
			WritableRaster raster = img.getRaster();

			for(int y = 0; y < height; ++y) {
				for(int x = 0; x < width; ++x) {
					raster.setSample(x, y, 0, readAsciiInt()*65536/(maxval+1));
				}
			}
		}
		return img;
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly, boolean skipMetadata) {
		super.setInput(input, seekForwardOnly, skipMetadata);
		this.stream = (ImageInputStream)input;
		this.img = null;
		this.buffer.clear();
		this.buffer.limit(0);
	}
}
