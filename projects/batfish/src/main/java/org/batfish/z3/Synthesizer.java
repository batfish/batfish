package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.batfish.z3.expr.DeclareRelExpr;
import org.batfish.z3.expr.DeclareVarExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.expr.visitors.RelationCollector;
import org.batfish.z3.expr.visitors.Simplifier;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeTransit;
import org.batfish.z3.state.Originate;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostIn;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutInterface;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOut;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutInterface;
import org.batfish.z3.state.Query;
import org.batfish.z3.state.State;
import org.batfish.z3.state.Transition;

public class Synthesizer {

  public static Map<String, FuncDecl> getRelDeclFuncDecls(
      List<Statement> existingStatements, Context ctx) throws Z3Exception {
    return ImmutableSet.<String>builder()
        .addAll(
            existingStatements
                .stream()
                .map(RelationCollector::collectRelations)
                .flatMap(Collection::stream)
                .collect(ImmutableSet.toImmutableSet()))
        .add(Query.NAME)
        .build()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(), packetRel -> new DeclareRelExpr(packetRel).toFuncDecl(ctx)));
  }

  public static List<Statement> getVarDeclExprs() {
    return Arrays.stream(HeaderField.values())
        .map(DeclareVarExpr::new)
        .collect(ImmutableList.toImmutableList());
  }

  public static String indent(int n) {
    String output = "";
    for (int i = 0; i < n; i++) {
      output += "   ";
    }
    return output;
  }

  private final SynthesizerInput _input;

  private List<String> _warnings;

  public Synthesizer(SynthesizerInput input) {
    _input = input;
    _warnings = new ArrayList<>();
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public NodProgram synthesizeNodAclProgram(String hostname, String aclName, Context ctx)
      throws Z3Exception {
    Map<String, Set<Class<? extends Transition<?>>>> disabledTransitions =
        _input.getDisabledTransitions();
    List<Statement> rules =
        ImmutableSet.of(
                AclDeny.INSTANCE,
                AclLineMatch.INSTANCE,
                AclLineNoMatch.INSTANCE,
                AclPermit.INSTANCE)
            .stream()
            .flatMap(
                state ->
                    state
                        .getEnabledTransitions(disabledTransitions.get(state))
                        .stream()
                        .map(transition -> transition.generate(_input))
                        .flatMap(Collection::stream))
            .collect(ImmutableList.toImmutableList());
    return synthesizeNodProgram(ctx, rules);
  }

  public NodProgram synthesizeNodDataPlaneProgram(Context ctx) throws Z3Exception {
    Map<String, Set<Class<? extends Transition<?>>>> disabledTransitions =
        _input.getDisabledTransitions();
    Set<State<?, ?>> states =
        ImmutableSet.of(
            Accept.INSTANCE,
            AclDeny.INSTANCE,
            AclLineMatch.INSTANCE,
            AclLineNoMatch.INSTANCE,
            AclPermit.INSTANCE,
            Drop.INSTANCE,
            DropAcl.INSTANCE,
            DropAclIn.INSTANCE,
            DropAclOut.INSTANCE,
            DropNoRoute.INSTANCE,
            DropNullRoute.INSTANCE,
            NodeAccept.INSTANCE,
            NodeDrop.INSTANCE,
            NodeDropAcl.INSTANCE,
            NodeDropAclIn.INSTANCE,
            NodeDropAclOut.INSTANCE,
            NodeDropNoRoute.INSTANCE,
            NodeDropNullRoute.INSTANCE,
            NodeTransit.INSTANCE,
            Originate.INSTANCE,
            OriginateVrf.INSTANCE,
            PostIn.INSTANCE,
            PostInInterface.INSTANCE,
            PostInVrf.INSTANCE,
            PostOutInterface.INSTANCE,
            PreInInterface.INSTANCE,
            PreOut.INSTANCE,
            PreOutEdge.INSTANCE,
            PreOutInterface.INSTANCE);
    List<Statement> rules =
        states
            .stream()
            .flatMap(
                state ->
                    state
                        .getEnabledTransitions(disabledTransitions.get(state))
                        .stream()
                        .map(transition -> transition.generate(_input))
                        .flatMap(Collection::stream))
            .collect(ImmutableList.toImmutableList());
    return synthesizeNodProgram(ctx, rules);
  }

  private NodProgram synthesizeNodProgram(Context ctx, List<Statement> ruleStatements) {
    NodProgram nodProgram = new NodProgram(ctx);
    Map<String, FuncDecl> relDeclFuncDecls = getRelDeclFuncDecls(ruleStatements, ctx);
    nodProgram.getRelationDeclarations().putAll(relDeclFuncDecls);
    Map<HeaderField, BitVecExpr> variables = nodProgram.getVariables();
    Map<HeaderField, BitVecExpr> variablesAsConsts = nodProgram.getVariablesAsConsts();
    int deBruinIndex = 0;
    for (HeaderField headerField : HeaderField.values()) {
      int size = headerField.getSize();
      BitVecExpr varExpr = (BitVecExpr) ctx.mkBound(deBruinIndex, ctx.mkBitVecSort(size));
      BitVecExpr varAsConstExpr =
          (BitVecExpr) ctx.mkConst(headerField.name(), ctx.mkBitVecSort(size));
      variables.put(headerField, varExpr);
      variablesAsConsts.put(headerField, varAsConstExpr);
      deBruinIndex++;
    }
    List<BoolExpr> rules = nodProgram.getRules();
    for (Statement rawStatement : ruleStatements) {
      Expr statement;
      if (_input.getSimplify()) {
        statement = Simplifier.simplifyStatement(rawStatement);
      } else {
        statement = rawStatement;
      }
      if (statement instanceof RuleExpr) {
        RuleExpr ruleExpr = (RuleExpr) statement;
        BoolExpr ruleBoolExpr =
            BoolExprTransformer.toBoolExpr(ruleExpr.getSubExpression(), nodProgram);
        rules.add(ruleBoolExpr);
      }
    }
    return nodProgram;
  }
}
