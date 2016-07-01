package org.batfish.datamodel.routing_policy.statement;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class If extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Statement> _falseStatements;

   private BooleanExpr _guard;

   private List<Statement> _trueStatements;

   @JsonCreator
   public If() {
      _falseStatements = new ArrayList<Statement>();
      _trueStatements = new ArrayList<Statement>();
   }

   public List<Statement> getFalseStatements() {
      return _falseStatements;
   }

   public BooleanExpr getGuard() {
      return _guard;
   }

   public List<Statement> getTrueStatements() {
      return _trueStatements;
   }

   public void setFalseStatements(List<Statement> falseStatements) {
      _falseStatements = falseStatements;
   }

   public void setGuard(BooleanExpr guard) {
      _guard = guard;
   }

   public void setTrueStatements(List<Statement> trueStatements) {
      _trueStatements = trueStatements;
   }

}
