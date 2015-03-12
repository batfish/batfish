package org.batfish.util;

import org.antlr.v4.runtime.ParserRuleContext;
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
      if (ifaceNameLower.startsWith(prefix)) {
         String vlanStr = ifaceNameLower.substring(prefix.length());
         return Integer.parseInt(vlanStr);
      }
      return null;
   }

   public static String getText(ParserRuleContext ctx, String srcText) {
      int start = ctx.start.getStartIndex();
      int stop = ctx.stop.getStopIndex();
      return srcText.substring(start, stop);
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

   private Util() {
   }

}
