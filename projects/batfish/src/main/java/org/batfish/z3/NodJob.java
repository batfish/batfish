package org.batfish.z3;

import com.google.common.base.Throwables;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.config.Settings;
import org.batfish.z3.expr.BooleanExpr;

public final class NodJob extends AbstractNodJob {

  private final Synthesizer _dataPlaneSynthesizer;

  private final boolean _optimize;

  private final QuerySynthesizer _querySynthesizer;

  public NodJob(
      Settings settings,
      Synthesizer dataPlaneSynthesizer,
      QuerySynthesizer querySynthesizer,
      Map<IngressLocation, BooleanExpr> srcIpConstraints,
      String tag,
      boolean optimize) {
    super(settings, srcIpConstraints, tag);
    _dataPlaneSynthesizer = dataPlaneSynthesizer;
    _querySynthesizer = querySynthesizer;
    _optimize = optimize;
  }

  @Override
  protected SmtInput computeSmtInput(long startTime, Context ctx) {
    NodProgram program = getNodProgram(ctx);
    if (_settings.debugFlagEnabled("saveNodProgram")) {
      saveNodProgram(program);
    }
    BoolExpr expr = computeSmtConstraintsViaNod(program, _querySynthesizer.getNegate());
    Map<String, BitVecExpr> variablesAsConsts = program.getNodContext().getVariablesAsConsts();
    return new SmtInput(expr, variablesAsConsts);
  }

  private void saveNodProgram(NodProgram program) {
    // synchronize to avoid z3 concurrency bugs. TODO: is this really needed?
    // other writers also use NodJob.class to synchronize writes.
    synchronized (NodJob.class) {
      Path nodPath =
          _settings
              .getActiveTestrigSettings()
              .getBasePath()
              .resolve(
                  String.format(
                      "nodProgram-%s-%d.smt2", Instant.now(), Thread.currentThread().getId()));
      try (FileWriter writer = new FileWriter(nodPath.toFile())) {
        writer.write(program.toSmt2String());
      } catch (IOException e) {
        _logger.warnf("Error saving Nod program to file: %s", Throwables.getStackTraceAsString(e));
      }
    }
  }

  @Nonnull
  private NodProgram getNodProgram(Context ctx) {
    ReachabilityProgram baseProgram =
        instrumentReachabilityProgram(_dataPlaneSynthesizer.synthesizeNodProgram());
    ReachabilityProgram queryProgram =
        instrumentReachabilityProgram(
            _querySynthesizer.getReachabilityProgram(_dataPlaneSynthesizer.getInput()));

    return _optimize
        ? optimizedProgram(ctx, baseProgram, queryProgram)
        : new NodProgram(ctx, baseProgram, queryProgram);
  }
}
