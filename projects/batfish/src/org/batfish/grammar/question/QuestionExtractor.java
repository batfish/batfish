package org.batfish.grammar.question;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.question.QuestionParser.*;
import org.batfish.main.BatfishException;
import org.batfish.question.AndExpr;
import org.batfish.question.Assertion;
import org.batfish.question.BooleanExpr;
import org.batfish.question.IfExpr;
import org.batfish.question.InterfaceExpr;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.NotExpr;
import org.batfish.question.OrExpr;
import org.batfish.question.Question;
import org.batfish.question.StaticBooleanExpr;
import org.batfish.question.StaticInterfaceSelector;
import org.batfish.question.StaticNodeSelector;
import org.batfish.question.VerifyQuestion;

public class QuestionExtractor extends QuestionParserBaseListener implements
      BatfishExtractor {

   private MultipathQuestion _multipathQuestion;

   private Question _question;

   private VerifyQuestion _verifyQuestion;

   @Override
   public void enterAssertion(AssertionContext ctx) {
      BooleanExpr expr = toBooleanExpr(ctx.boolean_expr());
      Assertion assertion = new Assertion(expr);
      _verifyQuestion.setAssertion(assertion);
   }

   @Override
   public void enterForeach_interface(Foreach_interfaceContext ctx) {
      _verifyQuestion.setInterfaceSelector(StaticInterfaceSelector.TRUE);
   }

   @Override
   public void enterForeach_node(Foreach_nodeContext ctx) {
      _verifyQuestion.setNodeSelector(StaticNodeSelector.TRUE);
   }

   @Override
   public void enterMultipath_question(Multipath_questionContext ctx) {
      _multipathQuestion = new MultipathQuestion();
      _question = _verifyQuestion;
      _multipathQuestion.setMasterEnvironment(ctx.environment.getText());
   }

   @Override
   public void enterVerify_question(Verify_questionContext ctx) {
      _verifyQuestion = new VerifyQuestion();
      _question = _verifyQuestion;
   }

   public Question getQuestion() {
      return _question;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   private BooleanExpr toBooleanExpr(And_exprContext expr) {
      Set<BooleanExpr> conjuncts = new HashSet<BooleanExpr>();
      for (Boolean_exprContext conjunctCtx : expr.conjuncts) {
         BooleanExpr conjunct = toBooleanExpr(conjunctCtx);
         conjuncts.add(conjunct);
      }
      AndExpr andExpr = new AndExpr(conjuncts);
      return andExpr;
   }

   private BooleanExpr toBooleanExpr(Boolean_exprContext expr) {
      if (expr.true_expr() != null) {
         return StaticBooleanExpr.TRUE;
      }
      else if (expr.false_expr() != null) {
         return StaticBooleanExpr.FALSE;
      }
      else if (expr.and_expr() != null) {
         return toBooleanExpr(expr.and_expr());
      }
      else if (expr.or_expr() != null) {
         return toBooleanExpr(expr.or_expr());
      }
      else if (expr.if_expr() != null) {
         return toBooleanExpr(expr.if_expr());
      }
      else if (expr.not_expr() != null) {
         return toBooleanExpr(expr.not_expr());
      }
      else if (expr.property_expr() != null) {
         return toBooleanExpr(expr.property_expr());
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

   private BooleanExpr toBooleanExpr(If_exprContext expr) {
      BooleanExpr antecedent = toBooleanExpr(expr.antecedent);
      BooleanExpr consequent = toBooleanExpr(expr.consequent);
      IfExpr ifExpr = new IfExpr(antecedent, consequent);
      return ifExpr;
   }

   private BooleanExpr toBooleanExpr(Interface_isis_property_exprContext expr) {
      if (expr.interface_isis_active_expr() != null) {
         return InterfaceExpr.INTERFACE_ISIS_ACTIVE;
      }
      else if (expr.interface_isis_passive_expr() != null) {
         return InterfaceExpr.INTERFACE_ISIS_PASSIVE;
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

   private BooleanExpr toBooleanExpr(Interface_property_exprContext expr) {
      if (expr.interface_isis_property_expr() != null) {
         return toBooleanExpr(expr.interface_isis_property_expr());
      }
      else if (expr.interface_isloopback_expr() != null) {
         return InterfaceExpr.INTERFACE_IS_LOOPBACK;
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

   private BooleanExpr toBooleanExpr(Not_exprContext expr) {
      BooleanExpr argument = toBooleanExpr(expr.boolean_expr());
      NotExpr notExpr = new NotExpr(argument);
      return notExpr;
   }

   private BooleanExpr toBooleanExpr(Or_exprContext expr) {
      Set<BooleanExpr> disjuncts = new HashSet<BooleanExpr>();
      for (Boolean_exprContext disjunctCtx : expr.disjuncts) {
         BooleanExpr disjunct = toBooleanExpr(disjunctCtx);
         disjuncts.add(disjunct);
      }
      OrExpr orExpr = new OrExpr(disjuncts);
      return orExpr;
   }

   private BooleanExpr toBooleanExpr(Property_exprContext expr) {
      if (expr.interface_property_expr() != null) {
         return toBooleanExpr(expr.interface_property_expr());
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

}
