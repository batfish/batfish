package org.batfish.representation.juniper;

import java.io.Serializable;

public class IsisSettings implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _exportPolicy;

   private final IsisLevelSettings _level1Settings;

   private final IsisLevelSettings _level2Settings;

   private boolean _noIpv4Routing;

   private boolean _trafficEngineeringCredibilityProtocolPreference;

   private boolean _trafficEngineeringShortcuts;

   public IsisSettings() {
      _level1Settings = new IsisLevelSettings();
      _level2Settings = new IsisLevelSettings();
   }

   public String getExportPolicy() {
      return _exportPolicy;
   }

   public IsisLevelSettings getLevel1Settings() {
      return _level1Settings;
   }

   public IsisLevelSettings getLevel2Settings() {
      return _level2Settings;
   }

   public boolean getNoIpv4Routing() {
      return _noIpv4Routing;
   }

   public boolean getTrafficEngineeringCredibilityProtocolPreference() {
      return _trafficEngineeringCredibilityProtocolPreference;
   }

   public boolean getTrafficEngineeringShortcuts() {
      return _trafficEngineeringShortcuts;
   }

   public void setExportPolicy(String policy) {
      _exportPolicy = policy;
   }

   public void setNoIpv4Routing(boolean noIpv4Routing) {
      _noIpv4Routing = noIpv4Routing;
   }

   public void setTrafficEngineeringCredibilityProtocolPreference(
         boolean trafficEngineeringCredibilityProtocolPreference) {
      _trafficEngineeringCredibilityProtocolPreference = trafficEngineeringCredibilityProtocolPreference;
   }

   public void setTrafficEngineeringShortcuts(
         boolean trafficEngineeringShortcuts) {
      _trafficEngineeringShortcuts = trafficEngineeringShortcuts;
   }

}
