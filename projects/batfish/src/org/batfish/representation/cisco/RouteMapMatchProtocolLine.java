package org.batfish.representation.cisco;

import java.util.List;

public class RouteMapMatchProtocolLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private List<String> _protocol;

   public RouteMapMatchProtocolLine(List<String> protocol) {
      _protocol = protocol;
   }

   public List<String> getProtocl() {
      return _protocol;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.PROTOCOL;
   }

}
