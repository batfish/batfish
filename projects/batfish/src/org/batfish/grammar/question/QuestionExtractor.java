package org.batfish.grammar.question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.question.QuestionParser.*;
import org.batfish.main.BatfishException;
import org.batfish.question.AddIpStatement;
import org.batfish.question.AndExpr;
import org.batfish.question.Assertion;
import org.batfish.question.BgpNeighborIpExpr;
import org.batfish.question.BooleanExpr;
import org.batfish.question.ContainsIpExpr;
import org.batfish.question.ForEachBgpNeighborStatement;
import org.batfish.question.ForEachInterfaceStatement;
import org.batfish.question.ForEachNodeStatement;
import org.batfish.question.IfExpr;
import org.batfish.question.IfStatement;
import org.batfish.question.InterfaceBooleanExpr;
import org.batfish.question.InterfaceIpExpr;
import org.batfish.question.IpExpr;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.NotExpr;
import org.batfish.question.OrExpr;
import org.batfish.question.Question;
import org.batfish.question.Statement;
import org.batfish.question.StaticBooleanExpr;
import org.batfish.question.VerifyProgram;
import org.batfish.question.VerifyQuestion;

public class QuestionExtractor extends QuestionParserBaseListener implements
      BatfishExtractor {

   private static final String ERR_CONVERT_BOOLEAN = "Cannot convert parse tree node to boolean";

   private static final String ERR_CONVERT_IP = "Cannot convert parse tree node to IP";

   private static final String ERR_CONVERT_STATEMENT = "Cannot convert parse tree node to statement";;

   private Question _question;

   private VerifyProgram _verifyProgram;

   @Override
   public void enterMultipath_question(Multipath_questionContext ctx) {
      MultipathQuestion multipathQuestion = new MultipathQuestion();
      _question = multipathQuestion;
      multipathQuestion.setMasterEnvironment(ctx.environment.getText());
   }

   @Override
   public void enterVerify_question(Verify_questionContext ctx) {
      VerifyQuestion verifyQuestion = new VerifyQuestion();
      _question = verifyQuestion;
      _verifyProgram = verifyQuestion.getProgram();
   }

   @Override
   public void enterVerify_statement(Verify_statementContext ctx) {
      Statement statement = toStatement(ctx);
      _verifyProgram.getStatements().add(statement);
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
      if (expr.and_expr() != null) {
         return toBooleanExpr(expr.and_expr());
      }
      else if (expr.contains_ip_expr() != null) {
         return toBooleanExpr(expr.contains_ip_expr());
      }
      else if (expr.false_expr() != null) {
         return StaticBooleanExpr.FALSE;
      }
      else if (expr.if_expr() != null) {
         return toBooleanExpr(expr.if_expr());
      }
      else if (expr.not_expr() != null) {
         return toBooleanExpr(expr.not_expr());
      }
      else if (expr.or_expr() != null) {
         return toBooleanExpr(expr.or_expr());
      }
      else if (expr.property_expr() != null) {
         return toBooleanExpr(expr.property_expr());
      }
      else if (expr.true_expr() != null) {
         return StaticBooleanExpr.TRUE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Contains_ip_exprContext expr) {
      String caller = expr.caller.getText();
      IpExpr ipExpr = toIpExpr(expr.ip_expr());
      return new ContainsIpExpr(caller, ipExpr);
   }

   private BooleanExpr toBooleanExpr(If_exprContext expr) {
      BooleanExpr antecedent = toBooleanExpr(expr.antecedent);
      BooleanExpr consequent = toBooleanExpr(expr.consequent);
      IfExpr ifExpr = new IfExpr(antecedent, consequent);
      return ifExpr;
   }

   private BooleanExpr toBooleanExpr(Interface_isis_property_exprContext expr) {
      if (expr.interface_isis_active_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_ISIS_ACTIVE;
      }
      else if (expr.interface_isis_passive_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_ISIS_PASSIVE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Interface_property_exprContext expr) {
      if (expr.interface_isis_property_expr() != null) {
         return toBooleanExpr(expr.interface_isis_property_expr());
      }
      else if (expr.interface_isloopback_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_IS_LOOPBACK;
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

   private IpExpr toIpExpr(Bgp_neighbor_ip_exprContext expr) {
      if (expr.bgp_neighbor_remote_ip_ip_expr() != null) {
         return BgpNeighborIpExpr.REMOTE_IP;
      }
      else {
         throw new BatfishException(ERR_CONVERT_IP);
      }
   }

   private IpExpr toIpExpr(Interface_ip_exprContext expr) {
      if (expr.interface_ip_ip_expr() != null) {
         return InterfaceIpExpr.INTERFACE_IP;
      }
      else {
         throw new BatfishException(ERR_CONVERT_IP);
      }
   }

   private IpExpr toIpExpr(Ip_exprContext expr) {
      if (expr.bgp_neighbor_ip_expr() != null) {
         return toIpExpr(expr.bgp_neighbor_ip_expr());
      }
      else if (expr.interface_ip_expr() != null) {
         return toIpExpr(expr.interface_ip_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_IP);
      }
   }

   private Statement toStatement(Add_ip_statementContext statement) {
      String target = statement.target.getText();
      IpExpr ipExpr = toIpExpr(statement.ip_expr());
      return new AddIpStatement(target, ipExpr);
   }

   private Statement toStatement(AssertionContext ctx) {
      BooleanExpr expr = toBooleanExpr(ctx.boolean_expr());
      Assertion assertion = new Assertion(expr, ctx.getText());
      return assertion;
   }

   private Statement toStatement(Bgp_neighbor_statementContext ctx) {
      return toStatement(ctx.statement());
   }

   private Statement toStatement(Foreach_bgp_neighbor_statementContext statement) {
      List<Statement> bgpNeighborStatements = new ArrayList<Statement>();
      for (Bgp_neighbor_statementContext ctx : statement
            .bgp_neighbor_statement()) {
         Statement bgpNeighborStatement = toStatement(ctx);
         bgpNeighborStatements.add(bgpNeighborStatement);
      }
      return new ForEachBgpNeighborStatement(bgpNeighborStatements);
   }

   private Statement toStatement(Foreach_interface_statementContext statement) {
      List<Statement> interfaceStatements = new ArrayList<Statement>();
      for (Interface_statementContext ctx : statement.interface_statement()) {
         Statement interfaceStatement = toStatement(ctx);
         interfaceStatements.add(interfaceStatement);
      }
      return new ForEachInterfaceStatement(interfaceStatements);
   }

   private Statement toStatement(Foreach_node_statementContext statement) {
      List<Statement> nodeStatements = new ArrayList<Statement>();
      for (Node_statementContext ctx : statement.node_statement()) {
         Statement nodeStatement = toStatement(ctx);
         nodeStatements.add(nodeStatement);
      }
      return new ForEachNodeStatement(nodeStatements);
   }

   private Statement toStatement(If_statementContext statement) {
      BooleanExpr guard = toBooleanExpr(statement.guard);
      List<Statement> trueStatements = new ArrayList<Statement>();
      for (StatementContext ctx : statement.true_statements) {
         Statement trueStatement = toStatement(ctx);
         trueStatements.add(trueStatement);
      }
      List<Statement> falseStatements = new ArrayList<Statement>();
      for (StatementContext ctx : statement.false_statements) {
         Statement falseStatement = toStatement(ctx);
         falseStatements.add(falseStatement);
      }
      return new IfStatement(guard, trueStatements, falseStatements);
   }

   private Statement toStatement(Interface_statementContext ctx) {
      return toStatement(ctx.statement());
   }

   private Statement toStatement(Node_statementContext statement) {
      if (statement.foreach_bgp_neighbor_statement() != null) {
         return toStatement(statement.foreach_bgp_neighbor_statement());
      }
      else if (statement.foreach_interface_statement() != null) {
         return toStatement(statement.foreach_interface_statement());
      }
      else if (statement.statement() != null) {
         return toStatement(statement.statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(StatementContext statement) {
      if (statement.add_ip_statement() != null) {
         return toStatement(statement.add_ip_statement());
      }
      else if (statement.assertion() != null) {
         return toStatement(statement.assertion());
      }
      else if (statement.if_statement() != null) {
         return toStatement(statement.if_statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(Verify_statementContext statement) {
      if (statement.foreach_node_statement() != null) {
         return toStatement(statement.foreach_node_statement());
      }
      else if (statement.statement() != null) {
         return toStatement(statement.statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

}
