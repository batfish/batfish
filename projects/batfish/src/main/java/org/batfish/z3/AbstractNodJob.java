package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.Flow;

public abstract class AbstractNodJob extends Z3ContextJob<NodJobResult> {

  private final SortedSet<Pair<String, String>> _nodeVrfSet;

  private final String _tag;

  public AbstractNodJob(Settings settings, SortedSet<Pair<String, String>> nodeVrfSet, String tag) {
    super(settings);
    _nodeVrfSet = ImmutableSortedSet.copyOf(nodeVrfSet);
    _tag = tag;
  }

  @Override
  public final NodJobResult call() {
    long startTime = System.currentTimeMillis();
    try (Context ctx = new Context()) {
      SmtInput smtInput = computeSmtInput(startTime, ctx);
      Model model = getSmtModel(ctx, smtInput._expr);
      if (model == null) {
        return new NodJobResult(startTime, _logger.getHistory());
      }
      Map<HeaderField, Long> headerConstraints =
          getHeaderConstraints(model, smtInput._variablesAsConsts);
      Set<Flow> flows = getFlows(model, headerConstraints);
      return new NodJobResult(startTime, _logger.getHistory(), flows);
    } catch (Z3Exception e) {
      return new NodJobResult(
          startTime,
          _logger.getHistory(),
          new BatfishException("Error running NoD on concatenated data plane", e));
    }
  }

  Map<HeaderField,Long> getHeaderConstraints(
      Model model, Map<String, BitVecExpr> variablesAsConsts) {
    return Arrays.stream(model.getConstDecls())
        .map(FuncDecl::getName)
        .map(Object::toString)
        .map(HeaderField::parse)
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                headerField ->
                    ((BitVecNum)
                        model.getConstInterp(variablesAsConsts.get(headerField.getName())))
                        .getLong()));
  }

  protected abstract SmtInput computeSmtInput(long startTime, Context ctx);

  private Flow createFlow(String node, String vrf, Map<HeaderField, Long> constraints) {
    return createFlow(node, vrf, constraints, _tag);
  }

  protected Set<Flow> getFlows(Model model, Map<HeaderField, Long> constraints) {
    return _nodeVrfSet
        .stream()
        .map(
            nodeVrf -> {
              String node = nodeVrf.getFirst();
              String vrf = nodeVrf.getSecond();
              return createFlow(node, vrf, constraints);
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  protected Model getSmtModel(Context ctx, BoolExpr solverInput) {
    Solver solver = ctx.mkSolver();
    solver.add(solverInput);
    Status solverStatus = solver.check();
    switch (solverStatus) {
      case SATISFIABLE:
        return solver.getModel();

      case UNKNOWN:
        throw new BatfishException("Stage 2 query satisfiability unknown");

      case UNSATISFIABLE:
        return null;

      default:
        throw new BatfishException("invalid status");
    }
  }
}
