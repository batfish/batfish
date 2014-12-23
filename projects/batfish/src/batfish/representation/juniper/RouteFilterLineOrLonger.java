package batfish.representation.juniper;

import batfish.representation.LineAction;
import batfish.representation.Prefix;
import batfish.representation.RouteFilterLengthRangeLine;
import batfish.representation.RouteFilterList;
import batfish.util.SubRange;

public class RouteFilterLineOrLonger extends RouteFilterLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public RouteFilterLineOrLonger(Prefix prefix) {
      super(prefix);
   }

   @Override
   public void applyTo(RouteFilterList rfl) {
      int prefixLength = _prefix.getPrefixLength();
      RouteFilterLengthRangeLine line = new RouteFilterLengthRangeLine(
            LineAction.ACCEPT, _prefix.getAddress(), prefixLength,
            new SubRange(prefixLength, 32));
      rfl.addLine(line);
   }

}
