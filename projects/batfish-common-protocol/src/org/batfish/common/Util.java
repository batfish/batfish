package org.batfish.common;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public final class Util {

   public static File getConfigProperties(Class<?> locatorClass) {
      File configDir = getJarOrClassDir(locatorClass);
      return Paths.get(configDir.toString(),
            BfConsts.RELPATH_CONFIG_FILE_NAME_DIR).toFile();
   }

   public static File getJarOrClassDir(Class<?> locatorClass) {
      File locatorDirFile = null;
      URL locatorSourceURL = locatorClass.getProtectionDomain().getCodeSource()
            .getLocation();
      String locatorSourceString = locatorSourceURL.toString();
      if (locatorSourceString.startsWith("onejar:")) {
         URL onejarSourceURL = null;
         try {
            onejarSourceURL = Class.forName("com.simontuffs.onejar.Boot")
                  .getProtectionDomain().getCodeSource().getLocation();
         }
         catch (ClassNotFoundException e) {
            throw new BatfishException("could not find onejar class");
         }
         File jarDir = new File(onejarSourceURL.toString()).getParentFile();
         return jarDir;
      }
      else {
         char separator = System.getProperty("file.separator").charAt(0);
         String locatorPackageResourceName = locatorClass.getPackage()
               .getName().replace('.', separator);
         try {
            locatorDirFile = new File(locatorClass.getClassLoader()
                  .getResource(locatorPackageResourceName).toURI());
         }
         catch (URISyntaxException e) {
            throw new BatfishException("Failed to resolve locator directory", e);
         }
         assert Boolean.TRUE;
         return locatorDirFile;
      }
   }

   private Util() {
   }

}
