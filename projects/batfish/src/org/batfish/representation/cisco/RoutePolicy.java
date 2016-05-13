package org.batfish.representation.cisco;

import java.util.List;
import java.util.ArrayList;

import org.batfish.common.util.ComparableStructure;

public class RoutePolicy extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private List<RoutePolicyStatement> _stmtList;

   public RoutePolicy(String name) {
      super(name);
      _stmtList = new ArrayList<RoutePolicyStatement>();
   }

   public List<RoutePolicyStatement> getStatements() {
      return _stmtList;
   }

}
