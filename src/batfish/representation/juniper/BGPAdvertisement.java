package batfish.representation.juniper;

import java.util.List;

/**
 * Instances of this class represent hypothetical BGP advertisements used for
 * testing, or where the config of an advertising border router is unavailable
 * 
 * @author arifogel
 * 
 */

public class BGPAdvertisement {
   // / [BGPAdvertisement(destIpBlock, nextHopIp, asPath, localPref, srcIp,
   // dstIp)] =
   // / An advertisement of routes to ip block [destIpBlock] with next hop of ip
   // / [nextHopIp] through path [asPath] with metric [metric] should be sent by
   // / host [srcHost] at ip [srcIp] to host [dstHost] at [dstIp].

   private String _destIpBlock;
   private String _nextHopIp;
   private List<Integer> _asPath;
   private int _metric;
   private String _srcIp;
   private String _dstIp;

   public BGPAdvertisement(String destIpBlock, String nextHopIp,
         List<Integer> asPath, int metric, String srcIp, String dstIp) {
      _destIpBlock = destIpBlock;
      _nextHopIp = nextHopIp;
      _asPath = asPath;
      _metric = metric;
      _srcIp = srcIp;
      _dstIp = dstIp;
   }

   public String getDestIpBlock() {
      return _destIpBlock;
   }

   public String getNextHopIp() {
      return _nextHopIp;
   }

   public List<Integer> getAsPath() {
      return _asPath;
   }

   public int getMetric() {
      return _metric;
   }

   public String getSrcIp() {
      return _srcIp;
   }

   public String getDstIp() {
      return _dstIp;
   }

}
