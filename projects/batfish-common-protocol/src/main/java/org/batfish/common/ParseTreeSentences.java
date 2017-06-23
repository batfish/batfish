package org.batfish.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ParseTreeSentences implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected List<String> _sentences;

   public ParseTreeSentences() {
      _sentences = new ArrayList<>();
   }

   public void appendToLastSentence(String appendStr) {
      if (_sentences.size() == 0) {
         _sentences.add(appendStr);
      }
      else {
         String finalStr = _sentences.get(_sentences.size() - 1) + appendStr;
         _sentences.remove(_sentences.size() - 1);
         _sentences.add(finalStr);
      }
   }

   public List<String> getSentences() {
      return _sentences;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _sentences.isEmpty();
   }

   public void setSentences(List<String> sentences) {
      _sentences = sentences;
   }
}
