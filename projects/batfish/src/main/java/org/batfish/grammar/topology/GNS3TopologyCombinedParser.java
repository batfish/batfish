package org.batfish.grammar.topology;

import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.topology.GNS3TopologyParser.TopologyContext;

public class GNS3TopologyCombinedParser
      extends BatfishCombinedParser<GNS3TopologyParser, GNS3TopologyLexer> {

   public GNS3TopologyCombinedParser(String input, Settings settings) {
      super(GNS3TopologyParser.class, GNS3TopologyLexer.class, input, settings);
   }

   @Override
   public TopologyContext parse() {
      return _parser.topology();
   }

}
