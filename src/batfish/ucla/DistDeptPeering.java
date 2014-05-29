package batfish.ucla;

import batfish.representation.cisco.PrefixList;

public class DistDeptPeering {

   private String _distIp;
   private String _distName;
   private String _ip;
   private PrefixList _prefixList;
   private String _subnet;

   public String getDistIp() {
      return _distIp;
   }

   public String getDistName() {
      return _distName;
   }

   public String getIp() {
      return _ip;
   }

   public PrefixList getPrefixList() {
      return _prefixList;
   }

   public String getPrefixListName() {
      return "pl-" + _distIp.replace(".", "-");
   }
   
   public String getSubnet() {
      return _subnet;
   }

   public void setDistIp(String distrIp) {
      _distIp = distrIp;
   }

   public void setDistName(String name) {
      _distName = name;
   }

   public void setIp(String ip) {
      _ip = ip;
   }

   public void setPrefixList(PrefixList prefixList) {
      _prefixList = prefixList;
   }

   public void setSubnet(String subnet) {
      _subnet = subnet;
   }

   public String getRouteMapName() {
      return "out-" + _distIp.replace(".", "-");
   }

}
