package org.cougaar.test.knode.experiment.bette;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImagePanel {
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f3_0 = new DecimalFormat("000");
	private static final DateFormat dateFormatter = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private JPanel imagePanel;
	private int imageWidth;
	private int imageHeight;
	private javax.swing.JLabel imgLabel;
	private javax.swing.JLabel timeLabel;
	private boolean showSlides = true;
	
	public ImagePanel() {
		super();
		imageWidth = 650;
		imageHeight = 650;
	}

	
	
	public JPanel createImagePanel() {
		GridBagLayout bag = new GridBagLayout();
		imagePanel = new JPanel();
		imagePanel.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;

		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		imgLabel = new JLabel();
		imgLabel.setSize(new Dimension(imageWidth, imageHeight));
		bag.setConstraints(imgLabel, c);
		imagePanel.add(imgLabel);

		timeLabel = new JLabel();
		timeLabel.setSize(new Dimension(50, 20));
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
		} else {
			imgLabel.setText(Long.toString(count));
			timeLabel.setText(f2_1.format((count % 100000) / 1000.0));
		}
		// resize();
	}
	
	public void clearImage() {
	   imgLabel.setText("Waiting For Image");
	   imgLabel.setIcon(null);
	   timeLabel.setText("");
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
		return imageWidth;
	}



	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}



	public int getImageHeight() {
		return imageHeight;
	}



	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}


}
