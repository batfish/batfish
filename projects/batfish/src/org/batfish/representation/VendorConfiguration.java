package org.batfish.representation;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.main.Warnings;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class VendorConfiguration
      implements Serializable, GenericConfigObject {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient ConvertConfigurationAnswerElement _answerElement;

   private transient boolean _unrecognized;

   protected transient Warnings _w;

   @JsonIgnore
   public final ConvertConfigurationAnswerElement getAnswerElement() {
      return _answerElement;
   }

   public abstract String getHostname();

   public abstract RoleSet getRoles();

   public abstract Set<String> getUnimplementedFeatures();

   public boolean getUnrecognized() {
      return _unrecognized;
   }

   @JsonIgnore
   public final Warnings getWarnings() {
      return _w;
   }

   public final void setAnswerElement(
         ConvertConfigurationAnswerElement answerElement) {
      _answerElement = answerElement;
   }

   public abstract void setHostname(String hostname);

   public abstract void setRoles(RoleSet roles);

   public void setUnrecognized(boolean unrecognized) {
      _unrecognized = unrecognized;
   }

   public abstract void setVendor(ConfigurationFormat format);

   public final void setWarnings(Warnings warnings) {
      _w = warnings;
   }

   public abstract Configuration toVendorIndependentConfiguration()
         throws VendorConversionException;

   public void undefined(String message, String type, String name) {
      String hostname = getHostname();
      SortedMap<String, SortedSet<String>> byHostname = _answerElement
            .getUndefinedReferences().get(hostname);
      if (byHostname == null) {
         byHostname = new TreeMap<>();
         _answerElement.getUndefinedReferences().put(hostname, byHostname);
      }
      SortedSet<String> byType = byHostname.get(type);
      if (byType == null) {
         byType = new TreeSet<>();
         byHostname.put(type, byType);
      }
      byType.add(name);
   }

   protected void unused(String message, String type, String name) {
      String hostname = getHostname();
      SortedMap<String, SortedSet<String>> byHostname = _answerElement
            .getUnusedStructures().get(hostname);
      if (byHostname == null) {
         byHostname = new TreeMap<>();
         _answerElement.getUnusedStructures().put(hostname, byHostname);
      }
      SortedSet<String> byType = byHostname.get(type);
      if (byType == null) {
         byType = new TreeSet<>();
         byHostname.put(type, byType);
      }
      byType.add(name);
   }

   // protected void duplicate(String message, String type, String name) {
   // _w.redFlag(message, UNUSED);
   // String hostname = getHostname();
   // SortedMap<String, SortedSet<String>> byHostname = _answerElement
   // .getUnusedStructures().get(hostname);
   // if (byHostname == null) {
   // byHostname = new TreeMap<>();
   // _answerElement.getUnusedStructures().put(hostname, byHostname);
   // }
   // SortedSet<String> byType = byHostname.get(type);
   // if (byType == null) {
   // byType = new TreeSet<>();
   // byHostname.put(type, byType);
   // }
   // byType.add(name);
   // }

}
