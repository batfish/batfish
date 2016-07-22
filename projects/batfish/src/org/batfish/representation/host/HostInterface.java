package org.batfish.representation.host;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HostInterface implements Serializable {

   private static final String NAME_VAR = "name";

   private static final String OTHER_PREFIXES_VAR = "otherPrefixes";
   private static final String PREFIX_VAR = "prefix";
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;
   private Set<Prefix> _otherPrefixes;
   private Prefix _prefix;

   @JsonCreator
   public HostInterface(@JsonProperty(NAME_VAR) String name) {
      _name = name;
      _otherPrefixes = new TreeSet<Prefix>();
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

   public void setOtherPrefixes(Set<Prefix> otherPrefixes) {
      _otherPrefixes = otherPrefixes;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public Interface toInterface(Configuration configuration) {
      Interface iFace = new Interface(_name, configuration);
      iFace.setPrefix(_prefix);
      iFace.getAllPrefixes().add(_prefix);
      iFace.getAllPrefixes().addAll(_otherPrefixes);
      return iFace;
   }
}
