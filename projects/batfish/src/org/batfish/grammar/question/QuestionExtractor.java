package org.batfish.grammar.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.question.QuestionParser.*;
import org.batfish.main.BatfishException;
import org.batfish.question.AddIpStatement;
import org.batfish.question.AndExpr;
import org.batfish.question.Assertion;
import org.batfish.question.BgpNeighborBooleanExpr;
import org.batfish.question.BgpNeighborIntExpr;
import org.batfish.question.BgpNeighborIpExpr;
import org.batfish.question.BooleanExpr;
import org.batfish.question.ClearIpsStatement;
import org.batfish.question.ContainsIpExpr;
import org.batfish.question.DifferenceIntExpr;
import org.batfish.question.EqExpr;
import org.batfish.question.ForEachBgpNeighborStatement;
import org.batfish.question.ForEachGeneratedRouteStatement;
import org.batfish.question.ForEachInterfaceStatement;
import org.batfish.question.ForEachNodeBgpGeneratedRouteStatement;
import org.batfish.question.ForEachNodeStatement;
import org.batfish.question.ForEachStaticRouteStatement;
import org.batfish.question.GeneratedRoutePrefixExpr;
import org.batfish.question.GtExpr;
import org.batfish.question.IfExpr;
import org.batfish.question.IfStatement;
import org.batfish.question.IntExpr;
import org.batfish.question.IntegerAssignment;
import org.batfish.question.InterfaceBooleanExpr;
import org.batfish.question.InterfaceIpExpr;
import org.batfish.question.InterfacePrefixExpr;
import org.batfish.question.InterfaceStringExpr;
import org.batfish.question.IpExpr;
import org.batfish.question.LiteralIntExpr;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.NeqExpr;
import org.batfish.question.NodeBooleanExpr;
import org.batfish.question.NodeStringExpr;
import org.batfish.question.NotExpr;
import org.batfish.question.NumIpsIntExpr;
import org.batfish.question.OrExpr;
import org.batfish.question.PrintableExpr;
import org.batfish.question.PrintfStatement;
import org.batfish.question.ProductIntExpr;
import org.batfish.question.Question;
import org.batfish.question.QuotientIntExpr;
import org.batfish.question.Statement;
import org.batfish.question.StaticBooleanExpr;
import org.batfish.question.StaticRouteBooleanExpr;
import org.batfish.question.StaticRouteIntExpr;
import org.batfish.question.StaticRouteIpExpr;
import org.batfish.question.StaticRoutePrefixExpr;
import org.batfish.question.StaticRouteStringExpr;
import org.batfish.question.StringLiteralStringExpr;
import org.batfish.question.SumIntExpr;
import org.batfish.question.VarIntExpr;
import org.batfish.question.VerifyProgram;
import org.batfish.question.VerifyQuestion;
import org.batfish.util.Util;

