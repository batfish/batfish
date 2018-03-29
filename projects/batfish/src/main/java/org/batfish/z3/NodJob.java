package org.batfish.z3;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.Status;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.config.Settings;

public final class NodJob extends AbstractNodJob {

  private Synthesizer _dataPlaneSynthesizer;

  private QuerySynthesizer _querySynthesizer;

  public NodJob(
      Settings settings,
      Synthesizer dataPlaneSynthesizer,
      QuerySynthesizer querySynthesizer,
      SortedSet<Pair<String, String>> nodeVrfSet,
      String tag) {
    super(settings, nodeVrfSet, tag);
    _dataPlaneSynthesizer = dataPlaneSynthesizer;
    _querySynthesizer = querySynthesizer;
  }

  protected Status computeNodSat(long startTime, Context ctx) {
    NodProgram program = getNodProgram(ctx);
    Fixedpoint fix = mkFixedpoint(program, true);
    for (BoolExpr query : program.getQueries()) {
      Status status = fix.query(query);
      switch (status) {
        case SATISFIABLE:
          return status;
        case UNKNOWN:
          throw new BatfishException("Query satisfiability unknown");
        case UNSATISFIABLE:
          return status;
        default:
          throw new BatfishException("invalid status");
      }
    }
    throw new BatfishException("No queries");
  }

  @Override
  protected SmtInput computeSmtInput(long startTime, Context ctx) {
    NodProgram program = getNodProgram(ctx);
    BoolExpr expr = computeSmtConstraintsViaNod(program, _querySynthesizer.getNegate());
    Map<String, BitVecExpr> variablesAsConsts = program.getNodContext().getVariablesAsConsts();
    return new SmtInput(expr, variablesAsConsts);
  }

  @Nonnull
  protected NodProgram getNodProgram(Context ctx) {
    ReachabilityProgram baseProgram =
        instrumentReachabilityProgram(_dataPlaneSynthesizer.synthesizeNodDataPlaneProgram());
    ReachabilityProgram queryProgram =
        instrumentReachabilityProgram(
            _querySynthesizer.getReachabilityProgram(_dataPlaneSynthesizer.getInput()));
    return new NodProgram(ctx, baseProgram, queryProgram);
  }
}
