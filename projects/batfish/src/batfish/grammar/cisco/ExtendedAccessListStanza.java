package batfish.grammar.cisco;

import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.ExtendedAccessListLine;
import batfish.util.SubRange;

public class ExtendedAccessListStanza implements Stanza {

   private ExtendedAccessListLine _accessListLine;
   private String _id;

   public ExtendedAccessListStanza(LineAction action, String id, int protocol,
         String srcIp, String srcWildcard, String dstIp, String dstWildcard,
         List<SubRange> srcPortRanges, List<SubRange >dstPortRanges) {
      _id = id;
      _accessListLine = new ExtendedAccessListLine(action, protocol, srcIp,
            srcWildcard, dstIp, dstWildcard, srcPortRanges, dstPortRanges);
   }

   @Override
   public void process(CiscoVendorConfiguration c) {
      c.addExtendedAccessListLine(_id, _accessListLine);
   }

}
