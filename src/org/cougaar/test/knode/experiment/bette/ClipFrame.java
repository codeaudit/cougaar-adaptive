package org.cougaar.test.knode.experiment.bette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ClipFrame extends JFrame {
	private int frameWidth;
	private int frameHeight;
	private int xPos, yPos;
	private Quitable client;
	private ClipPanel clipPanel;

	ClipFrame(String title, String[] args, Quitable client) {
		super(title);

		this.client = client;
		this.clipPanel = new ClipPanel();
		// Defaults
		frameWidth = 840;
		frameHeight = 720;
		xPos = 20;
		yPos = 20;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-frame-width")) {
				frameWidth = Integer.parseInt(args[++i]);
			} else if (arg.equals("-frame-height")) {
				frameHeight = Integer.parseInt(args[++i]);
			} else if (arg.equals("-image-width")) {
				clipPanel.setImageWidth(Integer.parseInt(args[++i]));
			} else if (arg.equals("-image-height")) {
				clipPanel.setImageHeight(Integer.parseInt(args[++i]));
			} else if (arg.equals("-x-position")) {
				xPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-y-position")) {
				yPos = Integer.parseInt(args[++i]);
			} else if (arg.equals("-show-slides")) {
				clipPanel.setShowSlides((args[++i].equalsIgnoreCase("true")));
			}
		}

		// Make nice quit
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ClipFrame.this.client.quit();
			}
		});

		getContentPane().setLayout(new BorderLayout());
		JPanel cPanel = clipPanel.createClipPanel();
		getContentPane().add(cPanel, "Center");
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
		clipPanel.clearImage();		
	}

	public void update(byte[] image, long timeStamp) {
		clipPanel.update(image, timeStamp);
		resize();
	}

}
