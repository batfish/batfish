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

   private Logging _logging;

   private Ntp _ntp;

   private Boolean _proxyArp;

   private SortedMap<String, Service> _services;

   private Sntp _sntp;

   private Boolean _sourceRoute;

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

   public Logging getLogging() {
      return _logging;
   }

   public Ntp getNtp() {
      return _ntp;
   }

   public Boolean getProxyArp() {
      return _proxyArp;
   }

   public SortedMap<String, Service> getServices() {
      return _services;
   }

   public Sntp getSntp() {
      return _sntp;
   }

   public Boolean getSourceRoute() {
      return _sourceRoute;
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

   public void setLogging(Logging logging) {
      _logging = logging;
   }

   public void setNtp(Ntp ntp) {
      _ntp = ntp;
   }

   public void setProxyArp(Boolean proxyArp) {
      _proxyArp = proxyArp;
   }

   public void setServices(SortedMap<String, Service> services) {
      _services = services;
   }

   public void setSntp(Sntp sntp) {
      _sntp = sntp;
   }

   public void setSourceRoute(Boolean sourceRoute) {
      _sourceRoute = sourceRoute;
   }

   public void setSsh(SshSettings ssh) {
      _ssh = ssh;
   }

}
