package batfish.representation.cisco;

import batfish.representation.Protocol;

public class BgpRedistributionPolicy extends RedistributionPolicy {

   public static final String OSPF_PROCESS_NUMBER = "OSPF_PROCESS_NUMBER";

   private String _map;
   private Integer _metric;

   public BgpRedistributionPolicy(Protocol sourceProtocol) {
      super(sourceProtocol, Protocol.BGP);
   }

   public String getMap() {
      return _map;
   }

   public Integer getMetric() {
      return _metric;
   }

   public void setMap(String name) {
      _map = name;
   }

   public void setMetric(int metric) {
      _metric = metric;
   }

}
