package org.cougaar.test.knode.experiment.bette;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
    private int frameWidth;
    private int frameHeight;
    private int imageWidth;
    private int imageHeight;
    private javax.swing.JLabel imgLabel;
    private boolean showSlides = true;
    private ImageDisplaySenderPlugin client;

	 
	 ImageFrame(String title,  String[] args, ImageDisplaySenderPlugin client)
	    {
		super(title);

		this.client = client;
		// Defaults
		frameWidth = 840;
		frameHeight = 720;
		imageWidth = 650;
		imageHeight = 650;
		showSlides = true;

		for (int i=0; i < args.length; i++) {
		    String arg = args[i];
		    if (arg.equals("-frame-width")) {
			frameWidth = Integer.parseInt(args[++i]);
		    } else if (arg.equals("-frame-height")) {
			frameHeight = Integer.parseInt(args[++i]);
		    } else if (arg.equals("-image-width")) {
			imageWidth = Integer.parseInt(args[++i]);
		    } else if (arg.equals("-image-height")) {
			imageHeight = Integer.parseInt(args[++i]);
		    } else if (arg.equals("-show-slides")) {
			showSlides = (args[++i].equalsIgnoreCase("true"));
		    }
		}

		// Make nice quit
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
			    ImageFrame.this.client.quit();
			}
		    });

		getContentPane().setLayout(new java.awt.BorderLayout());

		addPictureArea();

		pack();

		setSize(frameWidth, frameHeight);
		setLocation(20, 20);

	    }

	    void quit() {
		dispose();
	    }



	    private void addPictureArea() {
		imgLabel = new javax.swing.JLabel();
		imgLabel.setSize(new java.awt.Dimension(imageWidth, imageHeight));
		java.awt.GridBagLayout bag = new java.awt.GridBagLayout();
		java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
		c.insets = new java.awt.Insets(20,20,20,20);
		bag.setConstraints(imgLabel, c);
		javax.swing.JPanel imgPanel = new javax.swing.JPanel();
		imgPanel.setLayout(bag);
		imgPanel.add(imgLabel);

		getContentPane().add(imgPanel, "Center");

	    }


	    void update(byte[] pixels, long count) {
		if (pixels == null) {
		    return;
		} else if (showSlides) {
		    imgLabel.setIcon(new javax.swing.ImageIcon(pixels));
		} else {
		    imgLabel.setText(Long.toString(count));
		}
	    }



	    // We're not using this at the moment
	    @SuppressWarnings("unused")
		private void resize() {
		java.awt.Dimension now = getSize();
		java.awt.Dimension pref = getPreferredSize();
		int newWidth = now.width;
		int newHeight = now.height;
		boolean change = false;
		if (now.width < pref.width) {
		    change = true;
		    newWidth = pref.width;
		}
		if (now.height < pref.height) {
		    change = true;
		    newHeight = pref.height;
		}
		if (change) {
		    setSize(new java.awt.Dimension(newWidth, newHeight));
		    setLocation(20, 20);
		}
	    }



}
