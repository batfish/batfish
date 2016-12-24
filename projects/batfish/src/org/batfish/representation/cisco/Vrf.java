package org.batfish.representation.cisco;

import java.util.HashSet;
import java.util.Set;

import org.batfish.common.util.ComparableStructure;

public final class Vrf extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private BgpProcess _bgpProcess;

   private IsisProcess _isisProcess;

   private OspfProcess _ospfProcess;

   private final Set<StaticRoute> _staticRoutes;

   public Vrf(String name) {
      super(name);
      _staticRoutes = new HashSet<>();
   }

   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public IsisProcess getIsisProcess() {
      return _isisProcess;
   }

   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public void setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
   }

   public void setIsisProcess(IsisProcess isisProcess) {
      _isisProcess = isisProcess;
   }

   public void setOspfProcess(OspfProcess proc) {
      _ospfProcess = proc;
   }

}
