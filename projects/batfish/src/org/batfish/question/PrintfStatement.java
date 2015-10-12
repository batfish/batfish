package org.batfish.question;

import java.util.ArrayList;
import java.util.List;

import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class PrintfStatement implements Statement {

   private PrintableExpr _formatString;

   private List<PrintableExpr> _replacements;

   public PrintfStatement(PrintableExpr formatString,
         List<PrintableExpr> replacements) {
      _formatString = formatString;
      _replacements = replacements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      String formatString = _formatString.print(environment);
      List<Object> replacements = new ArrayList<Object>();
      for (PrintableExpr replacementExpr : _replacements) {
         String replacement = replacementExpr.print(environment);
         replacements.add(replacement);
      }
      logger.outputf(formatString, replacements.toArray());
   }

}
