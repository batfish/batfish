package batfish.grammar.topology;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.topology.GNS3TopologyParser.TopologyContext;

public class GNS3TopologyCombinedParser extends BatfishCombinedParser {

   private GNS3TopologyLexer _lexer;
   private GNS3TopologyParser _parser;

   public GNS3TopologyCombinedParser(String input) {
      ANTLRInputStream inputStream = new ANTLRInputStream(input);
      _lexer = new GNS3TopologyLexer(inputStream);
      _lexer.initErrorListener(this);
      _tokens = new CommonTokenStream(_lexer);
      _parser = new GNS3TopologyParser(_tokens);
      _parser.initErrorListener(this);
      _parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
   }
   
   @Override
   public GNS3TopologyLexer getLexer() {
      return _lexer;
   }

   @Override
   public GNS3TopologyParser getParser() {
      return _parser;
   }

   @Override
   public TopologyContext parse() {
      return _parser.topology();
   }

}
