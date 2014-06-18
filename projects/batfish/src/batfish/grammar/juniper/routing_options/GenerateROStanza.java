package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.GenerateRoute;

public class GenerateROStanza extends ROStanza {
   private List<GenerateRoute> _routes;
   
   public GenerateROStanza(){
      _routes = new ArrayList<GenerateRoute>();
   }
   
   public void processRoute(RouteGROStanza rgros){
      GenerateRoute gr = new GenerateRoute(rgros.getPrefix(), rgros.getPrefixLength(), rgros.getPolicy(), 130);
      _routes.add(gr);
   }
   
   public List<GenerateRoute> getRoutes(){
      return _routes;
   }

   @Override
   public ROType getType() {
      return ROType.GENERATE;
   }

}
