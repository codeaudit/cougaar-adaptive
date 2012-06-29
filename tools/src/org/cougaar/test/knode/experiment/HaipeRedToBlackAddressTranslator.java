package org.cougaar.test.knode.experiment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary hack until we figure out where to get the mapping
 * 
 */
public class HaipeRedToBlackAddressTranslator {
   private static Map<InetAddress, InetAddress> map = new HashMap<InetAddress, InetAddress>();

   private static void add(String from, String to)
         throws UnknownHostException {
      map.put(InetAddress.getByName(from), InetAddress.getByName(to));
   }

   static {
      try {
         add("10.3.0.162", "10.4.0.162");
         add("10.3.0.163", "10.4.0.163");
         add("10.3.0.164", "10.4.0.164");
         add("10.3.0.165", "10.4.0.165");
         add("10.3.0.166", "10.4.0.166");
         add("10.3.0.167", "10.4.0.167");

      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
   }

   public static InetAddress get(InetAddress from) {
      return map.get(from);
   }

}
