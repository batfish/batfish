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
import java.util.function.Function;
import org.batfish.z3.expr.DeclareRelStatement;
import org.batfish.z3.expr.DeclareVarStatement;
import org.batfish.z3.expr.RuleStatement;
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
import org.batfish.z3.state.visitors.DefaultTransitionGenerator;

public class Synthesizer {

  public static List<Statement> getVarDeclExprs() {
    return Arrays.stream(HeaderField.values())
        .map(DeclareVarStatement::new)
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

  public SynthesizerInput getInput() {
    return _input;
  }

  public Map<String, FuncDecl> getRelDeclFuncDecls(List<Statement> existingStatements, Context ctx)
      throws Z3Exception {
    return ImmutableSet.<String>builder()
        .addAll(
            existingStatements
                .stream()
                .map(s -> RelationCollector.collectRelations(_input, s))
                .flatMap(Collection::stream)
                .collect(ImmutableSet.toImmutableSet()))
        .add(BoolExprTransformer.getNodName(_input, Query.INSTANCE))
        .build()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                packetRel -> new DeclareRelStatement(packetRel).toFuncDecl(ctx)));
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public NodProgram synthesizeNodAclProgram(String hostname, String aclName, Context ctx)
      throws Z3Exception {
    return synthesizeNodProgram(
        ctx,
        ImmutableList.<Statement>copyOf(
            DefaultTransitionGenerator.generateTransitions(
                _input,
                ImmutableSet.of(
                    AclDeny.State.INSTANCE,
                    AclLineMatch.State.INSTANCE,
                    AclLineNoMatch.State.INSTANCE,
                    AclPermit.State.INSTANCE))));
  }

  public NodProgram synthesizeNodDataPlaneProgram(Context ctx) throws Z3Exception {
    return synthesizeNodProgram(
        ctx,
        ImmutableList.<Statement>copyOf(
            DefaultTransitionGenerator.generateTransitions(
                _input,
                ImmutableSet.of(
                    Accept.State.INSTANCE,
                    AclDeny.State.INSTANCE,
                    AclLineMatch.State.INSTANCE,
                    AclLineNoMatch.State.INSTANCE,
                    AclPermit.State.INSTANCE,
                    Drop.State.INSTANCE,
                    DropAcl.State.INSTANCE,
                    DropAclIn.State.INSTANCE,
                    DropAclOut.State.INSTANCE,
                    DropNoRoute.State.INSTANCE,
                    DropNullRoute.State.INSTANCE,
                    NodeAccept.State.INSTANCE,
                    NodeDrop.State.INSTANCE,
                    NodeDropAcl.State.INSTANCE,
                    NodeDropAclIn.State.INSTANCE,
                    NodeDropAclOut.State.INSTANCE,
                    NodeDropNoRoute.State.INSTANCE,
                    NodeDropNullRoute.State.INSTANCE,
                    NodeTransit.State.INSTANCE,
                    Originate.State.INSTANCE,
                    OriginateVrf.State.INSTANCE,
                    PostIn.State.INSTANCE,
                    PostInInterface.State.INSTANCE,
                    PostInVrf.State.INSTANCE,
                    PostOutInterface.State.INSTANCE,
                    PreInInterface.State.INSTANCE,
                    PreOut.State.INSTANCE,
                    PreOutEdge.State.INSTANCE,
                    PreOutInterface.State.INSTANCE))));
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
      Statement statement;
      if (_input.getSimplify()) {
        statement = Simplifier.simplifyStatement(rawStatement);
      } else {
        statement = rawStatement;
      }
      if (statement instanceof RuleStatement) {
        RuleStatement ruleStatement = (RuleStatement) statement;
        BoolExpr ruleBoolExpr =
            BoolExprTransformer.toBoolExpr(ruleStatement.getSubExpression(), _input, nodProgram);
        rules.add(ruleBoolExpr);
      }
    }
    return nodProgram;
  }
}
