package org.batfish.question.statement;

import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public class SetClearStatement implements Statement {

   public SetClearStatement(String caller, VariableType type) {
      // TODO Auto-generated constructor stub
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
