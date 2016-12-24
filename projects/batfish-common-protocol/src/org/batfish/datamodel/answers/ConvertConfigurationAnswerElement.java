package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.batfish.common.Warning;
import org.batfish.common.Warnings;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ConvertConfigurationAnswerElement
      implements AnswerElement, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _undefinedReferences;

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _unusedStructures;

   private SortedMap<String, Warnings> _warnings;

   public ConvertConfigurationAnswerElement() {
      _warnings = new TreeMap<>();
      _undefinedReferences = new TreeMap<>();
      _unusedStructures = new TreeMap<>();
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUndefinedReferences() {
      return _undefinedReferences;
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUnusedStructures() {
      return _unusedStructures;
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      StringBuilder retString = new StringBuilder(
            "Results from converting vendor configurations\n");

      for (String name : _warnings.keySet()) {
         retString.append("\n  " + name + "[Conversion warnings]\n");
         for (Warning warning : _warnings.get(name).getRedFlagWarnings()) {
            retString.append("    RedFlag " + warning.getTag() + " : "
                  + warning.getText() + "\n");
         }
         for (Warning warning : _warnings.get(name)
               .getUnimplementedWarnings()) {
            retString.append("    Unimplemented " + warning.getTag() + " : "
                  + warning.getText() + "\n");
         }
         for (Warning warning : _warnings.get(name).getPedanticWarnings()) {
            retString.append("    Pedantic " + warning.getTag() + " : "
                  + warning.getText() + "\n");
         }
      }
      for (String name : _undefinedReferences.keySet()) {
         retString.append("\n  " + name + "[Undefined references]\n");
         for (String structType : _undefinedReferences.get(name).keySet()) {
            for (String structName : _undefinedReferences.get(name)
                  .get(structType)) {
               retString.append("    " + structType + ": " + structName + "\n");
            }
         }
      }
      for (String name : _unusedStructures.keySet()) {
         retString.append("\n  " + name + "[Unused structures]\n");
         for (String structType : _unusedStructures.get(name).keySet()) {
            for (String structName : _unusedStructures.get(name)
                  .get(structType)) {
               retString.append("    " + structType + ": " + structName + "\n");
            }
         }
      }

      return retString.toString();
   }

   public void setUndefinedReferences(
         SortedMap<String, SortedMap<String, SortedSet<String>>> undefinedReferences) {
      _undefinedReferences = undefinedReferences;
   }

   public void setUnusedStructures(
         SortedMap<String, SortedMap<String, SortedSet<String>>> unusedStructures) {
      _unusedStructures = unusedStructures;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }
}
