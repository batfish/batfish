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

public class CompositeNodJob extends BatfishJob<NodJobResult> {

   private List<Synthesizer> _dataPlaneSynthesizers;

   private final NodeSet _nodeSet;

   private int _numPrograms;

   private List<QuerySynthesizer> _querySynthesizers;

   private String _tag;

   public CompositeNodJob(List<Synthesizer> dataPlaneSynthesizer,
         List<QuerySynthesizer> querySynthesizer, NodeSet nodeSet, String tag) {
      _numPrograms = dataPlaneSynthesizer.size();
      if (_numPrograms != querySynthesizer.size()) {
         throw new BatfishException(
               "mismatch between number of programs and number of queries");
      }
      _dataPlaneSynthesizers = dataPlaneSynthesizer;
      _querySynthesizers = querySynthesizer;
      _nodeSet = new NodeSet();
      _nodeSet.addAll(nodeSet);
      _tag = tag;
   }

   @Override
   public NodJobResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      NodProgram latestProgram = null;
      Context ctx = null;
      try {
         BoolExpr[] answers = new BoolExpr[_numPrograms];
         ctx = new Context();
         Params p = ctx.mkParams();
         p.add("fixedpoint.engine", "datalog");
         p.add("fixedpoint.datalog.default_relation", "doc");
         p.add("fixedpoint.print_answer", true);
         for (int i = 0; i < _numPrograms; i++) {
            Synthesizer dataPlaneSynthesizer = _dataPlaneSynthesizers.get(i);
            QuerySynthesizer querySynthesizer = _querySynthesizers.get(i);
            NodProgram baseProgram = dataPlaneSynthesizer
                  .synthesizeNodProgram(ctx);
            NodProgram queryProgram = querySynthesizer
                  .getNodProgram(baseProgram);
            NodProgram program = baseProgram.append(queryProgram);
            latestProgram = program;
            Fixedpoint fix = ctx.mkFixedpoint();
            fix.setParameters(p);
            for (FuncDecl relationDeclaration : program
                  .getRelationDeclarations().values()) {
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
            if (_querySynthesizers.get(i).getNegate()) {
               answers[i] = ctx.mkNot(solverInput);
            }
            else {
               answers[i] = solverInput;
            }
         }
         BoolExpr compositeQuery = ctx.mkAnd(answers);
         Solver solver = ctx.mkSolver();
         solver.add(compositeQuery);
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
            BitVecExpr varConstExpr = latestProgram.getVariablesAsConsts().get(
                  name);
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
      finally {
         if (ctx != null) {
            ctx.dispose();
         }
      }
   }

   private Flow createFlow(String node, Map<String, Long> constraints) {
      long src_ip = 0;
      long dst_ip = 0;
      long src_port = 0;
      long dst_port = 0;
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

         default:
            throw new Error("invalid variable name");
         }
      }
      return new Flow(node, new Ip(src_ip), new Ip(dst_ip), (int) src_port,
            (int) dst_port, IpProtocol.fromNumber((int) protocol), _tag);
   }

}
