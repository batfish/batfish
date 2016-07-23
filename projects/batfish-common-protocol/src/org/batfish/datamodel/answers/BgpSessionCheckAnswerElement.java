package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpNeighbor.BgpNeighborSummary;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BgpSessionCheckAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _allBgpNeighborSummarys;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _broken;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpBroken;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpHalfOpen;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpLocalIpOnLoopback;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpLocalIpUnknown;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpMissingLocalIp;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpNonUniqueEndpoint;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpRemoteIpOnLoopback;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ebgpRemoteIpUnknown;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _halfOpen;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpBroken;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpHalfOpen;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpLocalIpOnNonLoopback;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpLocalIpUnknown;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpMissingLocalIp;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpNonUniqueEndpoint;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpRemoteIpOnNonLoopback;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ibgpRemoteIpUnknown;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _ignoredForeignEndpoints;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _localIpUnknown;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _missingLocalIp;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _nonUniqueEndpoint;

   private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _remoteIpUnknown;

   public BgpSessionCheckAnswerElement() {
      _allBgpNeighborSummarys = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _broken = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpBroken = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpBroken = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpHalfOpen = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpLocalIpOnLoopback = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpLocalIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpMissingLocalIp = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpNonUniqueEndpoint = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpRemoteIpOnLoopback = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ebgpRemoteIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _halfOpen = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpHalfOpen = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpLocalIpOnNonLoopback = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpLocalIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpMissingLocalIp = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpNonUniqueEndpoint = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpRemoteIpOnNonLoopback = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ibgpRemoteIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _ignoredForeignEndpoints = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _localIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _missingLocalIp = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _nonUniqueEndpoint = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
      _remoteIpUnknown = new TreeMap<String, SortedMap<Prefix, BgpNeighborSummary>>();
   }

   public void add(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> neighborsByHostname,
         Configuration c, BgpNeighborSummary bgpNeighbor) {
      String hostname = c.getHostname();
      SortedMap<Prefix, BgpNeighborSummary> neighborsByPrefix = neighborsByHostname
            .get(hostname);
      if (neighborsByPrefix == null) {
         neighborsByPrefix = new TreeMap<Prefix, BgpNeighborSummary>();
         neighborsByHostname.put(hostname, neighborsByPrefix);
      }
      Prefix prefix = bgpNeighbor.getPrefix();
      neighborsByPrefix.put(prefix, bgpNeighbor);
   }

   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getAllBgpNeighbors() {
      return _allBgpNeighborSummarys;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getBroken() {
      return _broken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpBroken() {
      return _ebgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpHalfOpen() {
      return _ebgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpLocalIpOnLoopback() {
      return _ebgpLocalIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpLocalIpUnknown() {
      return _ebgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpMissingLocalIp() {
      return _ebgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpNonUniqueEndpoint() {
      return _ebgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpRemoteIpOnLoopback() {
      return _ebgpRemoteIpOnLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getEbgpRemoteIpUnknown() {
      return _ebgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getHalfOpen() {
      return _halfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpBroken() {
      return _ibgpBroken;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpHalfOpen() {
      return _ibgpHalfOpen;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpLocalIpOnNonLoopback() {
      return _ibgpLocalIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpLocalIpUnknown() {
      return _ibgpLocalIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpMissingLocalIp() {
      return _ibgpMissingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpNonUniqueEndpoint() {
      return _ibgpNonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpRemoteIpOnNonLoopback() {
      return _ibgpRemoteIpOnNonLoopback;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIbgpRemoteIpUnknown() {
      return _ibgpRemoteIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getIgnoredForeignEndpoints() {
      return _ignoredForeignEndpoints;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getLocalIpUnknown() {
      return _localIpUnknown;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getMissingLocalIp() {
      return _missingLocalIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getNonUniqueEndpoint() {
      return _nonUniqueEndpoint;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getRemoteIpUnknown() {
      return _remoteIpUnknown;
   }

   public void setAllBgpNeighborSummarys(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> allBgpNeighborSummarys) {
      _allBgpNeighborSummarys = allBgpNeighborSummarys;
   }

   public void setBroken(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> broken) {
      _broken = broken;
   }

   public void setEbgpBroken(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpBroken) {
      _ebgpBroken = ebgpBroken;
   }

   public void setEbgpHalfOpen(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpHalfOpen) {
      _ebgpHalfOpen = ebgpHalfOpen;
   }

   public void setEbgpLocalIpOnLoopback(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpLocalIpOnLoopback) {
      _ebgpLocalIpOnLoopback = ebgpLocalIpOnLoopback;
   }

   public void setEbgpLocalIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpLocalIpUnknown) {
      _ebgpLocalIpUnknown = ebgpLocalIpUnknown;
   }

   public void setEbgpMissingLocalIp(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpMissingLocalIp) {
      _ebgpMissingLocalIp = ebgpMissingLocalIp;
   }

   public void setEbgpNonUniqueEndpoint(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpNonUniqueEndpoint) {
      _ebgpNonUniqueEndpoint = ebgpNonUniqueEndpoint;
   }

   public void setEbgpRemoteIpOnLoopback(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpRemoteIpOnLoopback) {
      _ebgpRemoteIpOnLoopback = ebgpRemoteIpOnLoopback;
   }

   public void setEbgpRemoteIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ebgpRemoteIpUnknown) {
      _ebgpRemoteIpUnknown = ebgpRemoteIpUnknown;
   }

   public void setHalfOpen(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> halfOpen) {
      _halfOpen = halfOpen;
   }

   public void setIbgpBroken(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpBroken) {
      _ibgpBroken = ibgpBroken;
   }

   public void setIbgpHalfOpen(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpHalfOpen) {
      _ibgpHalfOpen = ibgpHalfOpen;
   }

   public void setIbgpLocalIpOnNonLoopback(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpLocalIpOnNonLoopback) {
      _ibgpLocalIpOnNonLoopback = ibgpLocalIpOnNonLoopback;
   }

   public void setIbgpLocalIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpLocalIpUnknown) {
      _ibgpLocalIpUnknown = ibgpLocalIpUnknown;
   }

   public void setIbgpMissingLocalIp(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpMissingLocalIp) {
      _ibgpMissingLocalIp = ibgpMissingLocalIp;
   }

   public void setIbgpNonUniqueEndpoint(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpNonUniqueEndpoint) {
      _ibgpNonUniqueEndpoint = ibgpNonUniqueEndpoint;
   }

   public void setIbgpRemoteIpOnNonLoopback(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpRemoteIpOnNonLoopback) {
      _ibgpRemoteIpOnNonLoopback = ibgpRemoteIpOnNonLoopback;
   }

   public void setIbgpRemoteIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ibgpRemoteIpUnknown) {
      _ibgpRemoteIpUnknown = ibgpRemoteIpUnknown;
   }

   public void setIgnoredForeignEndpoints(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> ignoredForeignEndpoints) {
      _ignoredForeignEndpoints = ignoredForeignEndpoints;
   }

   public void setLocalIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> localIpUnknown) {
      _localIpUnknown = localIpUnknown;
   }

   public void setMissingLocalIp(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> missingLocalIp) {
      _missingLocalIp = missingLocalIp;
   }

   public void setNonUniqueEndpoint(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> nonUniqueEndpoint) {
      _nonUniqueEndpoint = nonUniqueEndpoint;
   }

   public void setRemoteIpUnknown(
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> remoteIpUnknown) {
      _remoteIpUnknown = remoteIpUnknown;
   }
   
   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
