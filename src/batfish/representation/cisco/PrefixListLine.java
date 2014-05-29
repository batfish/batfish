package batfish.representation.cisco;

import batfish.representation.LineAction;
import batfish.util.SubRange;

public class PrefixListLine {
   
   private String _prefix;
   
   private int _prefixLength;
   
   private SubRange _lengthRange;

   private LineAction _action;

   public PrefixListLine(LineAction action, String prefix, int prefixLength, SubRange lengthRange) {
      _action = action;
      _prefix = prefix;
      _prefixLength = prefixLength;
      _lengthRange = lengthRange;
   }
   
   public String getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public SubRange getLengthRange() {
      return _lengthRange;
   }

   public LineAction getAction() {
      return _action;
   }

}
