package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class RoutePolicyIfStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyBoolean _guard;
   private List<RoutePolicyStatement> _stmtList;
   private List<RoutePolicyElseIfBlock> _elseIfBlocks;
   private RoutePolicyElseBlock _elseBlock;

   public RoutePolicyIfStatement(RoutePolicyBoolean guard, 
   	List<RoutePolicyStatement> stmtList,
   	List<RoutePolicyElseIfBlock> elseIfBlocks, RoutePolicyElseBlock elseBlock) {
   	_guard = guard;
    _stmtList = stmtList;
    _elseIfBlocks = elseIfBlocks;
    _elseBlock = elseBlock;
   }

   public RoutePolicyBoolean getGuard() { return _guard; }

   public void addStatement(RoutePolicyStatement stmt) {
      _stmtList.add(stmt);
   }

   public List<RoutePolicyStatement> getStatements() {
      return _stmtList;
   }

   public List<RoutePolicyElseIfBlock> getElseIfBlocks() { return _elseIfBlocks; }
   public RoutePolicyElseBlock getElseBlock() { return _elseBlock; }

   public RoutePolicyStatementType getType() { return RoutePolicyStatementType.IF; }

}
