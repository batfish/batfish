package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.expr.visitors.RelationCollector;
import org.batfish.z3.expr.visitors.VariableSizeCollector;
import org.batfish.z3.state.visitors.FuncDeclTransformer;

public class NodContext {

  private final List<BitVecSort> _basicStateVariableSorts;

  private final VarIntExpr[] _basicStateVarIntExprs;

  private final ImmutableList<String> _variableNames;

  private Context _context;

  private final Map<String, FuncDecl> _relationDeclarations;

  private final Map<String, BitVecExpr> _variables;

  private final Map<String, BitVecExpr> _variablesAsConsts;

  private final Map<String, BitVecExpr> _transformedVariables;

  public NodContext(Context ctx, ReachabilityProgram... programs) {
    _context = ctx;
    AtomicInteger deBruijnIndex = new AtomicInteger(0);
    Map<String, Integer> variableSizes = VariableSizeCollector.collectVariableSizes(programs);
    _variableNames = computeVariableNames(variableSizes);

    Map<String, BitVecSort> variableSorts =
        variableSizes
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey,
                    variableSizeEntry -> ctx.mkBitVecSort(variableSizeEntry.getValue())));
    _variables =
        _variableNames
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    variableName ->
                        (BitVecExpr)
                            ctx.mkBound(
                                deBruijnIndex.getAndIncrement(), variableSorts.get(variableName))));

    _transformedVariables =
        _variableNames
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    variableName ->
                        (BitVecExpr)
                            ctx.mkBound(
                                deBruijnIndex.getAndIncrement(), variableSorts.get(variableName))));

    _variablesAsConsts =
        variableSizes
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey,
                    variableSizeEntry ->
                        ctx.mkBVConst(variableSizeEntry.getKey(), variableSizeEntry.getValue())));

    ImmutableMap<String, VarIntExpr> varIntExprs =
        variableSizes
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey,
                    variableSizeEntry ->
                        new VarIntExpr(variableSizeEntry.getKey(), variableSizeEntry.getValue())));
    _basicStateVarIntExprs =
        _variableNames.stream().map(varIntExprs::get).toArray(VarIntExpr[]::new);

    _basicStateVariableSorts =
        _variableNames.stream().map(variableSorts::get).collect(Collectors.toList());

    FuncDeclTransformer funcDeclTransformer =
        new FuncDeclTransformer(ctx, _basicStateVariableSorts);
    _relationDeclarations =
        Arrays.stream(programs)
            .flatMap(
                program ->
                    Streams.concat(program.getRules().stream(), program.getQueries().stream())
                        .map(
                            statement ->
                                RelationCollector.collectRelations(program.getInput(), statement))
                        .map(Map::entrySet)
                        .flatMap(Collection::stream))
            .collect(ImmutableSet.toImmutableSet())
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey,
                    relationsEntry ->
                        funcDeclTransformer.toFuncDecl(
                            relationsEntry.getKey(), relationsEntry.getValue())));
  }

  private static ImmutableList<String> computeVariableNames(Map<String, Integer> variableSizes) {
    return ImmutableList.sortedCopyOf(variableSizes.keySet());
  }

  public List<BitVecSort> getBasicStateVariableSorts() {
    return _basicStateVariableSorts;
  }

  public Context getContext() {
    return _context;
  }

  public Map<String, FuncDecl> getRelationDeclarations() {
    return _relationDeclarations;
  }

  public Map<String, BitVecExpr> getVariables() {
    return _variables;
  }

  public Map<String, BitVecExpr> getVariablesAsConsts() {
    return _variablesAsConsts;
  }

  public VarIntExpr[] getBasicStateVarIntExprs() {
    return _basicStateVarIntExprs;
  }

  public ImmutableList<String> getVariableNames() {
    return _variableNames;
  }

  public Map<String, BitVecExpr> getTransformedVariables() {
    return _transformedVariables;
  }
}
