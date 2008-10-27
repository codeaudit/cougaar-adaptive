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
	private int frameWidth;
	private int frameHeight;
	private int xPos, yPos;
	private Quitable client;
	private ImagePanel imagePanel;

	ImageFrame(String title, String[] args, Quitable client) {
		super(title);

		this.client = client;
		// Defaults
		frameWidth = 840;
		frameHeight = 720;
		xPos = 20;
		yPos = 20;
		this.imagePanel = new ImagePanel();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-frame-width")) {
				frameWidth = Integer.parseInt(args[++i]);
			} else if (arg.equals("-frame-height")) {
				frameHeight = Integer.parseInt(args[++i]);
			} else if (arg.equals("-image-width")) {
				imagePanel.setImageWidth(Integer.parseInt(args[++i]));
			} else if (arg.equals("-image-height")) {
				imagePanel.setImageHeight(Integer.parseInt(args[++i]));
			} else if (arg.equals("-x-position")) {
				xPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-y-position")) {
				yPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-show-slides")) {
				imagePanel.setShowSlides((args[++i].equalsIgnoreCase("true")));
			}
		}

		// Make nice quit
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ImageFrame.this.client.quit();
			}
		});

		getContentPane().setLayout(new BorderLayout());
		JPanel imgPanel = imagePanel.createImagePanel();
		getContentPane().add(imgPanel, "Center");
		pack();

		setSize(frameWidth, frameHeight);
		setLocation(xPos, yPos);

	}

	void quit() {
		dispose();
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

	public void clearImage() {
		imagePanel.clearImage();		
	}

	public void update(byte[] image, long timeStamp) {
		imagePanel.update(image, timeStamp);
		resize();
	}

}
