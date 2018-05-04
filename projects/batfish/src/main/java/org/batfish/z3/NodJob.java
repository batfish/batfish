package org.batfish.z3;

import com.google.common.base.Throwables;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.Status;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.ReachabilityProgramOptimizer;
import org.batfish.z3.expr.RuleStatement;

public final class NodJob extends AbstractNodJob {

  private final Synthesizer _dataPlaneSynthesizer;

  private final boolean _optimize;

  private final QuerySynthesizer _querySynthesizer;

  public NodJob(
      Settings settings,
      Synthesizer dataPlaneSynthesizer,
      QuerySynthesizer querySynthesizer,
      SortedSet<IngressPoint> ingressPoints,
      String tag,
      boolean optimize) {
    super(settings, ingressPoints, tag);
    _dataPlaneSynthesizer = dataPlaneSynthesizer;
    _querySynthesizer = querySynthesizer;
    _optimize = optimize;
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
    if (_settings.debugFlagEnabled("saveNodProgram")) {
      saveNodProgram(program);
    }
    BoolExpr expr = computeSmtConstraintsViaNod(program, _querySynthesizer.getNegate());
    Map<String, BitVecExpr> variablesAsConsts = program.getNodContext().getVariablesAsConsts();
    return new SmtInput(expr, variablesAsConsts);
  }

  private void saveNodProgram(NodProgram program) {
    // synchronize to avoid z3 concurrency bugs
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
  protected NodProgram getNodProgram(Context ctx) {
    ReachabilityProgram baseProgram =
        instrumentReachabilityProgram(_dataPlaneSynthesizer.synthesizeNodDataPlaneProgram());
    ReachabilityProgram queryProgram =
        instrumentReachabilityProgram(
            _querySynthesizer.getReachabilityProgram(_dataPlaneSynthesizer.getInput()));

    if (_optimize) {
      return optimizedProgram(ctx, baseProgram, queryProgram);
    } else {
      return new NodProgram(ctx, baseProgram, queryProgram);
    }
  }

  private NodProgram optimizedProgram(
      Context ctx, ReachabilityProgram baseProgram, ReachabilityProgram queryProgram) {
    List<RuleStatement> allRules = new ArrayList<>(baseProgram.getRules());
    allRules.addAll(queryProgram.getRules());

    List<QueryStatement> allQueries = new ArrayList<>(baseProgram.getQueries());
    allQueries.addAll(queryProgram.getQueries());

    Set<RuleStatement> optimizedRules = ReachabilityProgramOptimizer.optimize(allRules, allQueries);

    ReachabilityProgram optimizedBaseProgram =
        ReachabilityProgram.builder()
            .setInput(baseProgram.getInput())
            .setRules(
                baseProgram
                    .getRules()
                    .stream()
                    .filter(optimizedRules::contains)
                    .collect(Collectors.toList()))
            .setQueries(baseProgram.getQueries())
            .setSmtConstraint(baseProgram.getSmtConstraint())
            .build();

    ReachabilityProgram optimizedQueryProgram =
        ReachabilityProgram.builder()
            .setInput(queryProgram.getInput())
            .setRules(
                queryProgram
                    .getRules()
                    .stream()
                    .filter(optimizedRules::contains)
                    .collect(Collectors.toList()))
            .setQueries(queryProgram.getQueries())
            .setSmtConstraint(queryProgram.getSmtConstraint())
            .build();

    return new NodProgram(ctx, optimizedBaseProgram, optimizedQueryProgram);
  }
}
