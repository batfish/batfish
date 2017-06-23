package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Problem implements Serializable {

   private static final String DESCRIPTION_VAR = "description";

   private static final String FILES_VAR = "files";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _description;

   private SortedMap<String, SortedSet<Integer>> _files;

   public Problem() {
      _files = new TreeMap<>();
   }

   @JsonProperty(DESCRIPTION_VAR)
   public String getDescription() {
      return _description;
   }

   @JsonProperty(FILES_VAR)
   public SortedMap<String, SortedSet<Integer>> getFiles() {
      return _files;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public void setDescription(String description) {
      _description = description;
   }

   @JsonProperty(FILES_VAR)
   public void setFiles(SortedMap<String, SortedSet<Integer>> files) {
      _files = files;
   }

}
