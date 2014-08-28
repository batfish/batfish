package batfish.grammar.z3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import batfish.grammar.z3.DatalogQueryResultParser.*;
import batfish.z3.ConcretizerQuery;
import batfish.z3.Synthesizer;
import batfish.z3.node.*;

public class DatalogQueryResultExtractor extends
      DatalogQueryResultParserBaseListener {

   private List<ConcretizerQuery> _queries;

   @Override
   public void exitResult(ResultContext ctx) {
      if (ctx.UNSAT() != null) {
         List<Statement> unsatStatements = Collections
               .<Statement> singletonList(new UnsatExpr());
         _queries = Collections.singletonList(new ConcretizerQuery(
               unsatStatements));
      }
      else {
         _queries = new ArrayList<ConcretizerQuery>();
         BooleanExpr booleanExpr = toBooleanExpr(ctx.boolean_expr());
         if (booleanExpr instanceof OrExpr) {
            OrExpr orExpr = (OrExpr) booleanExpr;
            for (BooleanExpr disjunct : orExpr.getDisjuncts()) {
               ConcretizerQuery cq = new ConcretizerQuery(disjunct);
               _queries.add(cq);
            }
         }
         else if (booleanExpr instanceof LetExpr) {
            LetExpr letExpr = (LetExpr) booleanExpr;
            BooleanExpr subExpr = letExpr.getExpression();
            if (subExpr instanceof OrExpr) {
               OrExpr orExpr = (OrExpr) subExpr;
               for (BooleanExpr disjunct : orExpr.getDisjuncts()) {
                  LetExpr disjunctLet = new LetExpr(letExpr.getMacroDefs(),
                        disjunct);
                  ConcretizerQuery cq = new ConcretizerQuery(disjunctLet);
                  _queries.add(cq);
               }
            }
            else {
               ConcretizerQuery cq = new ConcretizerQuery(booleanExpr);
               _queries.add(cq);
            }
         }
         else {
            ConcretizerQuery cq = new ConcretizerQuery(booleanExpr);
            _queries.add(cq);
         }
      }
   }

   public List<ConcretizerQuery> getConcretizerQueries() {
      return _queries;
   }

   private AndExpr toAndExpr(And_exprContext ctx) {
      List<BooleanExpr> conjuncts = new ArrayList<BooleanExpr>();
      for (Boolean_exprContext conjunctContext : ctx.conjuncts) {
         BooleanExpr conjunct = toBooleanExpr(conjunctContext);
         conjuncts.add(conjunct);
      }
      return new AndExpr(conjuncts);
   }

   private BooleanExpr toBooleanExpr(Boolean_exprContext ctx) {
      if (ctx.and_expr() != null) {
         return toAndExpr(ctx.and_expr());
      }
      else if (ctx.eq_expr() != null) {
         return toEqExpr(ctx.eq_expr());
      }
      else if (ctx.let_expr() != null) {
         return toLetExpr(ctx.let_expr());
      }
      else if (ctx.macro_ref_expr() != null) {
         return toMacroRefExpr(ctx.macro_ref_expr());
      }
      else if (ctx.not_expr() != null) {
         return toNotExpr(ctx.not_expr());
      }
      else if (ctx.or_expr() != null) {
         return toOrExpr(ctx.or_expr());
      }
      else if (ctx.TRUE() != null) {
         return TrueExpr.INSTANCE;
      }
      else if (ctx.FALSE() != null) {
         return FalseExpr.INSTANCE;
      }
      else {
         throw new Error("bad boolean_expr");
      }
   }

   private EqExpr toEqExpr(Eq_exprContext ctx) {
      IntExpr lhs = toIntExpr(ctx.lhs);
      IntExpr rhs = toIntExpr(ctx.rhs);
      return new EqExpr(lhs, rhs);
   }

   private ExtractExpr toExtractExpr(Extract_exprContext ctx) {
      String highStr = ctx.high.getText();
      int high = Integer.parseInt(highStr);
      String lowStr = ctx.low.getText();
      int low = Integer.parseInt(lowStr);
      String varName = toVarIntExpr(ctx.var_int_expr()).getVariable();
      return new ExtractExpr(varName, low, high);
   }

   private IntExpr toIntExpr(Int_exprContext ctx) {
      if (ctx.lit_int_expr() != null) {
         return toLitIntExpr(ctx.lit_int_expr());
      }
      else if (ctx.extract_expr() != null) {
         return toExtractExpr(ctx.extract_expr());
      }
      else if (ctx.var_int_expr() != null) {
         return toVarIntExpr(ctx.var_int_expr());
      }
      else {
         throw new Error("bad int_expr");
      }
   }

   private BooleanExpr toLetExpr(Let_exprContext ctx) {
      List<MacroDefExpr> macroDefs = new ArrayList<MacroDefExpr>();
      for (Macro_defContext macroDefCtx : ctx.var_defs) {
         MacroDefExpr macroDef = toMacroDef(macroDefCtx);
         macroDefs.add(macroDef);
      }
      BooleanExpr b = toBooleanExpr(ctx.boolean_expr());
      return new LetExpr(macroDefs, b);
   }

   private LitIntExpr toLitIntExpr(Lit_int_exprContext ctx) {
      long value;
      int numBits;
      if (ctx.BIN() != null) {
         String binaryPortion = ctx.BIN().getText().substring(2);
         value = Long.parseLong(binaryPortion, 2);
         numBits = binaryPortion.length();
         return new LitIntExpr(value, numBits);
      }
      else if (ctx.HEX() != null) {
         String hexPortion = ctx.HEX().getText().substring(2);
         value = Long.parseLong(hexPortion, 16);
         numBits = hexPortion.length() * 4;
         return new LitIntExpr(value, numBits);
      }
      else {
         throw new Error("bad lit int token type");
      }
   }

   private MacroDefExpr toMacroDef(Macro_defContext ctx) {
      BooleanExpr b = toBooleanExpr(ctx.boolean_expr());
      String macro = ctx.VARIABLE().getText();
      return new MacroDefExpr(macro, b);
   }

   private BooleanExpr toMacroRefExpr(Macro_ref_exprContext ctx) {
      return new MacroRefExpr(ctx.VARIABLE().toString());
   }

   private BooleanExpr toNotExpr(Not_exprContext ctx) {
      BooleanExpr b = toBooleanExpr(ctx.boolean_expr());
      return new NotExpr(b);
   }

   private OrExpr toOrExpr(Or_exprContext ctx) {
      List<BooleanExpr> disjuncts = new ArrayList<BooleanExpr>();
      for (Boolean_exprContext disjunctContext : ctx.disjuncts) {
         BooleanExpr disjunct = toBooleanExpr(disjunctContext);
         disjuncts.add(disjunct);
      }
      return new OrExpr(disjuncts);
   }

   private VarIntExpr toVarIntExpr(Var_int_exprContext ctx) {
      String varNumStr = ctx.DEC().getText();
      int varNum = Integer.parseInt(varNumStr);
      String var = Synthesizer.PACKET_VARS.get(varNum);
      return new VarIntExpr(var);
   }

}
