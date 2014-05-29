package batfish.grammar.juniper.bgp;

public class PeerASGBStanza extends GBStanza {
   private int _peerASNum;

   public PeerASGBStanza(int a) {
      _peerASNum = a;
   }

   public int getPeerASNum() {
      return _peerASNum;
   }

   @Override
   public GBType getType() {
      return GBType.PEER_AS;
   }

}
