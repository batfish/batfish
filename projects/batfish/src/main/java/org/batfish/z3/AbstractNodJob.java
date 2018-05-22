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
import org.batfish.config.Settings;
import org.batfish.datamodel.Flow;
import org.batfish.z3.expr.RuleStatement;

public abstract class AbstractNodJob extends Z3ContextJob<NodJobResult> {
  private final String _tag;

  private final IngressPointInstrumentation _ingressPointInstrumentation;

  public AbstractNodJob(Settings settings, SortedSet<IngressPoint> ingressPoints, String tag) {
    super(settings);
    _tag = tag;
    _ingressPointInstrumentation =
        new IngressPointInstrumentation(ImmutableList.copyOf(ingressPoints));
  }

  /**
   * Try to find a model for each OriginateVrf. If an OriginateVrf does not have an entry in the
   * Map, then the query is unsat when originating from there.
   */
  protected Map<IngressPoint, Map<String, Long>> getIngressPointConstraints(
      Context ctx, SmtInput smtInput) {
    Solver solver = ctx.mkSolver();
    solver.add(smtInput._expr);

    int originateVrfBvSize = _ingressPointInstrumentation.getFieldBits();
    BitVecExpr originateVrfFieldConst =
        ctx.mkBVConst(IngressPointInstrumentation.INGRESS_POINT_FIELD_NAME, originateVrfBvSize);

    ImmutableMap.Builder<IngressPoint, Map<String, Long>> models = ImmutableMap.builder();
    // keep refining until no new models
    while (true) {
      try {
        Map<String, Long> constraints = getSolution(solver, smtInput._variablesAsConsts);
        int originateVrfId =
            Math.toIntExact(constraints.get(IngressPointInstrumentation.INGRESS_POINT_FIELD_NAME));
        IngressPoint ingressPoint =
            _ingressPointInstrumentation.getIngressPoints().get(originateVrfId);
        models.put(ingressPoint, constraints);

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

  protected Map<String, Long> getSolution(Solver solver, Map<String, BitVecExpr> variablesAsConsts)
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
      Map<IngressPoint, Map<String, Long>> ingressPointConstraints =
          getIngressPointConstraints(ctx, smtInput);
      Set<Flow> flows = getFlows(ingressPointConstraints);
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
            .map(_ingressPointInstrumentation::instrumentStatement)
            .map(RuleStatement.class::cast)
            .collect(ImmutableList.toImmutableList());

    return ReachabilityProgram.builder()
        .setInput(program.getInput())
        .setQueries(program.getQueries())
        .setRules(rules)
        .setSmtConstraint(program.getSmtConstraint())
        .build();
  }

  protected Set<Flow> getFlows(Map<IngressPoint, Map<String, Long>> ingressPointConstraints) {
    return ingressPointConstraints
        .entrySet()
        .stream()
        .map(
            entry ->
                createFlow(
                    /* ingress point */
                    entry.getKey(),
                    /* field constraints map */
                    entry.getValue(),
                    _tag))
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

  protected static class QueryUnsatException extends Throwable {
    static final long serialVersionUID = 1L;
  }
}
