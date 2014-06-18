package batfish.representation;

public abstract class RouteFilterLine {
   
   private LineAction _action;

   public RouteFilterLine(LineAction action) {
      _action = action;
   }

   public LineAction getAction() {
      return _action;
   }

   public abstract RouteFilterLineType getType();

   public abstract String getIFString(int indentLevel);

   public abstract boolean sameParseTree(RouteFilterLine routeFilterLine);
   
}
