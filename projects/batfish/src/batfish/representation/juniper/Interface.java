package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.SwitchportMode;
import batfish.util.SubRange;

public class Interface {
	

   private String _name;
   private String _ip;
   private boolean _active;
   private int _nativeVlan;
   private SwitchportMode _switchportMode;
   private ArrayList<SubRange> _allowedVlans;
   private Integer _ospfCost;

   private String _subnet;
   private Double _bandwidth;
   private int _accessVlan;  // TODO : I don't see this ever getting set
   private int _ospfDeadInterval;// TODO : I don't see this ever getting set
   private int _ospfHelloMultiplier; // TODO : I don't see this ever getting set
   private SwitchportEncapsulationType _switchportTrunkEncapsulation; // TODO : I don't see this ever getting set
   
   /* ------------------------------ Constructor ----------------------------*/
   public Interface(String name) {
	      _name = name;
	      _ip = "";
	      _active = true;
	      _nativeVlan = 1;
	      _switchportMode = SwitchportMode.NONE;
	      _allowedVlans = new ArrayList<SubRange>();
	      _ospfCost = null;
	   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/ 
   public void set_active (boolean b) {
	   _active = b;
   }  
   public void set_subnet (String s) {
	   _subnet = s;
   }
   public void set_bandwidth (Double d) {
	   _bandwidth = d;
   }
   public void set_ip (String ip) {
	   _ip = ip;
   }
   public boolean get_active () {
	   return _active;
   }
   public int get_accessVlan () {
	   return _accessVlan;
   }
   public String get_name () { 
	   return _name;
   }
   public String get_subnet () {
	   return _subnet;
   }
   public Double get_bandwidth () {
	   return _bandwidth;
   }
   public String get_ip () {
	   return _ip;
   }
   public int get_nativeVlan () {
	   return _nativeVlan;
   }
   public Integer get_ospfCost () {
	   return _ospfCost;
   }
   public int get_ospfDeadInterval () {
	   return _ospfDeadInterval;
   }
   public int get_ospfHelloMultiplier () {
	   return _ospfHelloMultiplier;
   }
   public SwitchportEncapsulationType get_switchportTrunkEncapsulation () {
	   return _switchportTrunkEncapsulation;
   }
   /* --------------------------- Inherited Methods -------------------------*/  
   

  /* public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }
*/

}
