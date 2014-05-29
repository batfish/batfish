package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BGPProcess {

   private int _pid;
   private String _routerID;
   private Map<String, BGPGroup> _peerGroups;
   private ArrayList<String> _activatedNeighbors;

   public BGPProcess(int procnum) {
      _pid = procnum;
      _peerGroups = new HashMap<String, BGPGroup>();
      _activatedNeighbors = new ArrayList<String>();
   }

   public int getPid() {
      return _pid;
   }
   
   public String getRouterID() {
      return _routerID;
   }

   public Map<String, BGPGroup> getAllPeerGroups() {
      return _peerGroups;
   }

   public BGPGroup getPeerGroup(String name) {
      return _peerGroups.get(name);
   }

   public void addPeerGroup(BGPGroup peerGroup) {
      _peerGroups.put(peerGroup.getName(), peerGroup);
   }

   public List<String> getActivatedNeighbors() {
      return _activatedNeighbors;
   }

   public void addActivatedNeighbor(String address) {
      _activatedNeighbors.add(address);
   }

   public void addActivatedNeighbors(List<String> addresses) {
      _activatedNeighbors.addAll(addresses);
   }
   
   public void setAsNum(int as){
      _pid = as;
   }
   
   public void setRouterID(String id) {
      _routerID = id;
   }


}
