package org.cougaar.test.knode.experiment.bette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class ClipPanel {
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f3_0 = new DecimalFormat("000");
	private static final DateFormat dateFormatter = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private ImagePanel imagePanel;
	private JPanel clipPanel;
	private JPanel imgPanel;
	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JTextField noteLabel;
	private JLabel imageCountLabel;
	private JLabel nameLabel;
	
	private Dimension clipSize;
	
	public ClipPanel() {
		super();
		clipSize = new Dimension(640,480);
	}

	
	
	public JPanel createClipPanel() {
		GridBagLayout bag = new GridBagLayout();
		clipPanel = new JPanel();
		clipPanel.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;

		// Image
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		imagePanel = new ImagePanel();
		imgPanel = imagePanel.createImagePanel();
		bag.setConstraints(imgPanel, c);
		clipPanel.add(imgPanel);
		
		//
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		noteLabel= new JTextField(20);
		noteLabel.setName("NOTE");
		bag.setConstraints(noteLabel, c);
		clipPanel.add(noteLabel);
		return clipPanel;

	}

	public void update(byte[] pixels, long count) {
		imagePanel.update(pixels, count);
	}
	
	public void clearImage() {
	   imagePanel.clearImage();
	}

	public boolean isShowSlides() {
		return imagePanel.isShowSlides();
	}

	public void setShowSlides(boolean showSlides) {
		imagePanel.setShowSlides(showSlides);
	}

	public JPanel getPanel() {
		return clipPanel;
	}

	public int getImageWidth() {
		return (int) clipSize.getWidth();
	}

	public void setImageWidth(int width) {
		int height = (int) clipSize.getHeight();
		this.clipSize = new Dimension(width, height);
	}

	public int getImageHeight() {
		return (int) clipSize.getHeight();
	}

	public void setImageHeight(int height) {
		int width = (int) clipSize.getWidth();
		this.clipSize = new Dimension(width, height);
	}

}
