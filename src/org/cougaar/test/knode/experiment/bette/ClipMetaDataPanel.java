package org.cougaar.test.knode.experiment.bette;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClipMetaDataPanel
      implements ActionListener {
   private static final DecimalFormat f6_4 = new DecimalFormat("00.0000");

   private ClipHolder clip;
   private JPanel metaDataPanel;
   private JLabel latitudeLabel = new JLabel("Lat");
   private JLabel longitudeLabel = new JLabel("Long");
   private JLabel noteLabel = new JLabel("Note:");
   private JLabel imageCountLabel = new JLabel("Available Images");
   private JLabel totalCountLabel = new JLabel("Captured Images");
   private JTextField latitudeField = new JTextField(10);
   private JTextField longitudeField = new JTextField(10);
   private JTextField noteField = new JTextField(25);
   private JTextField imageCountField = new JTextField(10);
   private JTextField totalCountField = new JTextField(10);

   private Dimension clipSize;

   public ClipMetaDataPanel() {
      super();
      clipSize = new Dimension(640, 480);
   }

   public JPanel createClipMetaDataPanel() {
      noteLabel.setLabelFor(noteField);
      noteField.setActionCommand("note");
      noteField.addActionListener(this);

      longitudeLabel.setLabelFor(longitudeField);
      longitudeField.setActionCommand("longitude");
      longitudeField.setEditable(false);

      latitudeLabel.setLabelFor(latitudeField);
      latitudeField.setActionCommand("latitude");
      latitudeField.setEditable(false);

      imageCountLabel.setLabelFor(imageCountField);
      imageCountField.setActionCommand("imageCount");
      imageCountField.setEditable(false);

      totalCountLabel.setLabelFor(totalCountField);
      totalCountField.setActionCommand("totalCount");
      totalCountField.setEditable(false);

      JLabel[] labels = {
         latitudeLabel,
         longitudeLabel,
         imageCountLabel,
         totalCountLabel
      };
      JTextField[] textFields = {
         latitudeField,
         longitudeField,
         imageCountField,
         totalCountField
      };

      GridBagLayout bag = new GridBagLayout();
      metaDataPanel = new JPanel();
      metaDataPanel.setLayout(bag);
      GridBagConstraints c = new GridBagConstraints();
      c.insets = new Insets(5, 5, 5, 5);
      c.fill = GridBagConstraints.BOTH;

      addLabelTextRows(labels, textFields, bag, metaDataPanel);

      return metaDataPanel;
   }

   public void updateWithClip(ClipHolder newClip) {
      setClip(newClip);
      noteField.setText(clip.getNote());
      latitudeField.setText(f6_4.format(clip.getLatitude()));
      longitudeField.setText(f6_4.format(clip.getLongitude()));
      imageCountField.setText(Integer.toString(clip.getImageCount()));
      totalCountField.setText(Integer.toString(clip.getTotalImages()));
   }

   public void clear() {
      setClip(null);
      noteField.setText("");
      latitudeField.setText("");
      longitudeField.setText("");
      imageCountField.setText("");
      totalCountField.setText("");
   }

   public ClipHolder getClip() {
      return clip;
   }

   public void setClip(ClipHolder clip) {
      this.clip = clip;
   }

   // from swing tutorial
   private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, GridBagLayout gridbag, Container container) {
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      int numLabels = labels.length;

      for (int i = 0; i < numLabels; i++) {
         c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
         c.fill = GridBagConstraints.NONE; // reset to default
         c.weightx = 0.0; // reset to default
         container.add(labels[i], c);

         c.gridwidth = GridBagConstraints.REMAINDER; // end row
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1.0;
         container.add(textFields[i], c);
      }
   }

   public void actionPerformed(ActionEvent e) {
      // TODO Auto-generated method stub

   }

}
