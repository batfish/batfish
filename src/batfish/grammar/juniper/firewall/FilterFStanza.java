package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.juniper.ExtendedAccessList;
import batfish.representation.juniper.ExtendedAccessListLine;
import batfish.util.SubRange;

public class FilterFStanza {
   private ExtendedAccessList _filter;

   public FilterFStanza(String n) {
      _filter = new ExtendedAccessList(n);
   }

   public void processTerm(TermFFStanza ts) {
      List<String> daList = ts.getDestinationAddress();
      List<String> saList = ts.getSourceAddress();
      List<String> seaList = ts.getSourceExceptAddress();
      List<SubRange> dpoList = ts.getPorts();
      List<SubRange> spoList = ts.getSourcePorts();
      List<Integer> prList = ts.getProtocols();
      ExtendedAccessListLine line;
      String da, sa, dw, sw;
      int pr;

      // Check if the line Action is accept or reject
      if (ts.getLineAction() != null) {

         if (prList == null) {
            prList = new ArrayList<Integer>();
            prList.add(0);
         }

         if (daList == null) {
            daList = new ArrayList<String>();
            daList.add("0.0.0.0/0");
         }

         if (saList == null) {
            saList = new ArrayList<String>();
            saList.add("0.0.0.0/0");
         }
         
         if (seaList == null) {
            seaList = new ArrayList<String>();
         }

         for (Integer p : prList) {
            pr = p;
            for (String s : saList) {
               String[] tmpS = s.split("/");
               sa = tmpS[0];
               sw = convertInvertedSubnet(tmpS[1]);
               for (String d : daList) {
                  String[] tmpD = d.split("/");
                  da = tmpD[0];
                  dw = convertInvertedSubnet(tmpD[1]);
                  LineAction la = ts.getLineAction();
                  if (seaList.contains(s)){
                     if(la == LineAction.ACCEPT){
                        la = LineAction.REJECT;
                     }else if(la == LineAction.REJECT){
                        la = LineAction.ACCEPT;
                     }
                  }
                  line = new ExtendedAccessListLine(la, pr, sa,
                        sw, da, dw, spoList, dpoList); 
                  _filter.addLine(line);
               }
            }
         }
      }

   }

   public ExtendedAccessList getFilter() {
      return _filter;
   }

   private String convertInvertedSubnet(String s) {
      String result = "";
      int sval = Integer.parseInt(s);
      sval = 32 - sval;
      if ((sval >= 0) && (sval <= 8)) {

         if (sval == 0) {
            result += "0";
         }
         else if (sval == 1) {
            result += "128";
         }
         else if (sval == 2) {
            result += "192";
         }
         else if (sval == 3) {
            result += "224";
         }
         else if (sval == 4) {
            result += "240";
         }
         else if (sval == 5) {
            result += "248";
         }
         else if (sval == 6) {
            result += "252";
         }
         else if (sval == 7) {
            result += "254";
         }
         else if (sval == 8) {
            result += "255";
         }
         result += ".0.0.0";

      }
      else if ((sval >= 9) && (sval <= 16)) {

         result += "255.";
         if (sval == 9) {
            result += "128";
         }
         else if (sval == 10) {
            result += "192";
         }
         else if (sval == 11) {
            result += "224";
         }
         else if (sval == 12) {
            result += "240";
         }
         else if (sval == 13) {
            result += "248";
         }
         else if (sval == 14) {
            result += "252";
         }
         else if (sval == 15) {
            result += "254";
         }
         else if (sval == 16) {
            result += "255";
         }
         result += ".0.0";

      }
      else if ((sval >= 17) && (sval <= 24)) {

         result += "255.255.";
         if (sval == 17) {
            result += "128";
         }
         else if (sval == 18) {
            result += "192";
         }
         else if (sval == 19) {
            result += "224";
         }
         else if (sval == 20) {
            result += "240";
         }
         else if (sval == 21) {
            result += "248";
         }
         else if (sval == 22) {
            result += "252";
         }
         else if (sval == 23) {
            result += "254";
         }
         else if (sval == 24) {
            result += "255";
         }
         result += ".0";

      }
      else if ((sval >= 25) && (sval <= 32)) {
         result += "255.255.255.";
         if (sval == 25) {
            result += "128";
         }
         else if (sval == 26) {
            result += "192";
         }
         else if (sval == 27) {
            result += "224";
         }
         else if (sval == 28) {
            result += "240";
         }
         else if (sval == 29) {
            result += "248";
         }
         else if (sval == 30) {
            result += "252";
         }
         else if (sval == 31) {
            result += "254";
         }
         else if (sval == 32) {
            result += "255";
         }

      }
      else {
         System.out.println("bad subnet value");
      }

      return result;
   }

}
