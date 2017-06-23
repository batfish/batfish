package org.batfish.datamodel;

import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigDiffElement implements AnswerElement {

   private static final String DIFF_INFO_VAR = "diffInfo";

   private static final String DIFF_VAR = "diff";

   // private static final String IDENTICAL_VAR = "identical";

   private static final String IN_AFTER_ONLY_VAR = "inAfterOnly";

   private static final String IN_BEFORE_ONLY_VAR = "inBeforeOnly";

   private static final int MAX_IDENTICAL = 5;

   private Set<String> _after;

   private Set<String> _before;

   protected SortedSet<String> _diff;

   protected SortedMap<String, AnswerElement> _diffInfo;

   protected SortedSet<String> _identical;

   protected SortedSet<String> _inAfterOnly;

   protected SortedSet<String> _inBeforeOnly;

   @JsonCreator
   public ConfigDiffElement() {
   }

   /**
    * basic diff with only names of different items and optional summary
    *
    * @param before
    * @param after
    */
   protected <T> ConfigDiffElement(NavigableMap<String, T> before,
         NavigableMap<String, T> after, boolean summarizeIdentical) {
      this(before.keySet(), after.keySet());
      for (String name : common()) {
         if (!skip(name)) {
            T beforeCurrent = before.get(name);
            T afterCurrent = after.get(name);
            if (beforeCurrent.equals(afterCurrent)) {
               _identical.add(name);
            }
            else {
               _diff.add(name);
            }
         }
      }
      if (summarizeIdentical) {
         summarizeIdentical();
      }
   }

   public ConfigDiffElement(Set<String> before, Set<String> after) {
      _before = before;
      _after = after;
      _diff = new TreeSet<>();
      _diffInfo = new TreeMap<>();
      _identical = new TreeSet<>();
      _inBeforeOnly = CommonUtil.difference(before, after, TreeSet::new);
      _inAfterOnly = CommonUtil.difference(after, before, TreeSet::new);
   }

   public Set<String> common() {
      return CommonUtil.intersection(_before, _after, TreeSet::new);
   }

   @JsonProperty(DIFF_VAR)
   public SortedSet<String> getDiff() {
      return _diff;
   }

   @JsonProperty(DIFF_INFO_VAR)
   public SortedMap<String, AnswerElement> getDiffInfo() {
      return _diffInfo;
   }

   // @JsonProperty(IDENTICAL_VAR)
   @JsonIgnore
   public SortedSet<String> getIdentical() {
      return _identical;
   }

   @JsonProperty(IN_AFTER_ONLY_VAR)
   public SortedSet<String> getInAfterOnly() {
      return _inAfterOnly;
   }

   @JsonProperty(IN_BEFORE_ONLY_VAR)
   public SortedSet<String> getInBeforeOnly() {
      return _inBeforeOnly;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _diff.isEmpty() && _diffInfo.isEmpty() && _inBeforeOnly.isEmpty()
            && _inAfterOnly.isEmpty();
   }

   @JsonProperty(DIFF_VAR)
   public void setDiff(SortedSet<String> diff) {
      _diff = diff;
   }

   @JsonProperty(DIFF_INFO_VAR)
   public void setDiffInfo(SortedMap<String, AnswerElement> diffInfo) {
      _diffInfo = diffInfo;
   }

   // @JsonProperty(IDENTICAL_VAR)
   @JsonIgnore
   public void setIdentical(SortedSet<String> identical) {
      _identical = identical;
   }

   @JsonProperty(IN_AFTER_ONLY_VAR)
   public void setInAfterOnly(SortedSet<String> inAfterOnly) {
      _inAfterOnly = inAfterOnly;
   }

   @JsonProperty(IN_BEFORE_ONLY_VAR)
   public void setInBeforeOnly(SortedSet<String> inBeforeOnly) {
      _inBeforeOnly = inBeforeOnly;
   }

   protected boolean skip(String name) {
      return false;
   }

   protected final void summarizeIdentical() {
      int numIdentical = _identical.size();
      if (numIdentical > MAX_IDENTICAL) {
         _identical = new TreeSet<>();
         _identical.add(
               numIdentical + " identical elements not shown for readability.");
      }
   }

}
