package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public abstract class ForEachStatement<T> implements Statement {

   private final String _setVar;

   private final List<Statement> _statements;

   private final String _var;

   public ForEachStatement(List<Statement> statements, String var, String setVar) {
      _statements = statements;
      _var = var;
      _setVar = setVar;
   }

   protected void elementSideEffect(Environment environment, T t) {
   }

   @Override
   public final void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Collection<T> collection;
      if (_setVar == null) {
         collection = getCollection(environment);
      }
      else {
         collection = getSetMap(environment).get(_setVar);
      }
      for (T t : collection) {
         Environment statementEnv = environment.copy();
         if (_var != null) {
            getVarMap(statementEnv).put(_var, t);
         }
         else {
            writeVal(statementEnv, t);
            elementSideEffect(statementEnv, t);
         }
         for (Statement statement : _statements) {
            statement.execute(statementEnv, logger, settings);
         }
      }
   }

   protected abstract Collection<T> getCollection(Environment environment);

   protected abstract Map<String, Set<T>> getSetMap(Environment environment);

   protected abstract Map<String, T> getVarMap(Environment environment);

   protected abstract void writeVal(Environment environment, T t);

}
