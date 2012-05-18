package org.cougaar.test.knode.experiment.bette;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class ClipPanel {

	private ImagePanel imagePanel;
	private JPanel clipPanel;
	private JPanel imgPanel;
	private ClipMetaDataPanel clipMetaDataPanel;
	private JPanel metaPanel;
	
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
		clipMetaDataPanel = new ClipMetaDataPanel();
		metaPanel=clipMetaDataPanel.createClipMetaDataPanel();
		bag.setConstraints(metaPanel, c);
		clipPanel.add(metaPanel);
		return clipPanel;

	}

	public void update(byte[] pixels, long count) {
		imagePanel.update(pixels, count);
	}
	
	public void updateWithClip(ClipHolder clip) {
		clipMetaDataPanel.updateWithClip(clip);
	}
	
	public void clearImage() {
	   imagePanel.clearImage();
	   clipMetaDataPanel.clear();
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
