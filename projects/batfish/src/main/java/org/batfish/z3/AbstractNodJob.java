package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.Flow;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.state.OriginateVrf;

public abstract class AbstractNodJob extends Z3ContextJob<NodJobResult> {
  private final String _tag;

  private final OriginateVrfInstrumentation _originateVrfInstrumentation;

  public AbstractNodJob(Settings settings, SortedSet<Pair<String, String>> nodeVrfSet, String tag) {
    super(settings);
    _tag = tag;
    _originateVrfInstrumentation =
        new OriginateVrfInstrumentation(
            nodeVrfSet
                .stream()
                .map(
                    nodeVrfPair ->
                        new OriginateVrf(nodeVrfPair.getFirst(), nodeVrfPair.getSecond()))
                .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Try to find a model for each OriginateVrf. If an OriginateVrf does not have an entry in the
   * Map, then the query is unsat when originating from there.
   */
  Map<OriginateVrf, Map<String, Long>> getOriginateVrfConstraints(Context ctx, SmtInput smtInput) {
    Solver solver = ctx.mkSolver();
    solver.add(smtInput._expr);

    int originateVrfBvSize = _originateVrfInstrumentation.getFieldBits();
    BitVecExpr originateVrfFieldConst =
        ctx.mkBVConst(OriginateVrfInstrumentation.ORIGINATE_VRF_FIELD_NAME, originateVrfBvSize);

    ImmutableMap.Builder<OriginateVrf, Map<String, Long>> models = ImmutableMap.builder();
    // keep refining until no new models
    while (true) {
      try {
        Map<String, Long> constraints = getSolution(solver, smtInput._variablesAsConsts);
        int originateVrfId =
            Math.toIntExact(constraints.get(OriginateVrfInstrumentation.ORIGINATE_VRF_FIELD_NAME));
        OriginateVrf originateVrf =
            _originateVrfInstrumentation.getOriginateVrfs().get(originateVrfId);
        models.put(originateVrf, constraints);

        // refine: different OriginateVrf
        solver.add(
            ctx.mkNot(
                ctx.mkEq(originateVrfFieldConst, ctx.mkBV(originateVrfId, originateVrfBvSize))));
      } catch (QueryUnsatException e) {
        break;
      }
    }

    return models.build();
  }

  private Map<String, Long> getSolution(Solver solver, Map<String, BitVecExpr> variablesAsConsts)
      throws QueryUnsatException {
    Status solverStatus = solver.check();
    switch (solverStatus) {
      case SATISFIABLE:
        Model model = solver.getModel();
        return getFieldConstraints(model, variablesAsConsts);

      case UNKNOWN:
        // timeout. treat this as unsat
      case UNSATISFIABLE:
        // no more models for this or any remaining OriginationVrf
      default:
        throw new QueryUnsatException();
    }
  }

  @Override
  public final NodJobResult call() {
    long startTime = System.currentTimeMillis();
    try (Context ctx = new Context()) {
      SmtInput smtInput = computeSmtInput(startTime, ctx);
      Map<OriginateVrf, Map<String, Long>> originateVrfConstraints =
          getOriginateVrfConstraints(ctx, smtInput);
      Set<Flow> flows = getFlows(originateVrfConstraints);
      return new NodJobResult(startTime, _logger.getHistory(), flows);
    } catch (Z3Exception e) {
      return new NodJobResult(
          startTime,
          _logger.getHistory(),
          new BatfishException("Error running NoD on concatenated data plane", e));
    }
  }

  protected abstract SmtInput computeSmtInput(long startTime, Context ctx);

  ReachabilityProgram instrumentReachabilityProgram(ReachabilityProgram program) {
    List<RuleStatement> rules =
        program
            .getRules()
            .stream()
            .map(_originateVrfInstrumentation::instrumentStatement)
            .map(RuleStatement.class::cast)
            .collect(ImmutableList.toImmutableList());

    return ReachabilityProgram.builder()
        .setInput(program.getInput())
        .setQueries(program.getQueries())
        .setRules(rules)
        .build();
  }

  private Flow createFlow(String node, String vrf, Map<String, Long> constraints) {
    return createFlow(node, vrf, constraints, _tag);
  }

  protected Set<Flow> getFlows(
      Map<OriginateVrf, Map<String, Long>> fieldConstraintsByOriginateVrf) {
    return fieldConstraintsByOriginateVrf
        .entrySet()
        .stream()
        .map(
            entry ->
                createFlow(
                    /* hostname */
                    entry.getKey().getHostname(),
                    /* VRF name */
                    entry.getKey().getVrf(),
                    /* field constraints map */
                    entry.getValue()))
        .collect(Collectors.toSet());
  }

  Map<String, Long> getFieldConstraints(Model model, Map<String, BitVecExpr> variablesAsConsts) {
    return Arrays.stream(model.getConstDecls())
        .map(FuncDecl::getName)
        .map(Object::toString)
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                field ->
                    ((BitVecNum) model.getConstInterp(variablesAsConsts.get(field))).getLong()));
  }

  private static class QueryUnsatException extends Throwable {
    static final long serialVersionUID = 0L;
  }
}
