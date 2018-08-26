package org.batfish.z3;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Params;
import com.microsoft.z3.Status;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.FlowState;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.job.BatfishJob;
import org.batfish.job.BatfishJobResult;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.ReachabilityProgramOptimizer;
import org.batfish.z3.expr.RuleStatement;

public abstract class Z3ContextJob<R extends BatfishJobResult<?, ?>> extends BatfishJob<R> {

  /**
   * A map of handler functions used to construct Flows from a Z3 model. Each key is the name of a
   * Field, and the values are BiConsumers (two arguments, void return type) with arguments of type
   * Flow.Builder and Long (the value corresponding to the field taken by the model). Each converts
   * the Long to the appropriate type and sets the field's value in the flow.
   */
  private static Map<String, BiConsumer<Builder, Long>> flowBuilders =
      ImmutableMap.<String, BiConsumer<Builder, Long>>builder()
          .put(Field.DST_IP.getName(), (flowBuilder, value) -> flowBuilder.setDstIp(new Ip(value)))
          .put(
              Field.SRC_PORT.getName(),
              (flowBuilder, value) -> flowBuilder.setSrcPort(value.intValue()))
          .put(
              Field.DST_PORT.getName(),
              (flowBuilder, value) -> flowBuilder.setDstPort(value.intValue()))
          .put(
              Field.FRAGMENT_OFFSET.getName(),
              (flowBuilder, value) -> flowBuilder.setFragmentOffset(value.intValue()))
          .put(
              Field.IP_PROTOCOL.getName(),
              (flowBuilder, value) ->
                  flowBuilder.setIpProtocol(IpProtocol.fromNumber(value.intValue())))
          .put(Field.DSCP.getName(), (flowBuilder, value) -> flowBuilder.setDscp(value.intValue()))
          .put(Field.ECN.getName(), (flowBuilder, value) -> flowBuilder.setEcn(value.intValue()))
          .put(
              Field.STATE.getName(),
              (flowBuilder, value) -> flowBuilder.setState(FlowState.fromNum(value.intValue())))
          .put(
              Field.ICMP_TYPE.getName(),
              (flowBuilder, value) -> flowBuilder.setIcmpType(value.intValue()))
          .put(
              Field.ICMP_CODE.getName(),
              (flowBuilder, value) -> flowBuilder.setIcmpCode(value.intValue()))
          .put(
              Field.ORIG_SRC_IP.getName(),
              (flowBuilder, value) -> flowBuilder.setSrcIp(new Ip(value)))
          .put(
              Field.PACKET_LENGTH.getName(),
              (flowBuilder, value) -> flowBuilder.setPacketLength(value.intValue()))
          .put(
              Field.TCP_FLAGS_CWR.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsCwr(value.intValue()))
          .put(
              Field.TCP_FLAGS_ECE.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsEce(value.intValue()))
          .put(
              Field.TCP_FLAGS_URG.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsUrg(value.intValue()))
          .put(
              Field.TCP_FLAGS_ACK.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsAck(value.intValue()))
          .put(
              Field.TCP_FLAGS_PSH.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsPsh(value.intValue()))
          .put(
              Field.TCP_FLAGS_RST.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsRst(value.intValue()))
          .put(
              Field.TCP_FLAGS_SYN.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsSyn(value.intValue()))
          .put(
              Field.TCP_FLAGS_FIN.getName(),
              (flowBuilder, value) -> flowBuilder.setTcpFlagsFin(value.intValue()))
          .build();

  protected static Flow createFlow(
      IngressLocation ingressLocation, Map<String, Long> constraints, String tag) {
    Flow.Builder flowBuilder = new Flow.Builder();
    switch (ingressLocation.getType()) {
      case INTERFACE_LINK:
        flowBuilder
            .setIngressNode(ingressLocation.getNode())
            .setIngressInterface(ingressLocation.getInterface());
        break;
      case VRF:
        flowBuilder
            .setIngressNode(ingressLocation.getNode())
            .setIngressVrf(ingressLocation.getVrf());
        break;
      default:
        throw new BatfishException("Unexpected IngressLocation Type: " + ingressLocation.getType());
    }
    flowBuilder.setTag(tag);
    constraints.forEach(
        (name, value) -> {
          if (!flowBuilders.containsKey(name)) {
            return;
          }
          flowBuilders.get(name).accept(flowBuilder, value);
        });
    return flowBuilder.build();
  }

