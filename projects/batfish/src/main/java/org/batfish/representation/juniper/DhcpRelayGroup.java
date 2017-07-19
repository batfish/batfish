package org.batfish.representation.juniper;

import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

public class DhcpRelayGroup extends ComparableStructure<String> {

   public static final String MASTER_DHCP_RELAY_GROUP_NAME = "~MASTER_DHCP_RELAY_GROUP~";

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String _activeServerGroup;

   private int _activeServerGroupLine;

   public int getActiveServerGroupLine() {
      return _activeServerGroupLine;
   }

   public void setActiveServerGroupLine(int activeServerGroupLine) {
      _activeServerGroupLine = activeServerGroupLine;
   }

   private SortedSet<String> _interfaces;

   private boolean _allInterfaces;

   public DhcpRelayGroup(String name) {
      super(name);
      _interfaces = new TreeSet<>();
   }

   public String getActiveServerGroup() {
      return _activeServerGroup;
   }

   public void setActiveServerGroup(String activeServerGroup) {
      _activeServerGroup = activeServerGroup;
   }

   public SortedSet<String> getInterfaces() {
      return _interfaces;
   }

   public void setInterfaces(SortedSet<String> interfaces) {
      _interfaces = interfaces;
   }

   public void setAllInterfaces(boolean allInterfaces) {
      _allInterfaces = allInterfaces;
   }

   public boolean getAllInterfaces() {
      return _allInterfaces;
   }

}
