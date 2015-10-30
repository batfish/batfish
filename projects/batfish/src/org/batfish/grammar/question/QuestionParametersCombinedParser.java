package org.batfish.grammar.question;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.question.QuestionParametersParser.ParametersContext;

public class QuestionParametersCombinedParser extends
      BatfishCombinedParser<QuestionParametersParser, QuestionParametersLexer> {

   public QuestionParametersCombinedParser(String questionText,
         boolean throwOnParserError, boolean throwOnLexerError) {
      super(QuestionParametersParser.class, QuestionParametersLexer.class,
            questionText, throwOnParserError, throwOnLexerError);
   }

   @Override
   public ParametersContext parse() {
      return _parser.parameters();
   }

}
