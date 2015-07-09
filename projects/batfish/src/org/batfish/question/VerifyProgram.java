package org.batfish.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.representation.Configuration;

public class VerifyProgram {

   private Environment _environment;

   private List<Statement> _statements;

   public VerifyProgram() {
      _environment = new Environment();
      _statements = new ArrayList<Statement>();
   }

   public void execute(Map<String, Configuration> configurations,
         BatfishLogger logger, Settings settings) {
      _environment.setConfigurations(configurations);
      try {
         for (Statement statement : _statements) {
            statement.execute(_environment, logger, settings);
         }
      }
      catch (BatfishException e) {
         throw new BatfishException("Question failed", e);
      }
   }

   public List<Statement> getStatements() {
      return _statements;
   }

   public boolean getUnsafe() {
      return _environment.getUnsafe();
   }

}
