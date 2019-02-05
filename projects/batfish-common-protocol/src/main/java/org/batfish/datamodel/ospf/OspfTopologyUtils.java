package org.batfish.datamodel.ospf;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility functions for computing OSPF topology */
public final class OspfTopologyUtils {
  public static void initRemoteOspfNeighbors(
      Map<String, Configuration> configurations, Topology topology) {
    for (Entry<String, Configuration> e : configurations.entrySet()) {
      String hostname = e.getKey();
      Configuration c = e.getValue();
      for (Entry<String, Vrf> e2 : c.getVrfs().entrySet()) {
        Vrf vrf = e2.getValue();
        OspfProcess proc = vrf.getOspfProcess();
        if (proc != null) {
          proc.setOspfNeighbors(new TreeMap<>());
          String vrfName = e2.getKey();
          for (Entry<Long, OspfArea> e3 : proc.getAreas().entrySet()) {
            long areaNum = e3.getKey();
            OspfArea area = e3.getValue();
            for (String ifaceName : area.getInterfaces()) {
              Interface iface = c.getAllInterfaces().get(ifaceName);
              if (iface.getOspfPassive()) {
                continue;
              }
              Set<NodeInterfacePair> ifaceNeighbors =
                  topology.getNeighbors(new NodeInterfacePair(hostname, ifaceName));
              boolean hasOspfNeighbor = false;
              Ip localIp = iface.getAddress().getIp();
              if (ifaceNeighbors != null) {
                for (NodeInterfacePair neighbor : ifaceNeighbors) {
                  String remoteHostname = neighbor.getHostname();
                  String remoteIfaceName = neighbor.getInterface();
                  Configuration remoteNode = configurations.get(remoteHostname);
                  Interface remoteIface = remoteNode.getAllInterfaces().get(remoteIfaceName);
                  if (remoteIface.getOspfPassive()) {
                    continue;
                  }
                  Vrf remoteVrf = remoteIface.getVrf();
                  String remoteVrfName = remoteVrf.getName();
                  OspfProcess remoteProc = remoteVrf.getOspfProcess();
                  if (remoteProc != null) {
                    if (remoteProc.getOspfNeighbors() == null) {
                      remoteProc.setOspfNeighbors(new TreeMap<>());
                    }
                    OspfArea remoteArea = remoteProc.getAreas().get(areaNum);
                    if (remoteArea != null
                        && remoteArea.getInterfaces().contains(remoteIfaceName)) {
                      Ip remoteIp = remoteIface.getAddress().getIp();
                      IpLink localKey = new IpLink(localIp, remoteIp);
                      OspfNeighbor ospfNeighbor = proc.getOspfNeighbors().get(localKey);
                      if (ospfNeighbor == null) {
                        hasOspfNeighbor = true;

                        // initialize local ospfNeighbor
                        ospfNeighbor = new OspfNeighbor(localKey);
                        ospfNeighbor.setArea(areaNum);
                        ospfNeighbor.setVrf(vrfName);
                        ospfNeighbor.setOwner(c);
                        ospfNeighbor.setInterface(iface);
                        proc.getOspfNeighbors().put(localKey, ospfNeighbor);

                        // initialize remote ospfNeighbor
                        IpLink remoteKey = new IpLink(remoteIp, localIp);
                        OspfNeighbor remoteNeighbor = new OspfNeighbor(remoteKey);
                        remoteNeighbor.setArea(areaNum);
                        remoteNeighbor.setVrf(remoteVrfName);
                        remoteNeighbor.setOwner(remoteNode);
                        remoteNeighbor.setInterface(remoteIface);
                        remoteProc.getOspfNeighbors().put(remoteKey, remoteNeighbor);

                        // link neighbors
                        ospfNeighbor.setRemoteOspfNeighbor(remoteNeighbor);
                        remoteNeighbor.setRemoteOspfNeighbor(ospfNeighbor);
                      }
                    }
                  }
                }
              }
              if (!hasOspfNeighbor) {
                IpLink key = new IpLink(localIp, Ip.ZERO);
                OspfNeighbor neighbor = new OspfNeighbor(key);
                neighbor.setArea(areaNum);
                neighbor.setVrf(vrfName);
                neighbor.setOwner(c);
                neighbor.setInterface(iface);
                proc.getOspfNeighbors().put(key, neighbor);
              }
            }
          }
        }
      }
    }
  }

  private OspfTopologyUtils() {}
}
