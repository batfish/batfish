package batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;

import batfish.representation.Protocol;

public abstract class RedistributionPolicy {
   protected final Protocol _destinationProtocol;
   protected final Protocol _sourceProtocol;
   protected final Map<String, Object> _specialAttributes;

   public RedistributionPolicy(Protocol sourceProtocol,
         Protocol destinationProtocol) {
      _sourceProtocol = sourceProtocol;
      _destinationProtocol = destinationProtocol;
      _specialAttributes = new TreeMap<String, Object>();
   }

   public Protocol getDestinationProtocol() {
      return _destinationProtocol;
   }

   public Protocol getSourceProtocol() {
      return _sourceProtocol;
   }

   public Map<String, Object> getSpecialAttributes() {
      return _specialAttributes;
   }

}
