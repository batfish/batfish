package org.batfish.representation.cisco_xr;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class RoutePolicyIfStatement extends RoutePolicyStatement {

  private RoutePolicyElseBlock _elseBlock;

  private @Nonnull List<RoutePolicyElseIfBlock> _elseIfBlocks;

  private RoutePolicyBoolean _guard;

  private @Nonnull List<RoutePolicyStatement> _stmtList;

  public RoutePolicyIfStatement() {
    _stmtList = ImmutableList.of();
    _elseIfBlocks = ImmutableList.of();
  }

  @VisibleForTesting
  public RoutePolicyIfStatement(
      RoutePolicyBoolean guard,
      List<RoutePolicyStatement> stmtList,
      List<RoutePolicyElseIfBlock> elseIfBlocks,
      @Nullable RoutePolicyElseBlock elseBlock) {
    _guard = guard;
    _stmtList = stmtList;
    _elseIfBlocks = elseIfBlocks;
    _elseBlock = elseBlock;
  }

  public void addStatement(RoutePolicyStatement stmt) {
    _stmtList =
        ImmutableList.<RoutePolicyStatement>builderWithExpectedSize(_stmtList.size() + 1)
            .addAll(_stmtList)
            .add(stmt)
            .build();
  }

  public void addElseIfBlock(RoutePolicyElseIfBlock elseIfBlock) {
    _elseIfBlocks =
        ImmutableList.<RoutePolicyElseIfBlock>builderWithExpectedSize(_elseIfBlocks.size() + 1)
            .addAll(_elseIfBlocks)
            .add(elseIfBlock)
            .build();
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    assert _guard != null;
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
      assert elseIfBlock.getGuard() != null;
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

  public @Nullable RoutePolicyElseBlock getElseBlock() {
    return _elseBlock;
  }

  public void setElseBlock(@Nullable RoutePolicyElseBlock elseBlock) {
    _elseBlock = elseBlock;
  }

  public @Nonnull List<RoutePolicyElseIfBlock> getElseIfBlocks() {
    return _elseIfBlocks;
  }

  public @Nullable RoutePolicyBoolean getGuard() {
    return _guard;
  }

  public void setGuard(RoutePolicyBoolean guard) {
    _guard = guard;
  }

  public @Nonnull List<RoutePolicyStatement> getStatements() {
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
        && Objects.equals(_guard, that._guard)
        && _stmtList.equals(that._stmtList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_elseBlock, _elseIfBlocks, _guard, _stmtList);
  }
}
