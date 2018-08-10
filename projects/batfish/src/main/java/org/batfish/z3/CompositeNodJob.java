package org.batfish.z3;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.z3.expr.BooleanExpr;

public class CompositeNodJob extends AbstractNodJob {

  private List<Synthesizer> _dataPlaneSynthesizers;

  private int _numPrograms;

  private final boolean _optimize;

  private List<QuerySynthesizer> _querySynthesizers;

  public CompositeNodJob(
      Settings settings,
      List<Synthesizer> dataPlaneSynthesizer,
      List<QuerySynthesizer> querySynthesizer,
      Map<IngressLocation, BooleanExpr> srcIpConstraints,
      boolean optimize,
      String tag) {
    super(settings, srcIpConstraints, tag);
    _optimize = optimize;
    _numPrograms = dataPlaneSynthesizer.size();
    if (_numPrograms != querySynthesizer.size()) {
      throw new BatfishException("mismatch between number of programs and number of queries");
    }
    _dataPlaneSynthesizers = dataPlaneSynthesizer;
    _querySynthesizers = querySynthesizer;
  }

  @Override
  protected SmtInput computeSmtInput(long startTime, Context ctx) {
    BoolExpr[] answers = new BoolExpr[_numPrograms];
    Map<String, BitVecExpr> variablesAsConsts = new HashMap<>();
    for (int i = 0; i < _numPrograms; i++) {
      Synthesizer dataPlaneSynthesizer = _dataPlaneSynthesizers.get(i);
      QuerySynthesizer querySynthesizer = _querySynthesizers.get(i);
      ReachabilityProgram baseProgram =
          instrumentReachabilityProgram(dataPlaneSynthesizer.synthesizeNodProgram());
      ReachabilityProgram queryProgram =
          instrumentReachabilityProgram(
              querySynthesizer.getReachabilityProgram(dataPlaneSynthesizer.getInput()));

      NodProgram program =
          _optimize
              ? optimizedProgram(ctx, baseProgram, queryProgram)
              : new NodProgram(ctx, baseProgram, queryProgram);
      variablesAsConsts.putAll(program.getNodContext().getVariablesAsConsts());
      answers[i] = computeSmtConstraintsViaNod(program, querySynthesizer.getNegate());
    }
    return new SmtInput(ctx.mkAnd(answers), variablesAsConsts);
  }
}
