package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class RoutePolicyElseIfBlock implements Serializable {

   private static final long serialVersionUID = 1L;

   private RoutePolicyBoolean _guard;
   private List<RoutePolicyStatement> _stmtList;

   public RoutePolicyElseIfBlock(RoutePolicyBoolean guard, List<RoutePolicyStatement> stmtList) {
   	_guard = guard;
      _stmtList = stmtList;
   }

   public RoutePolicyBoolean getGuard() { return _guard; }

   public void addStatement(RoutePolicyStatement stmt) {
      _stmtList.add(stmt);
   }

   public List<RoutePolicyStatement> getStatements() {
      return _stmtList;
   }

}
