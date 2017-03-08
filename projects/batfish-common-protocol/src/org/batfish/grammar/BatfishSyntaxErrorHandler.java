package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public abstract class BatfishSyntaxErrorHandler {

   public boolean handle(ParserRuleContext ctx, Object offendingSymbol,
         int line, int charPositionInLine, String msg) {
      return false;
   }

   public boolean handle(Recognizer<?, ?> recognizer, Object offendingSymbol,
         int line, int charPositionInLine, String msg, RecognitionException e) {
      return false;
   }

}
