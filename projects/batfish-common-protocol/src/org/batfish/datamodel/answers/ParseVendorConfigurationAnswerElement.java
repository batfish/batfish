package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ParseVendorConfigurationAnswerElement
      implements AnswerElement, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, ParseStatus> _parseStatus;

   private SortedMap<String, ParseTreeSentences> _parseTrees;

   private SortedMap<String, Warnings> _warnings;

   public ParseVendorConfigurationAnswerElement() {
      _parseStatus = new TreeMap<>();
      _parseTrees = new TreeMap<>();
      _warnings = new TreeMap<>();
   }

   public void addRedFlagWarning(String name, Warning warning) {
      if (!_warnings.containsKey(name)) {
         _warnings.put(name, new Warnings());
      }
      _warnings.get(name).getRedFlagWarnings().add(warning);
   }

   public SortedMap<String, ParseStatus> getParseStatus() {
      return _parseStatus;
   }

   public SortedMap<String, ParseTreeSentences> getParseTrees() {
      return _parseTrees;
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      StringBuilder retString = new StringBuilder(
            "Results of parsing vendor configurations\n");

      for (String name : _warnings.keySet()) {
         retString.append("\n  " + name + "[Parser warnings]\n");
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
      for (String name : _parseTrees.keySet()) {
         retString.append("\n  " + name + " [Parse trees]\n");
         for (String sentence : _parseTrees.get(name).getSentences()) {
            retString.append("    ParseTreeSentence : " + sentence + "\n");
         }
      }

      return retString.toString();
   }

   public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
      _parseStatus = parseStatus;
   }

   public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
      _parseTrees = parseTrees;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }
}
