package org.cougaar.test.knode.experiment.bette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ClipControlFrame extends JFrame {
	/**
    * 
    */
   private static final long serialVersionUID = 1L;
   private int frameWidth;
	private int frameHeight;
	private int xPos, yPos;
	private ImagePanel imagePanel;
	private JButton captureButton;
	private JButton sendButton;
	private JButton clearButton;
	private ClipControlInterface client;

	ClipControlFrame(String title, String[] args, ClipControlInterface client) {
		super(title);

		this.client = client;
		this.imagePanel = new ImagePanel();

		// Defaults
		frameWidth = 250;
		frameHeight = 250;
		xPos = 20;
		yPos = 20;

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
			@Override
         public void windowClosing(WindowEvent e) {
				ClipControlFrame.this.client.quit();
			}
		});
		JPanel imgPanel=imagePanel.createImagePanel();
		JPanel ctrPanel=createControlPanel();
        JPanel outside = new JPanel();
        outside.add(ctrPanel);
        outside.add(imgPanel); 
        
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(outside,"Center");
		pack();
		// TODO Too complicated to figure out size, for now let it be calculated automatically
		//setSize(frameWidth, frameHeight);
		setLocation(xPos, yPos);
	}

	public void update(byte[] image, long timeStamp) {
		imagePanel.update(image, timeStamp);
		resize();
	}

	public void clearImage() {
		imagePanel.clearImage();		
	}



	private JPanel createControlPanel() {
		GridBagLayout bag = new GridBagLayout();
		JPanel ctrPanel = new JPanel();
		ctrPanel.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;

	
		//Capture Button
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		captureButton = new JButton("Capture");
		captureButton.setToolTipText("Press and Hold to caputure Clip");
		MouseListener captureListener = new MouseAdapter() {
			@Override
         public void mousePressed(MouseEvent mouseEvent) {
				client.startCapture();
			}

			@Override
         public void mouseReleased(MouseEvent mouseEvent) {
				client.stopCapture();
			}
		};
		captureButton.addMouseListener(captureListener);
		bag.setConstraints(captureButton, c);
		ctrPanel.add(captureButton);
		
		//Send Button
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		sendButton = new JButton("Send");
		sendButton.setToolTipText("Send clip to remote viewer");
		ActionListener sendListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				client.send();
			}
		};
		sendButton.addActionListener(sendListener);
		bag.setConstraints(sendButton, c);
		ctrPanel.add(sendButton);

		//Clear button
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Clear caputured clip");
		bag.setConstraints(clearButton, c);
		ActionListener clearListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				client.clear();
			}
		};
		clearButton.addActionListener(clearListener);
		ctrPanel.add(clearButton);

		return ctrPanel;
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
}
