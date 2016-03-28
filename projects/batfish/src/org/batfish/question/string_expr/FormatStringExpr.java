package org.batfish.question.string_expr;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public final class FormatStringExpr extends BaseStringExpr implements
      StringExpr {

   private final StringExpr _formatString;

   private final List<Expr> _replacements;

   public FormatStringExpr(StringExpr formatString, List<Expr> replacements) {
      _formatString = formatString;
      _replacements = replacements;
   }

   @Override
   public String evaluate(Environment environment) {
      String formatString = _formatString.print(environment);
      List<Object> replacements = new ArrayList<Object>();
      for (Expr replacementExpr : _replacements) {
         String replacement = replacementExpr.print(environment);
         replacements.add(replacement);
      }
      try {
         String output = String.format(formatString, replacements.toArray());
         return output;
      }
      catch (MissingFormatArgumentException e) {
         throw new BatfishException("error on format string: \"" + formatString
               + "\" with arguments: " + replacements.toString());
      }
   }

}
