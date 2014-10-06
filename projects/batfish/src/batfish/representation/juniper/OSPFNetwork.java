package batfish.representation.juniper;

import java.io.Serializable;

public class OSPFNetwork implements Serializable {

   private static final long serialVersionUID = 1L;


   private String _networkAddress;
   private String _subnetMask;
   private String _interface;
   private int _areaNum;
   
   /* ------------------------------ Constructor ----------------------------*/
   public OSPFNetwork(String networkAddress, String subnetMask, int area) {
      _networkAddress = networkAddress;
      _subnetMask = subnetMask;
      _areaNum = area;
   }
   public OSPFNetwork(String inf, int area){
      _interface = inf;
      _areaNum = area;
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_areaNum () {
	   return _areaNum;
   }
   public String get_subnetMask () {
	   return _subnetMask;
   }
   public String get_networkAddress () {
	   return _networkAddress;
   }
   public String get_interface () {
	   return _interface;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  

}
