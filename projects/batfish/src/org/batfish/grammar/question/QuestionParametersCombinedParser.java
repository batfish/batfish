package org.batfish.grammar.question;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.question.QuestionParametersParser.ParametersContext;
import org.batfish.main.Settings;

public class QuestionParametersCombinedParser extends
      BatfishCombinedParser<QuestionParametersParser, QuestionParametersLexer> {

   public QuestionParametersCombinedParser(String questionText,
         Settings settings) {
      super(QuestionParametersParser.class, QuestionParametersLexer.class,
            questionText, settings);
   }

   @Override
   public ParametersContext parse() {
      return _parser.parameters();
   }

}
