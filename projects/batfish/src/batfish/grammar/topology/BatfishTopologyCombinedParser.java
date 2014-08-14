package batfish.grammar.topology;


import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.topology.BatfishTopologyParser.TopologyContext;

public class BatfishTopologyCombinedParser extends BatfishCombinedParser<BatfishTopologyParser, BatfishTopologyLexer> {

   public BatfishTopologyCombinedParser(String input) {
      super(BatfishTopologyParser.class, BatfishTopologyLexer.class, input);
   }
   
   @Override
   public TopologyContext parse() {
      return _parser.topology();
   }

}