public class QuestionExtractor extends QuestionParserBaseListener implements
      BatfishExtractor {

   private static final String ERR_CONVERT_BOOLEAN = "Cannot convert parse tree node to boolean expression";

   private static final String ERR_CONVERT_INT = "Cannot convert parse tree node to integer expression";

   private static final String ERR_CONVERT_IP = "Cannot convert parse tree node to IP expression";

   private static final String ERR_CONVERT_PREFIX = "Cannot convert parse tree node to prefix expression";

   private static final String ERR_CONVERT_PRINTABLE = "Cannot convert parse tree node to printable expression";

   private static final String ERR_CONVERT_STATEMENT = "Cannot convert parse tree node to statement";

   private static final String ERR_CONVERT_STRING = "Cannot convert parse tree node to string expression";

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

   private BooleanExpr toBooleanExpr(Bgp_neighbor_boolean_exprContext expr) {
      if (expr.bgp_neighbor_has_generated_route_boolean_expr() != null) {
         return BgpNeighborBooleanExpr.BGP_NEIGHBOR_HAS_GENERATED_ROUTE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
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
      else if (expr.eq_expr() != null) {
         return toBooleanExpr(expr.eq_expr());
      }
      else if (expr.gt_expr() != null) {
         return toBooleanExpr(expr.gt_expr());
      }
      else if (expr.if_expr() != null) {
         return toBooleanExpr(expr.if_expr());
      }
      else if (expr.neq_expr() != null) {
         return toBooleanExpr(expr.neq_expr());
      }
      else if (expr.not_expr() != null) {
         return toBooleanExpr(expr.not_expr());
      }
      else if (expr.or_expr() != null) {
         return toBooleanExpr(expr.or_expr());
      }
      else if (expr.property_boolean_expr() != null) {
         return toBooleanExpr(expr.property_boolean_expr());
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

   private BooleanExpr toBooleanExpr(Eq_exprContext expr) {
      IntExpr lhs = toIntExpr(expr.lhs);
      IntExpr rhs = toIntExpr(expr.rhs);
      return new EqExpr(lhs, rhs);
   }

   private BooleanExpr toBooleanExpr(Gt_exprContext expr) {
      IntExpr lhs = toIntExpr(expr.lhs);
      IntExpr rhs = toIntExpr(expr.rhs);
      return new GtExpr(lhs, rhs);
   }

   private BooleanExpr toBooleanExpr(If_exprContext expr) {
      BooleanExpr antecedent = toBooleanExpr(expr.antecedent);
      BooleanExpr consequent = toBooleanExpr(expr.consequent);
      IfExpr ifExpr = new IfExpr(antecedent, consequent);
      return ifExpr;
   }

   private BooleanExpr toBooleanExpr(Interface_boolean_exprContext expr) {
      if (expr.interface_enabled_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_ENABLED;
      }
      else if (expr.interface_has_ip_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_HAS_IP;
      }
      else if (expr.interface_isis_boolean_expr() != null) {
         return toBooleanExpr(expr.interface_isis_boolean_expr());
      }
      else if (expr.interface_isloopback_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_IS_LOOPBACK;
      }
      else if (expr.interface_ospf_boolean_expr() != null) {
         return toBooleanExpr(expr.interface_ospf_boolean_expr());
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

   private BooleanExpr toBooleanExpr(Interface_isis_boolean_exprContext expr) {
      if (expr.interface_isis_active_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_ISIS_ACTIVE;
      }
      else if (expr.interface_isis_passive_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_ISIS_PASSIVE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Interface_ospf_boolean_exprContext expr) {
      if (expr.interface_ospf_active_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_OSPF_ACTIVE;
      }
      else if (expr.interface_ospf_passive_boolean_expr() != null) {
         return InterfaceBooleanExpr.INTERFACE_OSPF_PASSIVE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Neq_exprContext expr) {
      IntExpr lhs = toIntExpr(expr.lhs);
      IntExpr rhs = toIntExpr(expr.rhs);
      return new NeqExpr(lhs, rhs);
   }

   private BooleanExpr toBooleanExpr(Node_bgp_boolean_exprContext expr) {
      if (expr.node_bgp_configured_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_BGP_CONFIGURED;
      }
      else if (expr.node_bgp_has_generated_route_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_BGP_HAS_GENERATED_ROUTE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Node_boolean_exprContext expr) {
      if (expr.node_bgp_boolean_expr() != null) {
         return toBooleanExpr(expr.node_bgp_boolean_expr());
      }
      else if (expr.node_has_generated_route_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_HAS_GENERATED_ROUTE;
      }
      else if (expr.node_isis_boolean_expr() != null) {
         return toBooleanExpr(expr.node_isis_boolean_expr());
      }
      else if (expr.node_ospf_boolean_expr() != null) {
         return toBooleanExpr(expr.node_ospf_boolean_expr());
      }
      else if (expr.node_static_boolean_expr() != null) {
         return toBooleanExpr(expr.node_static_boolean_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Node_isis_boolean_exprContext expr) {
      if (expr.node_isis_configured_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_ISIS_CONFIGURED;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Node_ospf_boolean_exprContext expr) {
      if (expr.node_ospf_configured_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_OSPF_CONFIGURED;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private BooleanExpr toBooleanExpr(Node_static_boolean_exprContext expr) {
      if (expr.node_static_configured_boolean_expr() != null) {
         return NodeBooleanExpr.NODE_STATIC_CONFIGURED;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
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

   private BooleanExpr toBooleanExpr(Property_boolean_exprContext expr) {
      if (expr.bgp_neighbor_boolean_expr() != null) {
         return toBooleanExpr(expr.bgp_neighbor_boolean_expr());
      }
      else if (expr.interface_boolean_expr() != null) {
         return toBooleanExpr(expr.interface_boolean_expr());
      }
      else if (expr.node_boolean_expr() != null) {
         return toBooleanExpr(expr.node_boolean_expr());
      }
      else if (expr.static_route_boolean_expr() != null) {
         return toBooleanExpr(expr.static_route_boolean_expr());
      }
      else {
         throw new BatfishException("Missing conversion for expression");
      }
   }

   private BooleanExpr toBooleanExpr(Static_route_boolean_exprContext expr) {
      if (expr.static_route_has_next_hop_interface_boolean_expr() != null) {
         return StaticRouteBooleanExpr.STATICROUTE_HAS_NEXT_HOP_INTERFACE;
      }
      else if (expr.static_route_has_next_hop_ip_boolean_expr() != null) {
         return StaticRouteBooleanExpr.STATICROUTE_HAS_NEXT_HOP_IP;
      }
      else {
         throw new BatfishException(ERR_CONVERT_BOOLEAN);
      }
   }

   private IntExpr toIntExpr(Bgp_neighbor_int_exprContext expr) {
      if (expr.bgp_neighbor_local_as_int_expr() != null) {
         return BgpNeighborIntExpr.LOCAL_AS;
      }
      else if (expr.bgp_neighbor_remote_as_int_expr() != null) {
         return BgpNeighborIntExpr.REMOTE_AS;
      }
      else {
         throw new BatfishException(ERR_CONVERT_INT);
      }
   }

   private IntExpr toIntExpr(Int_exprContext expr) {
      if (expr.ASTERISK() != null) {
         IntExpr multiplicand1 = toIntExpr(expr.multiplicand1);
         IntExpr multiplicand2 = toIntExpr(expr.multiplicand2);
         return new ProductIntExpr(multiplicand1, multiplicand2);
      }
      else if (expr.FORWARD_SLASH() != null) {
         IntExpr dividend = toIntExpr(expr.dividend);
         IntExpr divisor = toIntExpr(expr.divisor);
         return new QuotientIntExpr(dividend, divisor);
      }
      else if (expr.MINUS() != null) {
         IntExpr subtrahend = toIntExpr(expr.subtrahend);
         IntExpr minuend = toIntExpr(expr.minuend);
         return new DifferenceIntExpr(subtrahend, minuend);
      }
      else if (expr.OPEN_PAREN() != null) {
         return toIntExpr(expr.parenthesized);
      }
      else if (expr.PLUS() != null) {
         IntExpr addend1 = toIntExpr(expr.addend1);
         IntExpr addend2 = toIntExpr(expr.addend2);
         return new SumIntExpr(addend1, addend2);
      }
      else if (expr.val_int_expr() != null) {
         return toIntExpr(expr.val_int_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_INT);
      }
   }

   private IntExpr toIntExpr(Literal_int_exprContext expr) {
      int i = Integer.parseInt(expr.DEC().toString());
      return new LiteralIntExpr(i);
   }

   private IntExpr toIntExpr(Num_ips_int_exprContext expr) {
      String caller = expr.caller.getText();
      return new NumIpsIntExpr(caller);
   }

   private IntExpr toIntExpr(Static_route_int_exprContext expr) {
      if (expr.static_route_administrative_cost_int_expr() != null) {
         return StaticRouteIntExpr.STATICROUTE_ADMINISTRATIVE_COST;
      }
      else {
         throw new BatfishException(ERR_CONVERT_INT);
      }
   }

   private IntExpr toIntExpr(Val_int_exprContext expr) {
      if (expr.bgp_neighbor_int_expr() != null) {
         return toIntExpr(expr.bgp_neighbor_int_expr());
      }
      else if (expr.literal_int_expr() != null) {
         return toIntExpr(expr.literal_int_expr());
      }
      else if (expr.num_ips_int_expr() != null) {
         return toIntExpr(expr.num_ips_int_expr());
      }
      else if (expr.static_route_int_expr() != null) {
         return toIntExpr(expr.static_route_int_expr());
      }
      else if (expr.var_int_expr() != null) {
         return toIntExpr(expr.var_int_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_INT);
      }
   }

   private IntExpr toIntExpr(Var_int_exprContext expr) {
      String variable = expr.VARIABLE().getText();
      return new VarIntExpr(variable);
   }

   private IpExpr toIpExpr(Bgp_neighbor_ip_exprContext expr) {
      if (expr.bgp_neighbor_local_ip_ip_expr() != null) {
         return BgpNeighborIpExpr.LOCAL_IP;
      }
      else if (expr.bgp_neighbor_remote_ip_ip_expr() != null) {
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
      else if (expr.static_route_ip_expr() != null) {
         return toIpExpr(expr.static_route_ip_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_IP);
      }
   }

   private IpExpr toIpExpr(Static_route_ip_exprContext expr) {
      if (expr.static_route_next_hop_ip_ip_expr() != null) {
         return StaticRouteIpExpr.STATICROUTE_NEXT_HOP_IP;
      }
      else {
         throw new BatfishException(ERR_CONVERT_IP);
      }
   }

   private PrintableExpr toPrefixExpr(Generated_route_prefix_exprContext expr) {
      if (expr.generated_route_prefix_prefix_expr() != null) {
         return GeneratedRoutePrefixExpr.GENERATED_ROUTE_PREFIX;
      }
      else {
         throw new BatfishException(ERR_CONVERT_PREFIX);
      }
   }

   private PrintableExpr toPrefixExpr(Interface_prefix_exprContext expr) {
      if (expr.interface_prefix_prefix_expr() != null) {
         return InterfacePrefixExpr.INTERFACE_PREFIX;
      }
      else {
         throw new BatfishException(ERR_CONVERT_PREFIX);
      }
   }

   private PrintableExpr toPrefixExpr(Prefix_exprContext expr) {
      if (expr.generated_route_prefix_expr() != null) {
         return toPrefixExpr(expr.generated_route_prefix_expr());
      }
      else if (expr.interface_prefix_expr() != null) {
         return toPrefixExpr(expr.interface_prefix_expr());
      }
      else if (expr.static_route_prefix_expr() != null) {
         return toPrefixExpr(expr.static_route_prefix_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_PREFIX);
      }
   }

   private PrintableExpr toPrefixExpr(Static_route_prefix_exprContext expr) {
      if (expr.static_route_prefix_prefix_expr() != null) {
         return StaticRoutePrefixExpr.STATICROUTE_PREFIX;
      }
      else {
         throw new BatfishException(ERR_CONVERT_PREFIX);
      }
   }

   private PrintableExpr toPrintableExpr(Printable_exprContext expr) {
      if (expr.boolean_expr() != null) {
         return toBooleanExpr(expr.boolean_expr());
      }
      else if (expr.int_expr() != null) {
         return toIntExpr(expr.int_expr());
      }
      else if (expr.ip_expr() != null) {
         return toIpExpr(expr.ip_expr());
      }
      else if (expr.prefix_expr() != null) {
         return toPrefixExpr(expr.prefix_expr());
      }
      else if (expr.string_expr() != null) {
         return toStringExpr(expr.string_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_PRINTABLE);
      }
   }

   private Statement toStatement(Add_ip_statementContext statement) {
      String target = statement.target.getText();
      IpExpr ipExpr = toIpExpr(statement.ip_expr());
      return new AddIpStatement(target, ipExpr);
   }

   private Statement toStatement(AssertionContext ctx) {
      BooleanExpr expr = toBooleanExpr(ctx.boolean_expr());
      Map<String, PrintableExpr> onErrorPrintables = new HashMap<String, PrintableExpr>();
      for (Assertion_failure_env_entryContext entry : ctx.env_entries) {
         String name = entry.VARIABLE().getText();
         PrintableExpr pexpr = toPrintableExpr(entry.printable_expr());
         onErrorPrintables.put(name, pexpr);
      }
      Assertion assertion = new Assertion(expr, ctx.getText(),
            onErrorPrintables);
      return assertion;
   }

   private Statement toStatement(AssignmentContext assignment) {
      if (assignment.int_assignment() != null) {
         return toStatement(assignment.int_assignment());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(Bgp_neighbor_statementContext ctx) {
      if (ctx.foreach_generated_route_statement() != null) {
         return toStatement(ctx.foreach_generated_route_statement());
      }
      else if (ctx.statement() != null) {
         return toStatement(ctx.statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(Clear_ips_statementContext statement) {
      String caller = statement.caller.getText();
      return new ClearIpsStatement(caller);
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

   private Statement toStatement(
         Foreach_generated_route_statementContext statement) {
      List<Statement> generatedRouteStatements = new ArrayList<Statement>();
      for (Generated_route_statementContext ctx : statement
            .generated_route_statement()) {
         Statement generatedRouteStatement = toStatement(ctx);
         generatedRouteStatements.add(generatedRouteStatement);
      }
      return new ForEachGeneratedRouteStatement(generatedRouteStatements);
   }

   private Statement toStatement(Foreach_interface_statementContext statement) {
      List<Statement> interfaceStatements = new ArrayList<Statement>();
      for (Interface_statementContext ctx : statement.interface_statement()) {
         Statement interfaceStatement = toStatement(ctx);
         interfaceStatements.add(interfaceStatement);
      }
      return new ForEachInterfaceStatement(interfaceStatements);
   }

   private Statement toStatement(
         Foreach_node_bgp_generated_route_statementContext statement) {
      List<Statement> generatedRouteStatements = new ArrayList<Statement>();
      for (Generated_route_statementContext ctx : statement
            .generated_route_statement()) {
         Statement generatedRouteStatement = toStatement(ctx);
         generatedRouteStatements.add(generatedRouteStatement);
      }
      return new ForEachNodeBgpGeneratedRouteStatement(generatedRouteStatements);
   }

   private Statement toStatement(Foreach_node_statementContext statement) {
      List<Statement> nodeStatements = new ArrayList<Statement>();
      for (Node_statementContext ctx : statement.node_statement()) {
         Statement nodeStatement = toStatement(ctx);
         nodeStatements.add(nodeStatement);
      }
      return new ForEachNodeStatement(nodeStatements);
   }

   private Statement toStatement(Foreach_static_route_statementContext statement) {
      List<Statement> static_routeStatements = new ArrayList<Statement>();
      for (Static_route_statementContext ctx : statement
            .static_route_statement()) {
         Statement static_routeStatement = toStatement(ctx);
         static_routeStatements.add(static_routeStatement);
      }
      return new ForEachStaticRouteStatement(static_routeStatements);

   }

   private Statement toStatement(Generated_route_statementContext ctx) {
      if (ctx.statement() != null) {
         return toStatement(ctx.statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
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

   private Statement toStatement(Int_assignmentContext int_assignment) {
      String variable = int_assignment.VARIABLE().getText();
      IntExpr intExpr = toIntExpr(int_assignment.int_expr());
      return new IntegerAssignment(variable, intExpr);
   }

   private Statement toStatement(Interface_statementContext ctx) {
      return toStatement(ctx.statement());
   }

   private Statement toStatement(Node_statementContext statement) {
      if (statement.foreach_bgp_neighbor_statement() != null) {
         return toStatement(statement.foreach_bgp_neighbor_statement());
      }
      else if (statement.foreach_generated_route_statement() != null) {
         return toStatement(statement.foreach_generated_route_statement());
      }
      else if (statement.foreach_interface_statement() != null) {
         return toStatement(statement.foreach_interface_statement());
      }
      else if (statement.foreach_node_bgp_generated_route_statement() != null) {
         return toStatement(statement
               .foreach_node_bgp_generated_route_statement());
      }
      else if (statement.foreach_static_route_statement() != null) {
         return toStatement(statement.foreach_static_route_statement());
      }
      else if (statement.statement() != null) {
         return toStatement(statement.statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(Printf_statementContext statement) {
      PrintableExpr formatString = toPrintableExpr(statement.format_string);
      List<PrintableExpr> replacements = new ArrayList<PrintableExpr>();
      for (Printable_exprContext pexpr : statement.replacements) {
         PrintableExpr replacement = toPrintableExpr(pexpr);
         replacements.add(replacement);
      }
      return new PrintfStatement(formatString, replacements);
   }

   private Statement toStatement(StatementContext statement) {
      if (statement.add_ip_statement() != null) {
         return toStatement(statement.add_ip_statement());
      }
      else if (statement.assertion() != null) {
         return toStatement(statement.assertion());
      }
      else if (statement.assignment() != null) {
         return toStatement(statement.assignment());
      }
      else if (statement.if_statement() != null) {
         return toStatement(statement.if_statement());
      }
      else if (statement.clear_ips_statement() != null) {
         return toStatement(statement.clear_ips_statement());
      }
      else if (statement.printf_statement() != null) {
         return toStatement(statement.printf_statement());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STATEMENT);
      }
   }

   private Statement toStatement(Static_route_statementContext ctx) {
      return toStatement(ctx.statement());
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

   private PrintableExpr toStringExpr(Interface_string_exprContext expr) {
      if (expr.interface_name_string_expr() != null) {
         return InterfaceStringExpr.INTERFACE_NAME;
      }
      else {
         throw new BatfishException(ERR_CONVERT_STRING);
      }
   }

   private PrintableExpr toStringExpr(Node_string_exprContext expr) {
      if (expr.node_name_string_expr() != null) {
         return NodeStringExpr.NODE_NAME;
      }
      else {
         throw new BatfishException(ERR_CONVERT_STRING);
      }
   }

   private PrintableExpr toStringExpr(Static_route_string_exprContext expr) {
      if (expr.static_route_next_hop_interface_string_expr() != null) {
         return StaticRouteStringExpr.STATICROUTE_NEXT_HOP_INTERFACE;
      }
      else {
         throw new BatfishException(ERR_CONVERT_STRING);
      }
   }

   private PrintableExpr toStringExpr(String_exprContext expr) {
      if (expr.interface_string_expr() != null) {
         return toStringExpr(expr.interface_string_expr());
      }
      else if (expr.node_string_expr() != null) {
         return toStringExpr(expr.node_string_expr());
      }
      else if (expr.static_route_string_expr() != null) {
         return toStringExpr(expr.static_route_string_expr());
      }
      else if (expr.string_literal_string_expr() != null) {
         return toStringExpr(expr.string_literal_string_expr());
      }
      else {
         throw new BatfishException(ERR_CONVERT_STRING);
      }
   }

   private PrintableExpr toStringExpr(String_literal_string_exprContext expr) {
      String withEscapes = expr.getText();
      String processed = Util.unescapeJavaString(withEscapes);
      return new StringLiteralStringExpr(processed);
   }

}
