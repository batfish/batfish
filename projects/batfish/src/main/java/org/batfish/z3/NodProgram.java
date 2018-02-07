package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodProgram {

  private Context _context;

  private final List<BoolExpr> _queries;

  private final Map<String, FuncDecl> _relationDeclarations;

  private final List<BoolExpr> _rules;

  private final Map<HeaderField, BitVecExpr> _variables;

  private final Map<HeaderField, BitVecExpr> _variablesAsConsts;

  private final Map<HeaderField, Integer> _variableSizes;

  public NodProgram(Context context) {
    _context = context;
    _queries = new ArrayList<>();
    _relationDeclarations = new LinkedHashMap<>();
    _rules = new ArrayList<>();
    _variables = new LinkedHashMap<>();
    _variableSizes = new LinkedHashMap<>();
    _variablesAsConsts = new LinkedHashMap<>();
  }

  public NodProgram append(NodProgram queryProgram) {
    NodProgram result = new NodProgram(_context);
    result._queries.addAll(_queries);
    result._relationDeclarations.putAll(_relationDeclarations);
    result._rules.addAll(_rules);
    result._variables.putAll(_variables);
    result._variableSizes.putAll(_variableSizes);
    result._variablesAsConsts.putAll(_variablesAsConsts);
    result._queries.addAll(queryProgram._queries);
    result._relationDeclarations.putAll(queryProgram._relationDeclarations);
    result._rules.addAll(queryProgram._rules);
    result._variables.putAll(queryProgram._variables);
    result._variableSizes.putAll(queryProgram._variableSizes);
    result._variablesAsConsts.putAll(queryProgram._variablesAsConsts);
    return result;
  }

  public Context getContext() {
    return _context;
  }

  public List<BoolExpr> getQueries() {
    return _queries;
  }

  public Map<String, FuncDecl> getRelationDeclarations() {
    return _relationDeclarations;
  }

  public List<BoolExpr> getRules() {
    return _rules;
  }

  public Map<HeaderField, BitVecExpr> getVariables() {
    return _variables;
  }

  public Map<HeaderField, BitVecExpr> getVariablesAsConsts() {
    return _variablesAsConsts;
  }

  public Map<HeaderField, Integer> getVariableSizes() {
    return _variableSizes;
  }

  public String toSmt2String() {
    StringBuilder sb = new StringBuilder();
    Arrays.stream(HeaderField.values())
        .forEach(
            hf -> {
              String var = hf.name();
              int size = hf.getSize();
              sb.append(String.format("(declare-var %s (_ BitVec %d))\n", var, size));
            });
    StringBuilder sizeSb = new StringBuilder("(");
    Arrays.stream(HeaderField.values())
        .map(HeaderField::getSize)
        .forEach(s -> sizeSb.append(String.format(" (_ BitVec %d)", s)));
    String sizes = sizeSb.append(")").toString();
    _relationDeclarations
        .keySet()
        .stream()
        .map(
            r ->
                r.contains(":") || r.contains("(") || r.contains(")")
                    ? String.format("|%s|", r)
                    : r)
        .forEach(relation -> sb.append(String.format("(declare-rel %s %s)\n", relation, sizes)));
    _rules.forEach(r -> sb.append(String.format("(rule %s)\n", r.toString())));

    sb.append("\n");
    sb.append("(query query_relation)\n");
    String[] intermediate = new String[] {sb.toString()};
    final AtomicInteger currentVar = new AtomicInteger(0);
    Arrays.stream(HeaderField.values())
        .map(HeaderField::name)
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(), v -> String.format("(:var %d)", currentVar.getAndIncrement())))
        .forEach(
            (name, var) ->
                intermediate[0] =
                    intermediate[0].replaceAll(Pattern.quote(var), Matcher.quoteReplacement(name)));
    return intermediate[0];
  }
}