  public Z3ContextJob(Settings settings) {
    super(settings);
  }

  protected Expr answerFixedPoint(Fixedpoint fix, NodProgram program) {
    for (BoolExpr query : program.getQueries()) {
      Status status = fix.query(query);
      switch (status) {
        case SATISFIABLE:
          break;
        case UNKNOWN:
          throw new BatfishException("Query satisfiability unknown");
        case UNSATISFIABLE:
          break;
        default:
          throw new BatfishException("invalid status");
      }
    }
    return fix.getAnswer();
  }

  protected BoolExpr computeSmtConstraintsViaNod(NodProgram program, boolean negate) {
    Fixedpoint fix = mkFixedpoint(program, true);
    Expr answer = answerFixedPoint(fix, program);
    BoolExpr solverInput = getSolverInput(answer, program, negate);
    if (_settings.debugFlagEnabled("saveSolverInput")) {
      saveSolverInput(solverInput.simplify());
    }
    return solverInput;
  }

  private void saveSolverInput(Expr expr) {
    // synchronize to avoid z3 concurrency bugs.
    // use NodJob to synchronize with any other similar writers.
    synchronized (NodJob.class) {
      Path nodPath =
          _settings
              .getActiveTestrigSettings()
              .getBasePath()
              .resolve(
                  String.format(
                      "solverInput-%s-%d.smt2", Instant.now(), Thread.currentThread().getId()));
      try (FileWriter writer = new FileWriter(nodPath.toFile())) {
        writer.write(expr.toString());
      } catch (IOException e) {
        _logger.warnf("Error saving Nod program to file: %s", Throwables.getStackTraceAsString(e));
      }
    }
  }

  protected BoolExpr getSolverInput(Expr answer, NodProgram program, boolean negate) {
    Map<String, BitVecExpr> variablesAsConsts = program.getNodContext().getVariablesAsConsts();
    List<BitVecExpr> vars =
        program
            .getNodContext()
            .getVariableNames()
            .stream()
            .map(variablesAsConsts::get)
            .collect(Collectors.toList());

    Expr substitutedSmtConstraint =
        program.getSmtConstraint().substituteVars(vars.toArray(new Expr[] {}));

    List<BitVecExpr> reversedVars = Lists.reverse(vars);
    Expr substitutedAnswer =
        answer.getArgs().length == 0
            ? answer
            : answer.substituteVars(reversedVars.toArray(new Expr[] {}));

    BoolExpr answerAndSmtConstraint =
        program
            .getNodContext()
            .getContext()
            .mkAnd((BoolExpr) substitutedAnswer, (BoolExpr) substitutedSmtConstraint);

    return negate
        ? program.getNodContext().getContext().mkNot(answerAndSmtConstraint)
        : answerAndSmtConstraint;
  }

  protected Fixedpoint mkFixedpoint(NodProgram program, boolean printAnswer) {
    Context ctx = program.getNodContext().getContext();
    Params p = ctx.mkParams();
    p.add("timeout", _settings.getZ3timeout());
    p.add("fixedpoint.engine", "datalog");
    p.add("fixedpoint.datalog.default_relation", "doc");
    p.add("fixedpoint.print_answer", printAnswer);
    Fixedpoint fix = ctx.mkFixedpoint();
    fix.setParameters(p);
    for (FuncDecl relationDeclaration :
        program.getNodContext().getRelationDeclarations().values()) {
      fix.registerRelation(relationDeclaration);
    }
    for (BoolExpr rule : program.getRules()) {
      fix.addRule(rule, null);
    }
    return fix;
  }

  protected NodProgram optimizedProgram(
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
