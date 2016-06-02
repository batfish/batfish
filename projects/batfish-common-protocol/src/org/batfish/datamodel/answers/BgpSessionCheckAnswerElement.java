package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.datamodel.BgpNeighbor.BgpNeighborSummary;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class BgpSessionCheckAnswerElement implements AnswerElement {

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _allBgpNeighborSummarys;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _broken;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpBroken;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpHalfOpen;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpLocalIpOnLoopback;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpLocalIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpMissingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpNonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpRemoteIpOnLoopback;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ebgpRemoteIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _halfOpen;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpBroken;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpHalfOpen;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpLocalIpOnNonLoopback;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpLocalIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpMissingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpNonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpRemoteIpOnNonLoopback;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ibgpRemoteIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _ignoredForeignEndpoints;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _localIpUnknown;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _missingLocalIp;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _nonUniqueEndpoint;

   private final Map<String, Map<Prefix, BgpNeighborSummary>> _remoteIpUnknown;

   public BgpSessionCheckAnswerElement() {
      _allBgpNeighborSummarys = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _broken = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpBroken = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpBroken = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpLocalIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpRemoteIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ebgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _halfOpen = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpLocalIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpRemoteIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ibgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _ignoredForeignEndpoints = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _localIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _missingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _nonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
      _remoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighborSummary>>();
   }

   public void add(
         Map<String, Map<Prefix, BgpNeighborSummary>> neighborsByHostname,
         Configuration c, BgpNeighborSummary bgpNeighbor) {
      String hostname = c.getHostname();
      Map<Prefix, BgpNeighborSummary> neighborsByPrefix = neighborsByHostname
            .get(hostname);
      if (neighborsByPrefix == null) {
         neighborsByPrefix = new TreeMap<Prefix, BgpNeighborSummary>();
         neighborsByHostname.put(hostname, neighborsByPrefix);
      }
      Prefix prefix = bgpNeighbor.getPrefix();
      neighborsByPrefix.put(prefix, bgpNeighbor);
   }

   public Map<String, Map<Prefix, BgpNeighborSummary>> getAllBgpNeighbors() {
      return _allBgpNeighborSummarys;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getBroken() {
      return _broken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpBroken() {
      return _ebgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpHalfOpen() {
      return _ebgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpLocalIpOnLoopback() {
      return _ebgpLocalIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpLocalIpUnknown() {
      return _ebgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpMissingLocalIp() {
      return _ebgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpNonUniqueEndpoint() {
      return _ebgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpRemoteIpOnLoopback() {
      return _ebgpRemoteIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getEbgpRemoteIpUnknown() {
      return _ebgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getHalfOpen() {
      return _halfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpBroken() {
      return _ibgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpHalfOpen() {
      return _ibgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpLocalIpOnNonLoopback() {
      return _ibgpLocalIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpLocalIpUnknown() {
      return _ibgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpMissingLocalIp() {
      return _ibgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpNonUniqueEndpoint() {
      return _ibgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpRemoteIpOnNonLoopback() {
      return _ibgpRemoteIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIbgpRemoteIpUnknown() {
      return _ibgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getIgnoredForeignEndpoints() {
      return _ignoredForeignEndpoints;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getLocalIpUnknown() {
      return _localIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getMissingLocalIp() {
      return _missingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getNonUniqueEndpoint() {
      return _nonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Map<Prefix, BgpNeighborSummary>> getRemoteIpUnknown() {
      return _remoteIpUnknown;
   }
}
