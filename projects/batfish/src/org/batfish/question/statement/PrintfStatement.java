package org.batfish.question.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.string_expr.StringExpr;

public class PrintfStatement implements Statement {

   private StringExpr _formatString;

   private List<Expr> _replacements;

   public PrintfStatement(StringExpr formatString, List<Expr> replacements) {
      _formatString = formatString;
      _replacements = replacements;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      String formatString = _formatString.print(environment);
      List<Object> replacements = new ArrayList<Object>();
      for (Expr replacementExpr : _replacements) {
         String replacement = replacementExpr.print(environment);
         replacements.add(replacement);
      }
      try {
         logger.outputf(formatString, replacements.toArray());
      }
      catch (MissingFormatArgumentException e) {
         throw new BatfishException("printf error on format string: \""
               + formatString + "\" with arguments: " + replacements.toString());
      }
   }

}
