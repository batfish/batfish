package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public abstract class ForEachStatement<T> implements Statement {

   private final List<Statement> _statements;

   protected final String _var;

   public ForEachStatement(List<Statement> statements, String var) {
      _statements = statements;
      _var = var;
   }

   @Override
   public final void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      T oldValue = null;
      if (_var != null) {
         oldValue = getOldVarVal(environment);
      }
      Collection<T> collection = getCollection(environment);
      for (T t : collection) {
         Environment statementEnv = environment.copy();
         if (_var != null) {
            writeVarVal(statementEnv, t);
         }
         else {
            writeVal(statementEnv, t);
         }
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
      if (oldValue != null) {
         writeVarVal(environment, oldValue);
      }
   }

   protected abstract Collection<T> getCollection(Environment environment);

   protected abstract T getOldVarVal(Environment environment);

   protected abstract void writeVal(Environment environment, T t);

   protected abstract void writeVarVal(Environment environment, T t);

}
