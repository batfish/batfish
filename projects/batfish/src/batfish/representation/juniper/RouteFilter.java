package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import batfish.util.NamedStructure;

public final class RouteFilter extends NamedStructure implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final List<RouteFilterLine> _lines;

   public RouteFilter(String name) {
      super(name);
      _lines = new ArrayList<RouteFilterLine>();
   }

   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
