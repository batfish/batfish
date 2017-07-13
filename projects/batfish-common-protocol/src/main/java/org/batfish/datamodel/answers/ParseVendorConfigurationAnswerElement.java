package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;

public class ParseVendorConfigurationAnswerElement
      implements AnswerElement, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, String> _fileMap;

   private SortedMap<String, ParseStatus> _parseStatus;

   private SortedMap<String, ParseTreeSentences> _parseTrees;

   private String _version;

   private Date _startTimestamp = null;

   private SortedMap<String, Warnings> _warnings;

   private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

   public ParseVendorConfigurationAnswerElement() {
      _fileMap = new TreeMap<>();
      _parseStatus = new TreeMap<>();
      _parseTrees = new TreeMap<>();
      _warnings = new TreeMap<>();
      _errors = new TreeMap<>();
   }

   public void addRedFlagWarning(String name, Warning warning) {
      if (!_warnings.containsKey(name)) {
         _warnings.put(name, new Warnings());
      }
      _warnings.get(name).getRedFlagWarnings().add(warning);
   }

   public SortedMap<String, String> getFileMap() {
      return _fileMap;
   }

   public SortedMap<String, ParseStatus> getParseStatus() {
      return _parseStatus;
   }

   public SortedMap<String, ParseTreeSentences> getParseTrees() {
      return _parseTrees;
   }

   public String getVersion() {
      return _version;
   }

   public Date getStartTimestamp() {
      return _startTimestamp;
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
   }

   public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
      return _errors;
   }

   @Override
   public String prettyPrint() {
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
      for (String name : _errors.keySet()) {
         retString.append("\n  " + name + "[Parser errors]\n");
         for (String line : _errors.get(name).getLineMap()) {
            retString.append("    " + line + "\n");
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

   public void setFileMap(SortedMap<String, String> fileMap) {
      _fileMap = fileMap;
   }

   public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
      _parseStatus = parseStatus;
   }

   public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
      _parseTrees = parseTrees;
   }

   public void setVersion(String version) {
      _version = version;
   }

   public void setStartTimestamp(Date startTimestamp) {
      _startTimestamp = startTimestamp;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }

   public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
      _errors = errors;
   }

}
