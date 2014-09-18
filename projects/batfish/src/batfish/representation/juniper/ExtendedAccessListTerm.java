package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.LineAction;
import batfish.util.SubRange;

public class ExtendedAccessListTerm {

   private LineAction _ala;
   private List<String> _destinationAddress;
   private List<SubRange> _dstPortRanges;
   private List<Integer> _protocols;
   private List<String> _sourceAddress;
   private List<SubRange> _srcPortRanges;
   private String _tName;

   public ExtendedAccessListTerm(String tname) {
      _tName = tname;
      _ala = null;
      _protocols = new ArrayList<Integer>();
      _destinationAddress = new ArrayList<String>();
      _sourceAddress = new ArrayList<String>();
      _srcPortRanges = new ArrayList<SubRange>();
      _dstPortRanges = new ArrayList<SubRange>();
   }

   public void addDestinationAddress(String d) {
      _destinationAddress.add(d);
   }

   public void addDstPortRange(SubRange p) {
      _dstPortRanges.add(p);
   }

   public void addProtocol(Integer p) {
      _protocols.add(p);
   }

   public void addSourceAddress(String s) {
      _sourceAddress.add(s);
   }

   public void addSrcPortRange(SubRange p) {
      _srcPortRanges.add(p);
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
         throw new Error ("bad subnet value");
      }

      return result;
   }

   public List<String> getDestinationAddress() {
      return _destinationAddress;
   }

   public List<SubRange> getDstPortRanges() {
      return _dstPortRanges;
   }

   public LineAction getLineAction() {
      return _ala;
   }

   public List<Integer> getProtocols() {
      return _protocols;
   }

   public List<String> getSourceAddress() {
      return _sourceAddress;
   }

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public String getTermName() {
      return _tName;
   }

   public void processTerm() {
      List<ExtendedAccessListLine> lines = new ArrayList<ExtendedAccessListLine>();
      String da, sa, dw, sw;
      int pr;

      if (_protocols.isEmpty()) {
         _protocols.add(0);
      }

      if (_destinationAddress.isEmpty()) {
         _destinationAddress.add("0.0.0.0/0");
      }

      if (_sourceAddress.isEmpty()) {
         _sourceAddress.add("0.0.0.0/0");
      }

      for (Integer p : _protocols) {
         pr = p;
         for (String s : _sourceAddress) {
            String[] tmpS = s.split("/");
            sa = tmpS[0];
            sw = convertInvertedSubnet(tmpS[1]);
            for (String d : _destinationAddress) {
               String[] tmpD = d.split("/");
               da = tmpD[0];
               dw = convertInvertedSubnet(tmpD[1]);
               ExtendedAccessListLine line = new ExtendedAccessListLine(_ala,
                     pr, sa, sw, da, dw, _srcPortRanges, _dstPortRanges);
               line.setID(_tName);
               lines.add(line);
            }
         }
      }

   }

   public void setLineAction(LineAction a) {
      _ala = a;
   }

}
