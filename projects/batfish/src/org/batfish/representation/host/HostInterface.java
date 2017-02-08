package org.batfish.representation.host;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HostInterface implements Serializable {

   private static final String BANDWIDTH_VAR = "bandwidth";

   private static final String GATEWAY_VAR = "gateway";

   private static final String NAME_VAR = "name";

   private static final String OTHER_PREFIXES_VAR = "otherPrefixes";

   private static final String PREFIX_VAR = "prefix";
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Double _bandwidth = 1000 * 1000 * 1000.0; // default is 1 Gbps
   private Ip _gateway;
   private String _name;
   private Set<Prefix> _otherPrefixes;

   private Prefix _prefix;

   @JsonCreator
   public HostInterface(@JsonProperty(NAME_VAR) String name) {
      _name = name;
      _otherPrefixes = new TreeSet<>();
   }

   @JsonProperty(BANDWIDTH_VAR)
   public Double getBandwidth() {
      return _bandwidth;
   }

   @JsonProperty(GATEWAY_VAR)
   public Ip getGateway() {
      return _gateway;
   }

   @JsonProperty(NAME_VAR)
   public String getName() {
      return _name;
   }

   @JsonProperty(OTHER_PREFIXES_VAR)
   public Set<Prefix> getOtherPrefixes() {
      return _otherPrefixes;
   }

   @JsonProperty(PREFIX_VAR)
   public Prefix getPrefix() {
      return _prefix;
   }

   @JsonProperty(BANDWIDTH_VAR)
   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   @JsonProperty(GATEWAY_VAR)
   public void setGateway(Ip gateway) {
      _gateway = gateway;
   }

   @JsonProperty(OTHER_PREFIXES_VAR)
   public void setOtherPrefixes(Set<Prefix> otherPrefixes) {
      _otherPrefixes = otherPrefixes;
   }

   @JsonProperty(PREFIX_VAR)
   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public Interface toInterface(Configuration configuration,
         Warnings warnings) {
      Interface iFace = new Interface(_name, configuration);
      iFace.setBandwidth(_bandwidth);
      iFace.setPrefix(_prefix);
      iFace.getAllPrefixes().add(_prefix);
      iFace.getAllPrefixes().addAll(_otherPrefixes);
      return iFace;
   }
}
