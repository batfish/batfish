package batfish.grammar.topology;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.topology.BatfishTopologyParser.TopologyContext;

public class BatfishTopologyCombinedParser extends BatfishCombinedParser {

   private BatfishTopologyLexer _lexer;
   private BatfishTopologyParser _parser;

   public BatfishTopologyCombinedParser(String input) {
      ANTLRInputStream inputStream = new ANTLRInputStream(input);
      _lexer = new BatfishTopologyLexer(inputStream);
      _lexer.initErrorListener(this);
      _tokens = new CommonTokenStream(_lexer);
      _parser = new BatfishTopologyParser(_tokens);
      _parser.initErrorListener(this);
      _parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
   }
   
   @Override
   public BatfishTopologyLexer getLexer() {
      return _lexer;
   }

   @Override
   public BatfishTopologyParser getParser() {
      return _parser;
   }

   @Override
   public TopologyContext parse() {
      return _parser.topology();
   }

}
