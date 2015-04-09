package org.batfish.z3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.batfish.collections.NodeSet;
import org.batfish.main.BatfishException;
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

public class NodJob implements Callable<NodJobResult> {

   private Synthesizer _dataPlaneSynthesizer;
   private final NodeSet _nodeSet;
   private QuerySynthesizer _querySynthesizer;

   public NodJob(Synthesizer dataPlaneSynthesizer,
         QuerySynthesizer querySynthesizer, NodeSet nodeSet) {
      _dataPlaneSynthesizer = dataPlaneSynthesizer;
      _querySynthesizer = querySynthesizer;
      _nodeSet = new NodeSet();
      _nodeSet.addAll(nodeSet);
   }

   @Override
   public NodJobResult call() throws Exception {
      try {
         Context ctx = new Context();
         NodProgram _baseProgram = _dataPlaneSynthesizer
               .synthesizeNodProgram(ctx);
         NodProgram queryProgram = _querySynthesizer
               .getNodProgram(_baseProgram);
         NodProgram program = _baseProgram.append(queryProgram);
         Params p = ctx.mkParams();
         p.add("fixedpoint.engine", "datalog");
         p.add("fixedpoint.datalog.default_relation", "doc");
         p.add("fixedpoint.print.answer", true);
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
         if (answer.getArgs().length > 0) {
            List<Expr> reversedVarList = new ArrayList<Expr>();
            reversedVarList.addAll(program.getVariablesAsConsts().values());
            Collections.reverse(reversedVarList);
            Expr[] reversedVars = reversedVarList.toArray(new Expr[] {});
            Expr substitutedAnswer = answer.substituteVars(reversedVars);
            BoolExpr solverInput = (BoolExpr) substitutedAnswer;
            Solver solver = ctx.mkSolver();
            solver.add(solverInput);
            Status solverStatus = solver.check();
            if (solverStatus != Status.SATISFIABLE) {
               throw new BatfishException(
                     "Sanity check failed: satisfiable expression no longer satisfiable in second stage");
            }
            Model model = solver.getModel();
            Map<String, Long> constraints = new LinkedHashMap<String, Long>();
            for (FuncDecl constDecl : model.getConstDecls()) {
               String name = constDecl.getName().toString();
               BitVecExpr varConstExpr = program.getVariablesAsConsts().get(
                     name);
               long val = ((BitVecNum) model.getConstInterp(varConstExpr))
                     .getLong();
               constraints.put(name, val);
            }
            Set<String> flowLines = new HashSet<String>();
            for (String node : _nodeSet) {
               String flowLine = createFlowLine(node, constraints);
               flowLines.add(flowLine);
            }
            return new NodJobResult(flowLines);
         }
         else {
            return new NodJobResult();
         }
      }
      catch (Z3Exception e) {
         return new NodJobResult(new BatfishException(
               "Error running NoD on concatenated data plane", e));
      }
   }

   private String createFlowLine(String node, Map<String, Long> constraints) {
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
      String line = node + "|" + src_ip + "|" + dst_ip + "|" + src_port + "|"
            + dst_port + "|" + protocol + "\n";
      return line;
   }

}
