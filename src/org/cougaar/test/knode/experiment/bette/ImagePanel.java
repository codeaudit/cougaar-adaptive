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
import javax.swing.border.Border;

public class ImagePanel {
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f3_0 = new DecimalFormat("000");
	private static final DateFormat dateFormatter = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private JPanel imagePanel;
	private Dimension imageSize;
	private JPanel imgBorder;
	private JLabel imgLabel;
	private JLabel timeLabel;
	private boolean showSlides = true;
	
	public ImagePanel() {
		super();
		imageSize = new Dimension(640,480);
	}

	
	
	public JPanel createImagePanel() {
		GridBagLayout bag = new GridBagLayout();
		imagePanel = new JPanel();
		imagePanel.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;

		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		imgBorder = new JPanel();
		//Border blackline = BorderFactory.createLoweredBevelBorder();
		//imgBorder.setBorder(blackline);
		imgBorder.setPreferredSize(imageSize);
		imgLabel = new JLabel();
		imgBorder.add(imgLabel);
		bag.setConstraints(imgBorder, c);
		imagePanel.add(imgBorder);

		timeLabel = new JLabel();
		timeLabel.setText("Waiting for Image");
		bag.setConstraints(timeLabel, c);
		imagePanel.add(timeLabel);
		
		return imagePanel;

	}

	public void update(byte[] pixels, long count) {
		if (pixels == null) {
			return;
		} else if (showSlides) {
			imgLabel.setIcon(new ImageIcon(pixels));
			imgLabel.setText(null);
			timeLabel.setText(dateFormatter.format(count) + " "
					+ f3_0.format(count % 1000) + "ms");
			this.imageSize = imgLabel.getSize();
		} else {
			imgLabel.setText(Long.toString(count));
			timeLabel.setText(f2_1.format((count % 100000) / 1000.0));
		}
	}
	
	public void clearImage() {
	   Dimension oldSize = imgBorder.getSize();
	   imgLabel.setIcon(null);
	   imgBorder.setPreferredSize(oldSize);
	   timeLabel.setText("Waiting For Image");
	}

	public boolean isShowSlides() {
		return showSlides;
	}

	public void setShowSlides(boolean showSlides) {
		this.showSlides = showSlides;
	}

	public JPanel getPanel() {
		return imagePanel;
	}

	public int getImageWidth() {
		return (int) imageSize.getWidth();
	}

	public void setImageWidth(int width) {
		int height = (int) imageSize.getHeight();
		this.imageSize = new Dimension(width, height);
	}

	public int getImageHeight() {
		return (int) imageSize.getHeight();
	}

	public void setImageHeight(int height) {
		int width = (int) imageSize.getWidth();
		this.imageSize = new Dimension(width, height);
	}

}
