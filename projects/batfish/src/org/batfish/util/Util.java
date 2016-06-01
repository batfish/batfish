package org.batfish.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.util.SkipForEqualityCheck;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

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
   
   public static List<String> getMatchingStrings(String regex, Set<String> allStrings) {
      List<String> matchingStrings = new ArrayList<String>();
      Pattern pattern;
      try {
         pattern = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex is not a valid java regex: \""
                     + regex + "\"", e);
      }
      if (pattern != null) {
         for (String s : allStrings) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.matches()) {
               matchingStrings.add(s);
            }
         }
      }
      else {
         matchingStrings.addAll(allStrings);
      }
      return matchingStrings;
   }
   
   private Util() {
   }
}
