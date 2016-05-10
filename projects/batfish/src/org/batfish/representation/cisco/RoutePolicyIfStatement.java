package org.batfish.representation.cisco;

import java.util.List;

public class RoutePolicyIfStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyElseBlock _elseBlock;
   private List<RoutePolicyElseIfBlock> _elseIfBlocks;
   private RoutePolicyBoolean _guard;
   private List<RoutePolicyStatement> _stmtList;

   public RoutePolicyIfStatement(RoutePolicyBoolean guard,
         List<RoutePolicyStatement> stmtList,
         List<RoutePolicyElseIfBlock> elseIfBlocks,
         RoutePolicyElseBlock elseBlock) {
      _guard = guard;
      _stmtList = stmtList;
      _elseIfBlocks = elseIfBlocks;
      _elseBlock = elseBlock;
   }

   public void addStatement(RoutePolicyStatement stmt) {
      _stmtList.add(stmt);
   }

   public RoutePolicyElseBlock getElseBlock() {
      return _elseBlock;
   }

   public List<RoutePolicyElseIfBlock> getElseIfBlocks() {
      return _elseIfBlocks;
   }

   public RoutePolicyBoolean getGuard() {
      return _guard;
   }

   public List<RoutePolicyStatement> getStatements() {
      return _stmtList;
   }

   @Override
   public RoutePolicyStatementType getType() {
      return RoutePolicyStatementType.IF;
   }

}
