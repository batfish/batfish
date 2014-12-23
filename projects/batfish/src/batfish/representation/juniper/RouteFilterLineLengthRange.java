package batfish.representation.juniper;

import batfish.representation.LineAction;
import batfish.representation.Prefix;
import batfish.representation.RouteFilterLengthRangeLine;
import batfish.representation.RouteFilterList;
import batfish.util.SubRange;

public final class RouteFilterLineLengthRange extends RouteFilterLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _maxPrefixLength;

   private final int _minPrefixLength;

   public RouteFilterLineLengthRange(Prefix prefix, int minPrefixLength,
         int maxPrefixLength) {
      super(prefix);
      _minPrefixLength = minPrefixLength;
      _maxPrefixLength = maxPrefixLength;
   }

   @Override
   public void applyTo(RouteFilterList rfl) {
      int prefixLength = _prefix.getPrefixLength();
      RouteFilterLengthRangeLine line = new RouteFilterLengthRangeLine(
            LineAction.ACCEPT, _prefix.getAddress(), prefixLength,
            new SubRange(_minPrefixLength, _maxPrefixLength));
      rfl.addLine(line);

   }

   public int getMaxPrefixLength() {
      return _maxPrefixLength;
   }

   public int getMinPrefixLength() {
      return _minPrefixLength;
   }

}
