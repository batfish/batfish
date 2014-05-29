package batfish.grammar.cisco.ospf;

import batfish.representation.OspfMetricType;
import batfish.representation.cisco.OspfProcess;

public class DefaultInformationOriginateROStanza implements ROStanza {

   private boolean _always;
   private Integer _metric;
   private Integer _metricType;
   private String _routeMap;

   public DefaultInformationOriginateROStanza(boolean always, Integer metric,
         Integer metricType, String routeMap) {
      _always = always;
      _metric = metric;
      _metricType = metricType;
      _routeMap = routeMap;
   }

   @Override
   public void process(OspfProcess p) {
      p.setDefaultInformationOriginate(true);
      p.setDefaultInformationOriginateAlways(_always);
      if (_metric != null) {
         p.setDefaultInformationMetric(_metric);
      }
      if (_metricType != null) {
         p.setDefaultInformationMetricType(OspfMetricType
               .fromInteger(_metricType));
      }
      p.setDefaultInformationOriginateMap(_routeMap);
   }

}
