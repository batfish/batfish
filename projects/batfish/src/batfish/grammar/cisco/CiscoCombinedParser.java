package batfish.grammar.cisco;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.cisco.CiscoGrammar.Cisco_configurationContext;

public class CiscoCombinedParser extends BatfishCombinedParser {

   private CiscoGrammarCommonLexer _lexer;
   private CommonTokenStream _tokens;
   private CiscoGrammar _parser;

   public CiscoCombinedParser(String input) {
      ANTLRInputStream inputStream = new ANTLRInputStream(input);
      _lexer = new CiscoGrammarCommonLexer(inputStream);
      _lexer.initErrorListener(this);
      _tokens = new CommonTokenStream(_lexer);
      _parser = new CiscoGrammar(_tokens);
      _parser.initErrorListener(this);
      _parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
   }

   @Override
   public Cisco_configurationContext parse() {
      return _parser.cisco_configuration();
   }

   @Override
   public CiscoGrammar getParser() {
      return _parser;
   }

   @Override
   public CiscoGrammarCommonLexer getLexer() {
      return _lexer;
   }

   @Override
   public CommonTokenStream getTokens() {
      return _tokens;
   }

}
