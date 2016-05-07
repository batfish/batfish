package org.batfish.grammar.question;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.question.QuestionParser.QuestionContext;
import org.batfish.main.Settings;

public class QuestionCombinedParser extends
      BatfishCombinedParser<QuestionParser, QuestionLexer> {

   public QuestionCombinedParser(String questionText, Settings settings) {
      super(QuestionParser.class, QuestionLexer.class, questionText, settings);
   }

   @Override
   public QuestionContext parse() {
      return _parser.question();
   }

}
