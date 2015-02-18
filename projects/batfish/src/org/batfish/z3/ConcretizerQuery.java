package org.batfish.z3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.batfish.representation.Ip;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.AssertExpr;
import org.batfish.z3.node.BooleanExpr;
import org.batfish.z3.node.CheckSatExpr;
import org.batfish.z3.node.EqExpr;
import org.batfish.z3.node.GetModelExpr;
import org.batfish.z3.node.LitIntExpr;
import org.batfish.z3.node.NotExpr;
import org.batfish.z3.node.Statement;
import org.batfish.z3.node.UnsatExpr;
import org.batfish.z3.node.VarIntExpr;

public class ConcretizerQuery {

   public static final ConcretizerQuery UNSAT = new ConcretizerQuery(
         Collections.<Statement> singletonList(new UnsatExpr()));;

   public static ConcretizerQuery blacklistDstIpQuery(Set<Ip> blacklistDstIps) {
      AndExpr conditions = new AndExpr();
      for (Ip blacklistDstIp : blacklistDstIps) {
         EqExpr hasBlacklistedDstIp = new EqExpr(new VarIntExpr(
               Synthesizer.DST_IP_VAR), new LitIntExpr(blacklistDstIp));
         NotExpr not = new NotExpr(hasBlacklistedDstIp);
         conditions.addConjunct(not);
      }
      return new ConcretizerQuery(conditions);
   }

   public static List<ConcretizerQuery> crossProduct(
         List<ConcretizerQuery> left, List<ConcretizerQuery> right) {
      List<ConcretizerQuery> queries = new ArrayList<ConcretizerQuery>();
      for (ConcretizerQuery leftQuery : left) {
         if (leftQuery == UNSAT) {
            continue;
         }
         for (ConcretizerQuery rightQuery : right) {
            if (rightQuery == UNSAT) {
               continue;
            }
            ConcretizerQuery combinedQuery = new ConcretizerQuery(leftQuery);
            combinedQuery.join(rightQuery);
            queries.add(combinedQuery);
         }
      }
      if (queries.size() == 0) {
         queries.add(UNSAT);
      }
      return queries;
   }

   private List<BooleanExpr> _constraints;

   private List<Statement> _statements;

   public ConcretizerQuery(BooleanExpr constraint) {
      _statements = new ArrayList<Statement>();
      _constraints = new ArrayList<BooleanExpr>();
      _constraints.add(constraint);
      refreshStatements();
   }

   private ConcretizerQuery(ConcretizerQuery query) {
      _statements = new ArrayList<Statement>();
      _constraints = new ArrayList<BooleanExpr>();
      _statements.addAll(query._statements);
      _constraints.addAll(query._constraints);
      refreshStatements();
   }

   private ConcretizerQuery(List<Statement> statements) {
      _statements = statements;
   }

   public String getText() {
      StringBuilder sb = new StringBuilder();
      for (Statement statement : _statements) {
         statement.print(sb, 0);
         sb.append("\n");
      }
      return sb.toString();
   }

   private void join(ConcretizerQuery query) {
      _constraints.addAll(query._constraints);
      refreshStatements();
   }

   private void refreshStatements() {
      _statements.clear();
      for (Statement varDecl : Synthesizer.getVarDeclExprs()) {
         _statements.add(varDecl);
      }
      for (BooleanExpr constraint : _constraints) {
         _statements.add(new AssertExpr(constraint));
      }
      _statements.add(CheckSatExpr.INSTANCE);
      _statements.add(GetModelExpr.INSTANCE);
   }

}
