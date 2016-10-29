package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class CiscoFamily implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private Aaa _aaa;

   private SortedMap<String, String> _banners;

   private SortedMap<String, Boolean> _features;

   private String _hostname;

   private SortedMap<String, Line> _lines;

   private SortedMap<String, Boolean> _services;

   private SnmpServer _snmpServer;

   private SshSettings _ssh;

   public CiscoFamily() {
      _banners = new TreeMap<>();
      _features = new TreeMap<>();
      _lines = new TreeMap<>();
      _services = new TreeMap<>();
   }

   public Aaa getAaa() {
      return _aaa;
   }

   public SortedMap<String, String> getBanners() {
      return _banners;
   }

   public SortedMap<String, Boolean> getFeatures() {
      return _features;
   }

   public String getHostname() {
      return _hostname;
   }

   public SortedMap<String, Line> getLines() {
      return _lines;
   }
   
   public SortedMap<String, Boolean> getServices() {
      return _services;
   }

   public SnmpServer getSnmpServer() {
      return _snmpServer;
   }

   public SshSettings getSsh() {
      return _ssh;
   }

   public void setAaa(Aaa aaa) {
      _aaa = aaa;
   }

   public void setBanners(SortedMap<String, String> banners) {
      _banners = banners;
   }

   public void setFeatures(SortedMap<String, Boolean> features) {
      _features = features;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setLines(SortedMap<String, Line> lines) {
      _lines = lines;
   }

   public void setServices(SortedMap<String, Boolean> services) {
      _services = services;
   }

   public void setSnmpServer(SnmpServer snmpServer) {
      _snmpServer = snmpServer;
   }

   public void setSsh(SshSettings ssh) {
      _ssh = ssh;
   }

}
