package batfish.grammar.juniper.policy_options;

import batfish.representation.juniper.RouteFilter;
import batfish.representation.juniper.RouteFilterLine;
import batfish.representation.juniper.RouteFilterSubRangeLine;
import batfish.util.SubRange;

public class PrefixListPOStanza extends POStanza {
   private RouteFilter _list;
   private boolean _isIPv6;

   public PrefixListPOStanza(String n) {
      _list = new RouteFilter(n);
      _isIPv6 = false;
   }

   public void addAddress(String ipmask) {
      String[] tmp = ipmask.split("/");
      int mask = Integer.parseInt(tmp[1]);
      RouteFilterLine tmpl = new RouteFilterSubRangeLine(tmp[0], mask,
            new SubRange(mask, mask));
      _list.addLine(tmpl);
   }

   public void setBool(boolean i) {
      _isIPv6 = i;
   }

   public RouteFilter getRouteFilter() {
      return _list;
   }

   public boolean isIPv6() {
      return _isIPv6;
   }

   @Override
   public POType getType() {
      return POType.PREFIX_LIST;
   }

}
