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
import java.text.DateFormat;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClipControlFrame extends JFrame {
	private static final DecimalFormat f2_1 = new DecimalFormat("0.0");
	private static final DecimalFormat f3_0 = new DecimalFormat("000");
	private static final DateFormat dateFormatter = DateFormat
			.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	private int frameWidth;
	private int frameHeight;
	private int imageWidth;
	private int imageHeight;
	private int xPos, yPos;
	private JLabel imgLabel;
	private JLabel timeLabel;
	private JButton captureButton;
	private JButton sendButton;
	private JButton clearButton;
	private boolean showSlides = true;
	private ClipControlInterface client;

	ClipControlFrame(String title, String[] args, ClipControlInterface client) {
		super(title);

		this.client = client;

		// Defaults
		frameWidth = 250;
		frameHeight = 250;
		imageWidth = 200;
		imageHeight = 200;
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
				ClipControlFrame.this.client.quit();
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

		//Image Label
		imgLabel = new JLabel();
		imgLabel.setSize(new Dimension(imageWidth, imageHeight));
		bag.setConstraints(imgLabel, c);
		imgPanel.add(imgLabel);
		
		//Capture Button
		c.gridwidth = GridBagConstraints.REMAINDER; // Make new row after this cell
		captureButton = new JButton("Capture");
		captureButton.setToolTipText("Press and Hold to caputure Clip");
		MouseListener captureListener = new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				client.startCapture();
			}

			public void mouseReleased(MouseEvent mouseEvent) {
				client.stopCapture();
			}
		};
		captureButton.addMouseListener(captureListener);
		bag.setConstraints(captureButton, c);
		imgPanel.add(captureButton);
		
		// Time Label
		timeLabel = new JLabel();
		timeLabel.setSize(new Dimension(50, 20));
		timeLabel.setText("Waiting for Image");
		bag.setConstraints(timeLabel, c);
		imgPanel.add(timeLabel);
		
		//Send Label
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
		imgPanel.add(sendButton);

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
		imgPanel.add(clearButton);

		getContentPane().add(imgPanel, "Center");

	}


	void update(byte[] pixels, long count) {
		if (pixels == null) {
			return;
		} else if (showSlides) {
			imgLabel.setIcon(new ImageIcon(pixels));
			timeLabel.setText(dateFormatter.format(count) + " "
					+ f3_0.format(count % 1000) + "ms");
		} else {
			imgLabel.setText(Long.toString(count));
			timeLabel.setText(f2_1.format((count % 100000) / 1000.0));
		}
		resize();
	}

	// We're not using this at the moment
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
