package de.ismll.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * subclasses shall override render(Graphics2D) and call super.render(g2) first.
 * 
 * @author Andre Busche
 *
 */
public class AbstractVolatileImagePanel extends JPanel {


	public static Color[] colors  = new Color[256];

	static {
		for (int i = 0; i < colors.length; i++) {
			colors[i] = new Color(i, i, i);
		}
	}


	protected Logger log = LogManager.getLogger(getClass());

	protected final static VolatileImage createVolatileImage(int width, int height, int transparency) {

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		VolatileImage image = gc.createCompatibleVolatileImage(width, height, transparency);

		int valid = image.validate(gc);

		if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
			image = createVolatileImage(width, height, transparency);
			return image;
		}

		return image;
	}

	private VolatileImage vimage;

	protected int width = -1;

	protected int height = -1;

	private int transparency = Transparency.OPAQUE;

	private final boolean infoEnabled;

	public AbstractVolatileImagePanel() {
		super();
		infoEnabled=log.isInfoEnabled();

	}

	public final void draw(Graphics2D g, int x, int y) {

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		if (vimage == null) {
			vimage = createVolatileImage(width, height, transparency);
			if (vimage.validate(gc) == VolatileImage.IMAGE_OK)
				render();
		}

		// Since we're copying from the VolatileImage, we need it in a good
		// state.
		if (vimage.validate(gc) != VolatileImage.IMAGE_OK) {
			vimage = createVolatileImage(vimage.getWidth(), vimage.getHeight(),
					vimage.getTransparency());
			render();
		}

		g.drawImage(vimage, x, y, null);
	}

	protected void paintComponent(Graphics g0) {
		if (!(g0 instanceof Graphics2D))
			return;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (ge.isHeadlessInstance()) {
			if (infoEnabled) log.info("This is a headless environment");
			render((Graphics2D)g0);
		} else
			draw((Graphics2D) g0, 0, 0);
	}

	public final void render() {

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

		if (vimage == null)
			vimage = createVolatileImage(width, height, transparency);

		Graphics2D g = null;
		do {

			int valid = vimage.validate(gc);

			if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
				vimage = createVolatileImage(width, height, transparency);
			}

			try {
				g = vimage.createGraphics();

				render(g);
				// else, and is only used as an example.
			} finally {
				// It's always best to dispose of your Graphics objects.
				g.dispose();
			}
		} while (vimage.contentsLost());
	}

	public void render(Graphics2D g) {
		// no op, implement in subclasses!
		g.clearRect(0, 0, width, height);

	}

}