package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;
import batfish.representation.cisco.BgpProcess;

public class AddressFamilyRBStanza implements RBStanza {
   private BgpAddressFamily _addressFamily;

   public AddressFamilyRBStanza(AFType type) {
      _addressFamily = new BgpAddressFamily(type);
   }

   @Override
   public void process(BgpProcess p) {
      if (_addressFamily.getType() != AFType.IPV4) {
         return;
      }
      p.setDefaultMetric(_addressFamily.getDefaultMetric());
      p.addNetworks(_addressFamily.getNetworks());
      p.addActivatedNeighbors(_addressFamily.getNeighbors());
      p.addPeerGroupRouteReflectorClients(_addressFamily.getRRCPeerGroups());
      p.addSendCommunityPeerGroups(_addressFamily.getSCPeerGroups());
      p.addPeerGroupInboundRouteMaps(_addressFamily.getInboundRouteMaps());
      p.addPeerGroupOutboundRouteMaps(_addressFamily.getOutboundRouteMaps());
      p.addDefaultOriginateNeighbors(_addressFamily
            .getDefaultOriginateNeighbors());
      p.getAggregateNetworks().putAll(_addressFamily.getAggregateNetworks());
      p.setRedistributeStatic(_addressFamily.getRedistributeStatic());
      p.setRedistributeStaticMap(_addressFamily.getRedistributeStaticMap());
      p.addPeerGroupInboundPrefixLists(_addressFamily.getInboundPrefixLists());
      p.addPeerGroupMembership(_addressFamily.getPeerGroupMembership());
   }

   public void processStanza(AFStanza afs) {
      if (afs == null) {
         return;
      }
      afs.process(_addressFamily);
   }

}
