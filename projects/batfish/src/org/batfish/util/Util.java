package org.batfish.util;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishException;
import org.batfish.representation.Ip;

public class Util {
   public static final String FACT_BLOCK_FOOTER = "\n//FACTS END HERE\n"
         + "   }) // clauses\n" + "} <-- .\n";
   public static final String NULL_INTERFACE_NAME = "null_interface";

   public static String applyPrefix(String prefix, String msg) {
      String[] lines = msg.split("\n");
      StringBuilder sb = new StringBuilder();
      for (String line : lines) {
         sb.append(prefix + line + "\n");
      }
      return sb.toString();
   }

   public static long communityStringToLong(String str) {
      String[] parts = str.split(":");
      long high = Long.parseLong(parts[0]);
      long low = Long.parseLong(parts[1]);
      long result = low + (high << 16);
      return result;
   }

   public static String escape(String offendingTokenText) {
      return offendingTokenText.replace("\n", "\\n").replace("\t", "\\t")
            .replace("\r", "\\r");
   }

   public static String extractBits(long l, int start, int end) {
      String s = "";
      for (int pos = end; pos >= start; pos--) {
         long mask = 1L << pos;
         long bit = l & mask;
         s += (bit != 0) ? 1 : 0;
      }
      return s;
   }

   public static String getIndentedString(String str, int indentLevel) {
      String indent = getIndentString(indentLevel);
      StringBuilder sb = new StringBuilder();
      String[] lines = str.split("\n");
      for (String line : lines) {
         sb.append(indent + line + "\n");
      }
      return sb.toString();
   }

   public static String getIndentString(int indentLevel) {

      String retString = "";

      for (int i = 0; i < indentLevel; i++) {
         retString += "  ";
      }

      return retString;
   }

   public static Integer getInterfaceVlanNumber(String ifaceName) {
      String prefix = "vlan";
      String ifaceNameLower = ifaceName.toLowerCase();
      String withoutDot = ifaceNameLower.replaceAll("\\.", "");
      if (withoutDot.startsWith(prefix)) {
         String vlanStr = withoutDot.substring(prefix.length());
         if (vlanStr.length() > 0) {
            return Integer.parseInt(vlanStr);
         }
      }
      return null;
   }

   public static String getText(ParserRuleContext ctx, String srcText) {
      int start = ctx.start.getStartIndex();
      int stop = ctx.stop.getStopIndex();
      return srcText.substring(start, stop);
   }

   public static String getTime(long millis) {
      long cs = (millis / 10) % 100;
      long s = (millis / 1000) % 60;
      long m = (millis / (1000 * 60)) % 60;
      long h = (millis / (1000 * 60 * 60)) % 24;
      String time = String.format("%02d:%02d:%02d.%02d", h, m, s, cs);
      return time;
   }

   public static int intWidth(int n) {
      if (n == 0) {
         return 1;
      }
      else {
         return 32 - Integer.numberOfLeadingZeros(n);
      }
   }

   public static boolean isLoopback(String interfaceName) {
      return (interfaceName.startsWith("Loopback") || interfaceName
            .startsWith("lo"));
   }

   public static boolean isNullInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("null");
   }

   public static boolean isValidWildcard(Ip wildcard) {
      long w = wildcard.asLong();
      long wp = w + 1l;
      int numTrailingZeros = Long.numberOfTrailingZeros(wp);
      long check = 1l << numTrailingZeros;
      return wp == check;
   }

   public static String longToCommunity(Long l) {
      Long upper = l >> 16;
      Long lower = l & 0xFFFF;
      return upper.toString() + ":" + lower;
   }

   public static long numWildcardBitsToWildcardLong(int numBits) {
      long wildcard = 0;
      for (int i = 0; i < numBits; i++) {
         wildcard |= (1 << i);
      }
      return wildcard;
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

   /**
    * Unescapes a string that contains standard Java escape sequences.
    * <ul>
    * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
    * BS, FF, NL, CR, TAB, double and single quote.</li>
    * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
    * specification (0 - 377, 0x00 - 0xFF).</li>
    * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
    * </ul>
    *
    * @param st
    *           A string optionally containing standard java escape sequences.
    * @return The translated string.
    */
   public static String unescapeJavaString(String st) {
      if (st == null) {
         return null;
      }
      StringBuilder sb = new StringBuilder(st.length());
      for (int i = 0; i < st.length(); i++) {
         char ch = st.charAt(i);
         if (ch == '\\') {
            char nextChar = (i == st.length() - 1) ? '\\' : st.charAt(i + 1);
            // Octal escape?
            if (nextChar >= '0' && nextChar <= '7') {
               String code = "" + nextChar;
               i++;
               if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                     && st.charAt(i + 1) <= '7') {
                  code += st.charAt(i + 1);
                  i++;
                  if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                        && st.charAt(i + 1) <= '7') {
                     code += st.charAt(i + 1);
                     i++;
                  }
               }
               sb.append((char) Integer.parseInt(code, 8));
               continue;
            }
            switch (nextChar) {
            case '\\':
               ch = '\\';
               break;
            case 'b':
               ch = '\b';
               break;
            case 'f':
               ch = '\f';
               break;
            case 'n':
               ch = '\n';
               break;
            case 'r':
               ch = '\r';
               break;
            case 't':
               ch = '\t';
               break;
            case '\"':
               ch = '\"';
               break;
            case '\'':
               ch = '\'';
               break;
            // Hex Unicode: u????
            case 'u':
               if (i >= st.length() - 5) {
                  ch = 'u';
                  break;
               }
               int code = Integer.parseInt(
                     "" + st.charAt(i + 2) + st.charAt(i + 3)
                           + st.charAt(i + 4) + st.charAt(i + 5), 16);
               sb.append(Character.toChars(code));
               i += 5;
               continue;
            }
            i++;
         }
         sb.append(ch);
      }
      return sb.toString();
   }

   private Util() {
   }

}
