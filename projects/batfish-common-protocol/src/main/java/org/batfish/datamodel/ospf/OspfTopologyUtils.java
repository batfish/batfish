package org.batfish.datamodel.ospf;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
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
              SortedSet<Edge> ifaceEdges =
                  topology.getInterfaceEdges().get(new NodeInterfacePair(hostname, ifaceName));
              boolean hasNeighbor = false;
              Ip localIp = iface.getAddress().getIp();
              if (ifaceEdges != null) {
                for (Edge edge : ifaceEdges) {
                  if (edge.getNode1().equals(hostname)) {
                    String remoteHostname = edge.getNode2();
                    String remoteIfaceName = edge.getInt2();
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
                        OspfNeighbor neighbor = proc.getOspfNeighbors().get(localKey);
                        if (neighbor == null) {
                          hasNeighbor = true;

                          // initialize local neighbor
                          neighbor = new OspfNeighbor(localKey);
                          neighbor.setArea(areaNum);
                          neighbor.setVrf(vrfName);
                          neighbor.setOwner(c);
                          neighbor.setInterface(iface);
                          proc.getOspfNeighbors().put(localKey, neighbor);

                          // initialize remote neighbor
                          IpLink remoteKey = new IpLink(remoteIp, localIp);
                          OspfNeighbor remoteNeighbor = new OspfNeighbor(remoteKey);
                          remoteNeighbor.setArea(areaNum);
                          remoteNeighbor.setVrf(remoteVrfName);
                          remoteNeighbor.setOwner(remoteNode);
                          remoteNeighbor.setInterface(remoteIface);
                          remoteProc.getOspfNeighbors().put(remoteKey, remoteNeighbor);

                          // link neighbors
                          neighbor.setRemoteOspfNeighbor(remoteNeighbor);
                          remoteNeighbor.setRemoteOspfNeighbor(neighbor);
                        }
                      }
                    }
                  }
                }
              }
              if (!hasNeighbor) {
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
