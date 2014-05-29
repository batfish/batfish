package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpAddressFamily;

public class DefaultMetricAFStanza implements AFStanza {

   private int _metric;

   public DefaultMetricAFStanza(int metric) {
      _metric = metric;
   }

   @Override
   public void process(BgpAddressFamily af) {
      af.setDefaultMetric(_metric);
   }

}
