package org.batfish.z3;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.batfish.main.BatfishException;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Params;
import com.microsoft.z3.Z3Exception;

public class NodJob implements Callable<NodJobResult> {

   private Path _outputPath;
   private Path _queryPath;
   private NodProgram _baseProgram;
   private QuerySynthesizer _querySynthesizer;

   public NodJob(NodProgram baseProgram, QuerySynthesizer querySynthesizer) {
      _baseProgram = baseProgram;
      _querySynthesizer = querySynthesizer;
   }

   @Override
   public NodJobResult call() throws Exception {
      _outputPath = Paths.get(_queryPath.toString() + ".out");
      try {
         Context ctx = _baseProgram.getContext();
         NodProgram queryProgram = _querySynthesizer.getNodProgram(_baseProgram);
         NodProgram program = _baseProgram.append(queryProgram);
         Params p = ctx.mkParams();
         p.add("fixedpoint.engine", "datalog");
         p.add("fixedpoint.datalog.default_relation", "doc");
         p.add("fixedpoint.print.answer", true);
         Fixedpoint fix = ctx.mkFixedpoint();
         fix.setParameters(p);
         for (FuncDecl relationDeclaration : program.getRelationDeclarations().values()) {
            fix.registerRelation(relationDeclaration);
         }
         for (BoolExpr rule : program.getRules()) {
            // TODO: figure out what belongs in 2nd position of addRule
            fix.addRule(rule, ctx.mkSymbol(0));
         }
         for (BoolExpr query : program.getQueries()) {
            fix.query(query);
         }
         Expr answer = fix.getAnswer();
         String answerText = answer.toString();

         return new NodJobResult(answerText);
      }
      catch (Z3Exception e) {
         return new NodJobResult(new BatfishException(
               "Error running NoD on concatenated data plane", e));
      }
   }

   public Path getOutputPath() {
      return _outputPath;
   }

}
