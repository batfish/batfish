package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import batfish.grammar.cisco.CiscoGrammar.Template_peer_stanzaContext;
import batfish.representation.Ip;

public class BgpPeerTemplatePeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;
   
   private transient Template_peer_stanzaContext _context;

   protected String _name;
   private Set<Ip> _neighborAddresses;
   
   public BgpPeerTemplatePeerGroup(String name) {
      _neighborAddresses = new LinkedHashSet<Ip>();
      _routeReflectorClient = false;
      _defaultOriginate = false;
      _sendCommunity = false;
      _name = name;
   }
  
   public Template_peer_stanzaContext getContext() {
      return _context;
   }

   @Override
   public String getName() {
      return _name.toString();
   }
   
   public Set<Ip> getNeighborAddresses() {
      return _neighborAddresses;
   }
   
   public void addNeighborAddress(Ip address) {
      _neighborAddresses.add(address);
   }
   
   public void setContext(Template_peer_stanzaContext ctx) {
      _context = ctx;
   }

   public void setDefaultOriginate(boolean b) {
      _defaultOriginate = true;
   }
   
}
