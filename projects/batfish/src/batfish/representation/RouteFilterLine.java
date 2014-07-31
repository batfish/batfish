package batfish.representation;

import java.io.Serializable;

public abstract class RouteFilterLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   public RouteFilterLine(LineAction action) {
      _action = action;
   }

   public LineAction getAction() {
      return _action;
   }

   public abstract String getIFString(int indentLevel);

   public abstract RouteFilterLineType getType();

   public abstract boolean sameParseTree(RouteFilterLine routeFilterLine);

}
