package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Prefix6;

public class BgpNetwork6 extends ComparableStructure<Prefix6> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Integer _routeMapLine;

   private final String _routeMapName;

   public BgpNetwork6(Prefix6 name, String routeMapName, Integer routeMapLine) {
      super(name);
      _routeMapName = routeMapName;
      _routeMapLine = routeMapLine;
   }

   public Integer getRouteMapLine() {
      return _routeMapLine;
   }

   public String getRouteMapName() {
      return _routeMapName;
   }

}
