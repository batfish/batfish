package org.batfish.representation.cisco;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public class Vrf extends ComparableStructure<String> {

   public final Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public final Map<String, BgpProcess> getBgpProcesses() {
      return _bgpProcesses;
   }

   private final Map<String, BgpProcess> _bgpProcesses;

   public Vrf(String name) {
      super(name);
      _bgpProcesses = new TreeMap<>();
      _staticRoutes = new HashSet<>();
   }

   public IsisProcess getIsisProcess() {
      return _isisProcess;
   }

   public final OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public void setIsisProcess(IsisProcess isisProcess) {
      _isisProcess = isisProcess;
   }

   public final void setOspfProcess(OspfProcess proc) {
      _ospfProcess = proc;
   }

   private IsisProcess _isisProcess;

   private OspfProcess _ospfProcess;

   private final Set<StaticRoute> _staticRoutes;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

}
