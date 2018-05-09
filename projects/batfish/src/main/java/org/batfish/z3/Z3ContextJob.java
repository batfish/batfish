package org.batfish.z3;

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
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.State;
import org.batfish.job.BatfishJob;
import org.batfish.job.BatfishJobResult;

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
              (flowBuilder, value) -> flowBuilder.setState(State.fromNum(value.intValue())))
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
      IngressPoint ingressPoint, Map<String, Long> constraints, String tag) {
    Flow.Builder flowBuilder = new Flow.Builder();
    flowBuilder.setIngressNode(ingressPoint.getNode());
    switch (ingressPoint.getType()) {
      case INTERFACE:
        flowBuilder.setIngressInterface(ingressPoint.getInterface());
        break;
      case VRF:
        flowBuilder.setIngressVrf(ingressPoint.getVrf());
        break;
      default:
        throw new BatfishException("Unexpected IngressPoint type");
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
    return getSolverInput(answer, program, negate);
  }

  protected BoolExpr getSolverInput(Expr answer, NodProgram program, boolean negate) {
    BoolExpr solverInput;
    if (answer.getArgs().length > 0) {

      Map<String, BitVecExpr> variablesAsConsts = program.getNodContext().getVariablesAsConsts();
      List<BitVecExpr> vars =
          program
              .getNodContext()
              .getVariableNames()
              .stream()
              .map(variablesAsConsts::get)
              .collect(Collectors.toList());
      List<BitVecExpr> reversedVars = Lists.reverse(vars);

      Expr substitutedSmtConstraint =
          program.getSmtConstraint().substituteVars(vars.toArray(new Expr[] {}));
      Expr substitutedAnswer = answer.substituteVars(reversedVars.toArray(new Expr[] {}));
      solverInput =
          program
              .getNodContext()
              .getContext()
              .mkAnd((BoolExpr) substitutedAnswer, (BoolExpr) substitutedSmtConstraint);
    } else {
      solverInput = (BoolExpr) answer;
    }
    if (negate) {
      solverInput = program.getNodContext().getContext().mkNot(solverInput);
    }
    return solverInput;
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
}
