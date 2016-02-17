package org.batfish.grammar.flatvyos;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class FlatVyosCombinedParser extends
      BatfishCombinedParser<FlatVyosParser, FlatVyosLexer> {

   public FlatVyosCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(FlatVyosParser.class, FlatVyosLexer.class, input,
            throwOnParserError, throwOnLexerError);
   }

   @Override
   public ParserRuleContext parse() {
      return _parser.flat_vyos_configuration();
   }

}
