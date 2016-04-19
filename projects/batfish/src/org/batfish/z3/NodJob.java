package org.batfish.z3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.collections.NodeSet;
import org.batfish.job.BatfishJob;
import org.batfish.common.BatfishException;
import org.batfish.representation.Flow;
import org.batfish.representation.IcmpCode;
import org.batfish.representation.IcmpType;
import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Params;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public final class NodJob extends BatfishJob<NodJobResult> {

   public static Flow createFlow(String node, Map<String, Long> constraints,
         String tag) {
      long src_ip = 0;
      long dst_ip = 0;
      long src_port = 0;
      long dst_port = 0;
      long icmp_type = IcmpType.UNSET;
      long icmp_code = IcmpCode.UNSET;
      long tcp_flags_cwr = 0;
      long tcp_flags_ece = 0;
      long tcp_flags_urg = 0;
      long tcp_flags_ack = 0;
      long tcp_flags_psh = 0;
      long tcp_flags_rst = 0;
      long tcp_flags_syn = 0;
      long tcp_flags_fin = 0;
      long protocol = IpProtocol.IP.number();
      for (String varName : constraints.keySet()) {
         Long value = constraints.get(varName);
         switch (varName) {
         case Synthesizer.SRC_IP_VAR:
            src_ip = value;
            break;

         case Synthesizer.DST_IP_VAR:
            dst_ip = value;
            break;

         case Synthesizer.SRC_PORT_VAR:
            src_port = value;
            break;

         case Synthesizer.DST_PORT_VAR:
            dst_port = value;
            break;

         case Synthesizer.IP_PROTOCOL_VAR:
            protocol = value;
            break;

         case Synthesizer.ICMP_TYPE_VAR:
            icmp_type = value;
            break;

         case Synthesizer.ICMP_CODE_VAR:
            icmp_code = value;
            break;

         case Synthesizer.TCP_FLAGS_CWR_VAR:
            tcp_flags_cwr = value;
            break;

         case Synthesizer.TCP_FLAGS_ECE_VAR:
            tcp_flags_ece = value;
            break;

         case Synthesizer.TCP_FLAGS_URG_VAR:
            tcp_flags_urg = value;
            break;

         case Synthesizer.TCP_FLAGS_ACK_VAR:
            tcp_flags_ack = value;
            break;

         case Synthesizer.TCP_FLAGS_PSH_VAR:
            tcp_flags_psh = value;
            break;

         case Synthesizer.TCP_FLAGS_RST_VAR:
            tcp_flags_rst = value;
            break;

         case Synthesizer.TCP_FLAGS_SYN_VAR:
            tcp_flags_syn = value;
            break;

         case Synthesizer.TCP_FLAGS_FIN_VAR:
            tcp_flags_fin = value;
            break;

         default:
            throw new Error("invalid variable name");
         }
      }
      return new Flow(node, new Ip(src_ip), new Ip(dst_ip), (int) src_port,
            (int) dst_port, IpProtocol.fromNumber((int) protocol),
            (int) icmp_type, (int) icmp_code, (int) tcp_flags_cwr,
            (int) tcp_flags_ece, (int) tcp_flags_urg, (int) tcp_flags_ack,
            (int) tcp_flags_psh, (int) tcp_flags_rst, (int) tcp_flags_syn,
            (int) tcp_flags_fin, tag);
   }

   private Synthesizer _dataPlaneSynthesizer;

   private final NodeSet _nodeSet;

   private QuerySynthesizer _querySynthesizer;

   private String _tag;

   public NodJob(Synthesizer dataPlaneSynthesizer,
         QuerySynthesizer querySynthesizer, NodeSet nodeSet, String tag) {
      _dataPlaneSynthesizer = dataPlaneSynthesizer;
      _querySynthesizer = querySynthesizer;
      _nodeSet = new NodeSet();
      _nodeSet.addAll(nodeSet);
      _tag = tag;
   }

   @Override
   public NodJobResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      Context ctx = null;
      try {
         ctx = new Context();
         NodProgram baseProgram = _dataPlaneSynthesizer
               .synthesizeNodDataPlaneProgram(ctx);
         NodProgram queryProgram = _querySynthesizer.getNodProgram(baseProgram);
         NodProgram program = baseProgram.append(queryProgram);
         Params p = ctx.mkParams();
         p.add("fixedpoint.engine", "datalog");
         p.add("fixedpoint.datalog.default_relation", "doc");
         p.add("fixedpoint.print_answer", true);
         Fixedpoint fix = ctx.mkFixedpoint();
         fix.setParameters(p);
         for (FuncDecl relationDeclaration : program.getRelationDeclarations()
               .values()) {
            fix.registerRelation(relationDeclaration);
         }
         for (BoolExpr rule : program.getRules()) {
            fix.addRule(rule, null);
         }
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
         Expr answer = fix.getAnswer();
         BoolExpr solverInput;
         if (answer.getArgs().length > 0) {
            List<Expr> reversedVarList = new ArrayList<Expr>();
            reversedVarList.addAll(program.getVariablesAsConsts().values());
            Collections.reverse(reversedVarList);
            Expr[] reversedVars = reversedVarList.toArray(new Expr[] {});
            Expr substitutedAnswer = answer.substituteVars(reversedVars);
            solverInput = (BoolExpr) substitutedAnswer;
         }
         else {
            solverInput = (BoolExpr) answer;
         }
         if (_querySynthesizer.getNegate()) {
            solverInput = ctx.mkNot(solverInput);
         }
         Solver solver = ctx.mkSolver();
         solver.add(solverInput);
         Status solverStatus = solver.check();
         switch (solverStatus) {
         case SATISFIABLE:
            break;

         case UNKNOWN:
            throw new BatfishException("Stage 2 query satisfiability unknown");

         case UNSATISFIABLE:
            elapsedTime = System.currentTimeMillis() - startTime;
            return new NodJobResult(elapsedTime);

         default:
            throw new BatfishException("invalid status");
         }
         Model model = solver.getModel();
         Map<String, Long> constraints = new LinkedHashMap<String, Long>();
         for (FuncDecl constDecl : model.getConstDecls()) {
            String name = constDecl.getName().toString();
            BitVecExpr varConstExpr = program.getVariablesAsConsts().get(name);
            long val = ((BitVecNum) model.getConstInterp(varConstExpr))
                  .getLong();
            constraints.put(name, val);
         }
         Set<Flow> flows = new HashSet<Flow>();
         for (String node : _nodeSet) {
            Flow flow = createFlow(node, constraints);
            flows.add(flow);
         }
         elapsedTime = System.currentTimeMillis() - startTime;
         return new NodJobResult(elapsedTime, flows);
      }
      catch (Z3Exception e) {
         elapsedTime = System.currentTimeMillis() - startTime;
         return new NodJobResult(elapsedTime, new BatfishException(
               "Error running NoD on concatenated data plane", e));
      }
   }

   private Flow createFlow(String node, Map<String, Long> constraints) {
      return createFlow(node, constraints, _tag);
   }

}
