package org.batfish.datamodel;

import java.util.HashSet;
import java.util.Set;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigDiffElement implements AnswerElement {

   protected static final String COMMON = "common";
   protected static final String UNIQUE = "unique";
   protected static final String IDENTICAL = "identical";

   protected Set<String> _common;
   protected Set<String> _unique;
   protected Set<String> _identical;

   @JsonCreator
   public ConfigDiffElement() {

   }

   public ConfigDiffElement(Set<String> a, Set<String> b) {
      _common = CommonUtil.intersection(a, b);
      _unique = CommonUtil.diff(a, b);
      _identical = new HashSet<String>();
   }

   /**
    * @return the _identicalAsPathAccessLists
    */
   @JsonProperty(IDENTICAL)
   public Set<String> get_identical() {
      return _identical;
   }

   public void set_identical(Set<String> i) {
      _identical = i;
   }

   /**
    * @return the _unique
    */
   @JsonProperty(UNIQUE)
   public Set<String> get_unique() {
      return _unique;
   }

   public void set_unique(Set<String> u) {
      this._unique = u;
   }

   /**
    * @return the _common
    */
   @JsonProperty(COMMON)
   public Set<String> get_common() {
      return _common;
   }

   public void set_common(Set<String> c) {
      _common = c;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO Auto-generated method stub
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

}
