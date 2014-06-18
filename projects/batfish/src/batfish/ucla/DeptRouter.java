package batfish.ucla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import batfish.representation.Ip;
import batfish.representation.cisco.PrefixList;
import batfish.representation.cisco.PrefixListLine;
import batfish.util.NamedStructure;
import batfish.util.Util;

public class DeptRouter extends NamedStructure {
   private int _as;
   private Map<Ip, Ip> _deptNetworks;
   private Map<Ip, Ip> _deptNetworksWithWildcard; // value is wildcard
   private List<DistDeptPeering> _peerings;
   private Map<Ip, Ip> _subgroupNetworks; // value is subnet
   
   public DeptRouter(String name, int as) {
      super(name);
      _peerings = new ArrayList<DistDeptPeering>();
      _as = as;
      _deptNetworksWithWildcard = new TreeMap<Ip, Ip>();
   }

   public void computeDeptNetworks() {
      Map<Ip, Ip> deptNetworksWithSubnet = new HashMap<Ip, Ip>();
      for (DistDeptPeering peering : _peerings) {
         PrefixList pl = peering.getPrefixList();
         if (pl != null) {
            for (PrefixListLine line : pl.getLines()) {
               Ip ip = new Ip(line.getPrefix());
               long subnetLong = Util.numSubnetBitsToSubnetInt(line
                     .getPrefixLength());
               Ip subnet = new Ip(Util.longToIp(subnetLong));
               int numWildcardBits = 32 - line.getPrefixLength();
               long wildcardLong = Util
                     .numWildcardBitsToWildcardLong(numWildcardBits);
               Ip wildcard = new Ip(Util.longToIp(wildcardLong));
               _deptNetworksWithWildcard.put(ip, wildcard);
               deptNetworksWithSubnet.put(ip, subnet);
            }
         }
      }
      _deptNetworks = deptNetworksWithSubnet;
   }

   public Map<Ip, Ip> getDeptNetworks() {
      return _deptNetworks;
   }

   public String getInterfaceFilterName() {
      return "filter-in-" + _as;
   }

   public int getNumFlowSinkInterfaces() {
      return _deptNetworks.size();
   }

   public List<DistDeptPeering> getPeerings() {
      return _peerings;
   }

   public void setSubgroupNetworks(Map<Ip, Ip> subgroupNetworks) {
      _subgroupNetworks = subgroupNetworks;
   }
      
   public String toConfigString() {
      StringBuilder sb = new StringBuilder();
      sb.append("!\n");
      sb.append("hostname " + _name + "\n");
      sb.append("!\n");
      sb.append("!\n");
      for (int i = 0; i < _peerings.size(); i++) {
         DistDeptPeering peering = _peerings.get(i);
         sb.append("interface TenGigabitEthernet0/" + i + "\n");
         sb.append(" description " + peering.getDistName() + "\n");
         sb.append(" ip address " + peering.getIp() + " " + peering.getSubnet()
               + "\n");
         sb.append(" ip access-group " + getInterfaceFilterName() + " in\n");
         sb.append("!\n");
      }

      Map<Ip, Ip> otherNetworks = new TreeMap<Ip, Ip>();
      otherNetworks.putAll(_subgroupNetworks);
      otherNetworks.keySet().removeAll(_deptNetworks.keySet());
      
      int deptIntNum = 0;
      for (Ip network : _deptNetworks.keySet()) {
         Ip subnet = _deptNetworks.get(network);
         sb.append("interface " + DeptGenerator.FLOW_SINK_INTERFACE_PREFIX + deptIntNum + "\n");
         sb.append(" ip address " + network + " " + subnet + "\n");
         sb.append("!\n");
         deptIntNum++;
      }

      int fakeNum = 0;
      for (Ip network : otherNetworks.keySet()) {
         Ip subnet = otherNetworks.get(network);
         sb.append("interface " + DeptGenerator.FAKE_INTERFACE_PREFIX + fakeNum + "\n");
         sb.append(" ip address " + network + " " + subnet + "\n");
         sb.append("!\n");
         fakeNum++;
      }

      sb.append("!\n");
      sb.append("!\n");
      sb.append("router bgp " + _as + "\n");
      for (Ip network : _subgroupNetworks.keySet()) {
         Ip subnet = _subgroupNetworks.get(network);
         sb.append(" network " + network + " mask " + subnet + "\n");
      }
      for (DistDeptPeering peering : _peerings) {
         sb.append(" neighbor " + peering.getDistIp() + " remote-as 52\n");
      }
      sb.append(" address-family ipv4\n");
      for (DistDeptPeering peering : _peerings) {
         PrefixList pl = peering.getPrefixList();
         if (pl != null) {
            sb.append("  neighbor " + peering.getDistIp() + " route-map "
                  + peering.getRouteMapName() + " out\n");
         }
      }
      sb.append(" exit-address-family\n");
      sb.append("!\n");
      sb.append("!\n");
      for (DistDeptPeering peering : _peerings) {
         if (peering.getPrefixList() != null) {
            sb.append("route-map " + peering.getRouteMapName()
                  + " permit 100\n");
            sb.append(" match ip address prefix-list "
                  + peering.getPrefixListName() + "\n");
            sb.append("!\n");
         }
      }
      sb.append("!\n");
      for (DistDeptPeering peering : _peerings) {
         PrefixList pl = peering.getPrefixList();
         if (pl != null) {
            for (PrefixListLine line : pl.getLines()) {
               sb.append("ip prefix-list " + peering.getPrefixListName()
                     + " permit " + line.getPrefix() + "/"
                     + line.getPrefixLength() + " le 32\n");
            }
            sb.append("!\n");
         }
      }
      sb.append("!\n");
      sb.append("ip access-list extended " + getInterfaceFilterName() + "\n");
      for (Ip network : _deptNetworksWithWildcard.keySet()) {
         Ip wildcard = _deptNetworksWithWildcard.get(network);
         sb.append(" permit ip any " + network + " " + wildcard + "\n");
      }
      sb.append(" deny ip any any\n");
      sb.append("!\n");
      sb.append("!\n");
      sb.append("end\n");
      return sb.toString();
   }

   @Override
   public String toString() {
      return "DeptRouter<" + _name + ">";
   }

}
