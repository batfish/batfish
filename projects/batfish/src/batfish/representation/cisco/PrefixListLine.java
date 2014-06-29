package batfish.representation.cisco;

import batfish.representation.Ip;
import batfish.representation.LineAction;
import batfish.util.SubRange;

public class PrefixListLine {
   
   private LineAction _action;
   
   private SubRange _lengthRange;
   
   private Ip _prefix;

   private int _prefixLength;

   public PrefixListLine(LineAction action, Ip prefix, int prefixLength, SubRange lengthRange) {
      _action = action;
      _prefix = prefix;
      _prefixLength = prefixLength;
      _lengthRange = lengthRange;
   }
   
   public LineAction getAction() {
      return _action;
   }

   public SubRange getLengthRange() {
      return _lengthRange;
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

}
