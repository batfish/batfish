package batfish.representation.cisco;

public class MasterBgpPeerGroup extends BgpPeerGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String MASTER_BGP_PEER_GROUP_NAME = "~MASTER_BGP_PEER_GROUP~";

   public MasterBgpPeerGroup() {
      _defaultOriginate = false;
      _routeReflectorClient = false;
      _sendCommunity = false;
   }

   @Override
   public String getName() {
      return MASTER_BGP_PEER_GROUP_NAME;
   }

}
