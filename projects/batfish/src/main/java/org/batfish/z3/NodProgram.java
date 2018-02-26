package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.batfish.z3.expr.visitors.BoolExprTransformer;

public class NodProgram {

  private final NodContext _context;

  private final List<BoolExpr> _queries;

  private final List<BoolExpr> _rules;

  public NodProgram(Context ctx, ReachabilityProgram... programs) {
    _context = new NodContext(ctx, programs);
    _queries =
        Arrays.stream(programs)
            .flatMap(
                program ->
                    program
                        .getQueries()
                        .stream()
                        .map(
                            booleanExpr ->
                                BoolExprTransformer.toBoolExpr(
                                    booleanExpr, program.getInput(), _context)))
            .collect(ImmutableList.toImmutableList());
    _rules =
        Arrays.stream(programs)
            .flatMap(
                program ->
                    program
                        .getRules()
                        .stream()
                        .map(
                            booleanExpr ->
                                BoolExprTransformer.toBoolExpr(
                                    booleanExpr, program.getInput(), _context)))
            .collect(ImmutableList.toImmutableList());
  }

  public NodContext getNodContext() {
    return _context;
  }

  public List<BoolExpr> getQueries() {
    return _queries;
  }

  public List<BoolExpr> getRules() {
    return _rules;
  }

  public String toSmt2String() {
    StringBuilder sb = new StringBuilder();
    Streams.concat(
            Arrays.stream(BasicHeaderField.values()),
            Arrays.stream(TransformationHeaderField.values()))
        .forEach(
            hf -> {
              String var = hf.name();
              int size = hf.getSize();
              sb.append(String.format("(declare-var %s (_ BitVec %d))\n", var, size));
            });
    _context
        .getRelationDeclarations()
        .values()
        .stream()
        .map(
            funcDecl ->
                funcDecl
                    .getSExpr()
                    .replaceAll("declare-fun", "declare-rel")
                    .replaceAll(" Bool\\)", ")"))
        .forEach(declaration -> sb.append(String.format("%s\n", declaration)));
    _rules.forEach(r -> sb.append(String.format("(rule %s)\n", r.toString())));

    sb.append("\n");
    _queries.forEach(
        query -> sb.append(String.format("(query %s)\n", query.getFuncDecl().getName())));

    String[] variablesAsNames =
        Streams.concat(
                Arrays.stream(BasicHeaderField.values()),
                Arrays.stream(TransformationHeaderField.values()))
            .map(HeaderField::getName)
            .toArray(String[]::new);
    String[] variablesAsDebruijnIndices =
        IntStream.range(0, variablesAsNames.length)
            .mapToObj(index -> String.format("(:var %d)", index))
            .toArray(String[]::new);
    return StringUtils.replaceEach(sb.toString(), variablesAsDebruijnIndices, variablesAsNames);
  }
}
