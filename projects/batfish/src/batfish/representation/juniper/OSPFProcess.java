package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

public class OSPFProcess {
	
   private int _pid;
   private ArrayList<OSPFNetwork> _networks;
   private String _routerId;
   private double _referenceBandwidth;
   private List<String> _exportPolicyStatements;
   
   /* ------------------------------ Constructor ----------------------------*/
   public OSPFProcess(int procnum) {
      _pid = procnum;
      _referenceBandwidth = 0;
      _networks = new ArrayList<OSPFNetwork>();
      _exportPolicyStatements = new ArrayList<String>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addNetwork(String networkAddress, String subnetMask, int area) {
      _networks.add(new OSPFNetwork(networkAddress, subnetMask, area));  
   }
   public void addNetworkByInterface(String inf, int area) {
      _networks.add(new OSPFNetwork(inf, area));      
   }
   public void addExportPolicyStatements(List<String> ps){
      _exportPolicyStatements.addAll(ps);
   }
   public void set_routerId (String s) {
	   _routerId = s;
   }
   public void set_referenceBandwidth (double d) {
	   _referenceBandwidth = d;
   }
   public List<String> get_exportPolicyStatements () {
	   return _exportPolicyStatements;
   }
   public ArrayList<OSPFNetwork> get_networks () {
	   return _networks;
   }
   public String get_routerId () {
	   return _routerId;
   }
   public double get_referenceBandwidth () {
	   return _referenceBandwidth;
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
   
}
