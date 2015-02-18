package org.batfish.representation;

import java.util.List;

/**
 * Instances of this class represent hypothetical BGP advertisements used for
 * testing, or where the config of an advertising border router is unavailable
 *
 * @author arifogel
 *
 */

public class BgpAdvertisement {
   // / [BGPAdvertisement(destIpBlock, nextHopIp, asPath, localPref, srcIp,
   // dstIp)] =
   // / An advertisement of routes to ip block [destIpBlock] with next hop of ip
   // / [nextHopIp] through path [asPath] with metric [metric] should be sent by
   // / host [srcHost] at ip [srcIp] to host [dstHost] at [dstIp].

   private List<Integer> _asPath;
   private String _destIpBlock;
   private String _dstIp;
   private int _metric;
   private String _nextHopIp;
   private String _srcIp;

   public BgpAdvertisement(String destIpBlock, String nextHopIp,
         List<Integer> asPath, int metric, String srcIp, String dstIp) {
      _destIpBlock = destIpBlock;
      _nextHopIp = nextHopIp;
      _asPath = asPath;
      _metric = metric;
      _srcIp = srcIp;
      _dstIp = dstIp;
   }

   public List<Integer> getAsPath() {
      return _asPath;
   }

   public String getDestIpBlock() {
      return _destIpBlock;
   }

   public String getDstIp() {
      return _dstIp;
   }

   public int getMetric() {
      return _metric;
   }

   public String getNextHopIp() {
      return _nextHopIp;
   }

   public String getSrcIp() {
      return _srcIp;
   }

}
