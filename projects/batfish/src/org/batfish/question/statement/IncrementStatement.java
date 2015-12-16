package org.batfish.question.statement;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public final class IncrementStatement implements Statement {

   private final String _var;

   public IncrementStatement(String var) {
      _var = var;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      int oldVal = environment.getIntegers().get(_var);
      int newVal = oldVal + 1;
      environment.getIntegers().put(_var, newVal);
   }

}
