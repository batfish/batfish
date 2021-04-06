package org.batfish.representation.cisco_xr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class RoutePolicyIfStatement extends RoutePolicyStatement {

  private RoutePolicyElseBlock _elseBlock;

  private List<RoutePolicyElseIfBlock> _elseIfBlocks;

  private RoutePolicyBoolean _guard;

  private List<RoutePolicyStatement> _stmtList;

  public RoutePolicyIfStatement(
      RoutePolicyBoolean guard,
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

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    If mainIf = new If();
    mainIf.setGuard(_guard.toBooleanExpr(cc, c, w));
    If currentIf = mainIf;
    List<Statement> mainIfStatements = new ArrayList<>();
    mainIf.setTrueStatements(mainIfStatements);
    for (RoutePolicyStatement stmt : _stmtList) {
      stmt.applyTo(mainIfStatements, cc, c, w);
    }
    for (RoutePolicyElseIfBlock elseIfBlock : _elseIfBlocks) {
      If elseIf = new If();
      elseIf.setGuard(elseIfBlock.getGuard().toBooleanExpr(cc, c, w));
      List<Statement> elseIfStatements = new ArrayList<>();
      elseIf.setTrueStatements(elseIfStatements);
      for (RoutePolicyStatement stmt : elseIfBlock.getStatements()) {
        stmt.applyTo(elseIfStatements, cc, c, w);
      }
      currentIf.setFalseStatements(Collections.singletonList(elseIf));
      currentIf = elseIf;
    }
    List<Statement> elseStatements = new ArrayList<>();
    currentIf.setFalseStatements(elseStatements);
    if (_elseBlock != null) {
      for (RoutePolicyStatement stmt : _elseBlock.getStatements()) {
        stmt.applyTo(elseStatements, cc, c, w);
      }
    }
    statements.add(mainIf);
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutePolicyIfStatement)) {
      return false;
    }
    RoutePolicyIfStatement that = (RoutePolicyIfStatement) o;
    return Objects.equals(_elseBlock, that._elseBlock)
        && _elseIfBlocks.equals(that._elseIfBlocks)
        && _guard.equals(that._guard)
        && _stmtList.equals(that._stmtList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_elseBlock, _elseIfBlocks, _guard, _stmtList);
  }
}
