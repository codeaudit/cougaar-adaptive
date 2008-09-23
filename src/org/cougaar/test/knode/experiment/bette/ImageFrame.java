package org.cougaar.test.knode.experiment.bette;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
    private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
    private static final DateFormat dateFormatter = DateFormat.getDateTimeInstance();
    private int frameWidth;
    private int frameHeight;
    private int imageWidth;
    private int imageHeight;
    private javax.swing.JLabel imgLabel;
    private javax.swing.JLabel timeLabel;
    private boolean showSlides = true;
    private Quitable client;

	 
	 ImageFrame(String title,  String[] args, Quitable client)
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
		//addTimeArea();
		getContentPane().validate();

		pack();

		setSize(frameWidth, frameHeight);
		setLocation(20, 20);

	    }

	    void quit() {
		dispose();
	    }



	    private void addPictureArea() {
	        GridBagLayout bag = new GridBagLayout();
	        JPanel imgPanel = new JPanel();
	        imgPanel.setLayout(bag);
	        GridBagConstraints c = new GridBagConstraints();
	        c.insets = new Insets(20,20,20,20);
	        c.fill = GridBagConstraints.BOTH;
	        c.weightx = 1.0;
	        c.gridwidth = GridBagConstraints.LINE_END;
	        
	        imgLabel = new JLabel();
		imgLabel.setSize(new java.awt.Dimension(imageWidth, imageHeight));
		bag.setConstraints(imgLabel, c);
		imgPanel.add(imgLabel);
		
		timeLabel = new javax.swing.JLabel();
		timeLabel.setSize(new java.awt.Dimension(200, 20));
		timeLabel.setText("hello world");
		bag.setConstraints(timeLabel, c);
		imgPanel.add(timeLabel);
		
		getContentPane().add(imgPanel, "Center");

	    }


	    void update(byte[] pixels, long count) {
		if (pixels == null) {
		    return;
		} else if (showSlides) {
		    imgLabel.setIcon(new javax.swing.ImageIcon(pixels));
		    timeLabel.setText(dateFormatter.format(count));
		} else {
		    imgLabel.setText(Long.toString(count));
		    timeLabel.setText(f2_1.format((count%100000)/1000.0));
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
