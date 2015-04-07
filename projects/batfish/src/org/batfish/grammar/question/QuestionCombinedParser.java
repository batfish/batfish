package org.batfish.grammar.question;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.question.QuestionParser.QuestionContext;

public class QuestionCombinedParser extends
      BatfishCombinedParser<QuestionParser, QuestionLexer> {

   public QuestionCombinedParser(String questionText,
         boolean throwOnParserError, boolean throwOnLexerError) {
      super(QuestionParser.class, QuestionLexer.class, questionText,
            throwOnParserError, throwOnLexerError);
   }

   @Override
   public QuestionContext parse() {
      return _parser.question();
   }

}
