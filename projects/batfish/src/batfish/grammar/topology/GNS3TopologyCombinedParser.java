package batfish.grammar.topology;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.topology.GNS3TopologyParser.TopologyContext;

public class GNS3TopologyCombinedParser extends
      BatfishCombinedParser<GNS3TopologyParser, GNS3TopologyLexer> {

   public GNS3TopologyCombinedParser(String input, boolean throwOnParserError,
         boolean throwOnLexerError) {
      super(GNS3TopologyParser.class, GNS3TopologyLexer.class, input,
            throwOnParserError, throwOnLexerError);
   }

   @Override
   public TopologyContext parse() {
      return _parser.topology();
   }

}
