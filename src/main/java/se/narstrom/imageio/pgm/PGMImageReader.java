package se.narstrom.imageio.pgm;

import java.awt.image.BufferedImage;
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

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
		if(this.input == null)
			throw new IllegalStateException();
		if(imageIndex != 0)
			throw new IndexOutOfBoundsException();
		if(img == null) {
			ImageInputStream in = (ImageInputStream)this.input;
			String line;

			int state = 0;
			int width = 0, height = 0;
			int maxval = 0;
			int x = 0, y = 0;
			while((line = in.readLine()) != null) {
				if(state != 0 && line.charAt(0) == '#') // line comment
					continue;
				for(String token : line.split("\\s+")) {
					switch(state) {
					case 0:
						if(!token.equals("P2"))
							throw new IIOException("Invalid magic number");
						state = 1;
						break;
					case 1:
						width = Integer.parseInt(token);
						state = 2;
						break;
					case 2:
						height = Integer.parseInt(token);
						state = 3;
						break;
					case 3:
						maxval = Integer.parseInt(token);
						if(maxval < 256)
							img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
						else if(maxval < 65536)
							img = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
						else
							throw new IIOException("Maxval most be less the 65536");
						state = 4;
						break;
					case 4:
						int sample = Integer.parseInt(token);
						if(y >= height)
							break;
						if(sample > maxval)
							throw new IIOException("sample larger then maxval");
						if(maxval < 256)
							sample = sample*256/(maxval+1);
						else
							sample = sample*65536/(maxval+1);
						img.getRaster().setSample(x, y, 0, sample);
						if(++x == width) {
							++y;
							x = 0;
						}
						break;
					}
				}
			}
			if(y < height)
				throw new IIOException("Unexpected end-of-stream");
		}
		return img;
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly, boolean skipMetadata) {
		super.setInput(input, seekForwardOnly, skipMetadata);
		this.img = null;
	}
}
