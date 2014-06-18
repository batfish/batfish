package batfish.grammar.juniper.firewall;

import java.util.List;

import batfish.representation.LineAction;
import batfish.util.SubRange;

public class TermFFStanza {
   private String _name;
   private List<String> _destinationAddress;
   private List<String> _sourceAddress;
   private List<String> _sourceExceptAddress;
   private List<SubRange> _destPorts;
   private List<SubRange> _sourcePorts;
   private List<Integer> _protocols;
   private LineAction _ala;

   public TermFFStanza(String n) {
      _name = n;
      _ala = null;
   }

   public void processFromStanza(FromTFFStanza fs) {
      switch (fs.getType()) {
      case DESTINATION_ADDRESS:
         DestinationAddressFromTFFStanza dfs = (DestinationAddressFromTFFStanza) fs;
         _destinationAddress = dfs.getAddress();
         break;

      case SOURCE_ADDRESS:
         SourceAddressFromTFFStanza sfs = (SourceAddressFromTFFStanza) fs;
         _sourceAddress = sfs.getAddress();
         _sourceExceptAddress = sfs.getExceptAddress();
         break;
         
      case SOURCE_PORT:
         SourcePortFromTFFStanza spfs = (SourcePortFromTFFStanza) fs;
         _sourcePorts = spfs.getPorts();
         break;

      case DESTINATION_PORT:
         DestinationPortFromTFFStanza dpfs = (DestinationPortFromTFFStanza) fs;
         _destPorts = dpfs.getPorts();
         break;

      case PROTOCOL:
         ProtocolFromTFFStanza prfs = (ProtocolFromTFFStanza) fs;
         _protocols = prfs.getProtocols();
         break;

      default:
         throw new Error("bad firewall term from stanza type");
      }
   }

   public void processThenStanza(ThenTFFStanza ts) {
      switch (ts.getType()) {
      case ACCEPT:
         _ala = LineAction.ACCEPT;
         break;

      case DISCARD:
         _ala = LineAction.REJECT;
         break;

      case NEXT_TERM:
      case NULL:
         break;

      default:
         throw new Error("bad firewall term then stanza type");
      }
   }

   public String getName() {
      return _name;
   }

   public List<String> getDestinationAddress() {
      return _destinationAddress;
   }

   public List<String> getSourceExceptAddress() {
      return _sourceExceptAddress;
   }
   
   public List<String> getSourceAddress() {
      return _sourceAddress;
   }

   public List<SubRange> getPorts() {
      return _destPorts;
   }
   
   public List<SubRange> getSourcePorts() {
      return _sourcePorts;
   }

   public List<Integer> getProtocols() {
      return _protocols;
   }

   public LineAction getLineAction() {
      return _ala;
   }

}
