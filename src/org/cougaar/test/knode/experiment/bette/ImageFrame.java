package org.cougaar.test.knode.experiment.bette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageFrame extends JFrame {
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f3_0 = new DecimalFormat("000");
	private static final DateFormat dateFormatter = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	private int frameWidth;
	private int frameHeight;
	private int imageWidth;
	private int imageHeight;
	private int xPos, yPos;
	private javax.swing.JLabel imgLabel;
	private javax.swing.JLabel timeLabel;
	private boolean showSlides = true;
	private Quitable client;

	ImageFrame(String title, String[] args, Quitable client) {
		super(title);

		this.client = client;
		// Defaults
		frameWidth = 840;
		frameHeight = 720;
		imageWidth = 650;
		imageHeight = 650;
		xPos = 20;
		yPos = 20;
		showSlides = true;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-frame-width")) {
				frameWidth = Integer.parseInt(args[++i]);
			} else if (arg.equals("-frame-height")) {
				frameHeight = Integer.parseInt(args[++i]);
			} else if (arg.equals("-image-width")) {
				imageWidth = Integer.parseInt(args[++i]);
			} else if (arg.equals("-image-height")) {
				imageHeight = Integer.parseInt(args[++i]);
			} else if (arg.equals("-x-position")) {
				xPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-y-position")) {
				yPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-show-slides")) {
				showSlides = (args[++i].equalsIgnoreCase("true"));
			}
		}

		// Make nice quit
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ImageFrame.this.client.quit();
			}
		});

		getContentPane().setLayout(new BorderLayout());
		addPictureArea();
		pack();

		setSize(frameWidth, frameHeight);
		setLocation(xPos, yPos);

	}

	void quit() {
		dispose();
	}

	private void addPictureArea() {
		GridBagLayout bag = new GridBagLayout();
		JPanel imgPanel = new JPanel();
		imgPanel.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;

		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		imgLabel = new JLabel();
		imgLabel.setSize(new Dimension(imageWidth, imageHeight));
		bag.setConstraints(imgLabel, c);
		imgPanel.add(imgLabel);

		timeLabel = new JLabel();
		timeLabel.setSize(new Dimension(50, 20));
		timeLabel.setText("Waiting for Image");
		bag.setConstraints(timeLabel, c);
		imgPanel.add(timeLabel);

		getContentPane().add(imgPanel, "Center");
	}

	public void update(byte[] pixels, long count) {
		if (pixels == null) {
			return;
		} else if (showSlides) {
			imgLabel.setIcon(new ImageIcon(pixels));
			imgLabel.setText(null);
			timeLabel.setText(dateFormatter.format(count) + " "
					+ f3_0.format(count % 1000) + "ms");
		} else {
			imgLabel.setText(Long.toString(count));
			timeLabel.setText(f2_1.format((count % 100000) / 1000.0));
		}
		resize();
	}
	
	public void clearImage() {
	   imgLabel.setText("Waiting For Image");
	   imgLabel.setIcon(null);
	   timeLabel.setText("");
	}

	private void resize() {
		Dimension now = getSize();
		Dimension pref = getPreferredSize();
		int newWidth = now.width;
		int newHeight = now.height;
		boolean change = false;
		if (now.width != pref.width) {
			change = true;
			newWidth = pref.width;
		}
		if (now.height != pref.height) {
			change = true;
			newHeight = pref.height;
		}
		if (change) {
			setSize(new Dimension(newWidth, newHeight));
			setLocation(xPos, yPos);
		}
	}

}
