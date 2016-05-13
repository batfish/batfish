package org.batfish.util;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishException;

public class Util {

   public static String getText(ParserRuleContext ctx, String srcText) {
      int start = ctx.start.getStartIndex();
      int stop = ctx.stop.getStopIndex();
      return srcText.substring(start, stop);
   }

   public static String readFile(File file) {
      String text = null;
      try {
         text = FileUtils.readFileToString(file);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to read file: " + file.toString(),
               e);
      }
      return text;
   }

   public static void writeFile(String outputPath, String output) {
      File outputFile = new File(outputPath);
      try {
         FileUtils.write(outputFile, output);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to write file: " + outputPath, e);
      }
   }

   private Util() {
   }
}
