package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class BgpSessionCheckAnswerElement implements AnswerElement {

   private final Map<String, Map<Prefix, BgpNeighbor>> _allBgpNeighbors;

   private final Map<String, Map<Prefix, BgpNeighbor>> _broken;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpBroken;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpHalfOpen;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpLocalIpOnLoopback;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpLocalIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpMissingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpNonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpRemoteIpOnLoopback;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpRemoteIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighbor>> _halfOpen;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpBroken;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpHalfOpen;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpLocalIpOnNonLoopback;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpLocalIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpMissingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpNonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpRemoteIpOnNonLoopback;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpRemoteIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighbor>> _ignoredForeignEndpoints;

   private final Map<String, Map<Prefix, BgpNeighbor>> _localIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighbor>> _missingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighbor>> _nonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighbor>> _remoteIpUnknown;

   public BgpSessionCheckAnswerElement() {
      _allBgpNeighbors = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _broken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpBroken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpBroken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpLocalIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpRemoteIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ebgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _halfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpLocalIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpRemoteIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ibgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _ignoredForeignEndpoints = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _localIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _missingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _nonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      _remoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
   }

   public void add(Map<String, Map<Prefix, BgpNeighbor>> neighborsByHostname,
         Configuration c, BgpNeighbor bgpNeighbor) {
      String hostname = c.getHostname();
      Map<Prefix, BgpNeighbor> neighborsByPrefix = neighborsByHostname
            .get(hostname);
      if (neighborsByPrefix == null) {
         neighborsByPrefix = new TreeMap<Prefix, BgpNeighbor>();
         neighborsByHostname.put(hostname, neighborsByPrefix);
      }
      Prefix prefix = bgpNeighbor.getPrefix();
      neighborsByPrefix.put(prefix, bgpNeighbor);
   }

   public Map<String, Map<Prefix, BgpNeighbor>> getAllBgpNeighbors() {
      return _allBgpNeighbors;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getBroken() {
      return _broken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpBroken() {
      return _ebgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpHalfOpen() {
      return _ebgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpLocalIpOnLoopback() {
      return _ebgpLocalIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpLocalIpUnknown() {
      return _ebgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpMissingLocalIp() {
      return _ebgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpNonUniqueEndpoint() {
      return _ebgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpRemoteIpOnLoopback() {
      return _ebgpRemoteIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getEbgpRemoteIpUnknown() {
      return _ebgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getHalfOpen() {
      return _halfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpBroken() {
      return _ibgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpHalfOpen() {
      return _ibgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpLocalIpOnNonLoopback() {
      return _ibgpLocalIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpLocalIpUnknown() {
      return _ibgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpMissingLocalIp() {
      return _ibgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpNonUniqueEndpoint() {
      return _ibgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpRemoteIpOnNonLoopback() {
      return _ibgpRemoteIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIbgpRemoteIpUnknown() {
      return _ibgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getIgnoredForeignEndpoints() {
      return _ignoredForeignEndpoints;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getLocalIpUnknown() {
      return _localIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getMissingLocalIp() {
      return _missingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getNonUniqueEndpoint() {
      return _nonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighbor>> getRemoteIpUnknown() {
      return _remoteIpUnknown;
   }
}
