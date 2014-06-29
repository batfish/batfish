package batfish.grammar.cisco;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
//import batfish.representation.cisco.PrefixListLine;
//import batfish.util.SubRange;

public class PrefixListLineStanza implements Stanza {

//   private PrefixListLine _line;
//   private String _prefixListName;

   public PrefixListLineStanza(String prefixListName, LineAction action,
         String prefix, int prefix_length, int min_prefix_length,
         int max_prefix_length) {
//      _prefixListName = prefixListName;
//      _line = new PrefixListLine(action, prefix, prefix_length, new SubRange(
//            min_prefix_length, max_prefix_length));
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
//      c.addPrefixListLine(_prefixListName, _line);
   }

}
