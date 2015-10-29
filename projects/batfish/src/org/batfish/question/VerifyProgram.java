package org.batfish.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.statement.Statement;
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

   public boolean getAssertions() {
      return _environment.getAssertions();
   }

   public int getFailedAssertions() {
      return _environment.getFailedAssertions();
   }

   public List<Statement> getStatements() {
      return _statements;
   }

   public int getTotalAssertions() {
      return _environment.getTotalAssertions();
   }

   public boolean getUnsafe() {
      return _environment.getUnsafe();
   }

}
