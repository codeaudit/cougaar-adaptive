package org.cougaar.test.knode.experiment.bette;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import org.cougaar.util.ConfigFinder;

public class TimedImageFromConfigPlugin
      extends TimedImageBasePlugin {

   @Override
   protected String[] listImages(String dirName) {
      ConfigFinder finder = ConfigFinder.getInstance();
      File dir = finder.locateFile(dirName);
      String[] fileList = dir.list(new ImageFilter());
      Arrays.sort(fileList);
      return fileList;
   }

   @Override
   protected byte[] readImage(int imageNumber)
         throws Exception {
      byte[] pixels = null;
      String[] imageNames = getImageNames();
      String imageName = imageNames[imageNumber];
      ConfigFinder finder = ConfigFinder.getInstance();
      InputStream imageStream = finder.open(imageDirectory + File.separator + imageName);
      pixels = new byte[imageStream.available()];
      imageStream.read(pixels);
      imageStream.close();
      return pixels;
   }
}
