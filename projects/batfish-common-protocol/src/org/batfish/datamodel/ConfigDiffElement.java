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
   protected static final String IDENTICAL = "identical";
   protected static final String UNIQUE = "unique";

   protected Set<String> _common;
   protected Set<String> _identical;
   protected Set<String> _unique;

   @JsonCreator
   public ConfigDiffElement() {

   }

   public ConfigDiffElement(Set<String> a, Set<String> b) {
      _common = CommonUtil.intersection(a, b);
      _unique = CommonUtil.diff(a, b);
      _identical = new HashSet<>();
   }

   @JsonProperty(COMMON)
   public Set<String> getCommon() {
      return _common;
   }

   @JsonProperty(IDENTICAL)
   public Set<String> getIdentical() {
      return _identical;
   }

   @JsonProperty(UNIQUE)
   public Set<String> getUnique() {
      return _unique;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setCommon(Set<String> c) {
      _common = c;
   }

   public void setIdentical(Set<String> i) {
      _identical = i;
   }

   public void setUnique(Set<String> u) {
      this._unique = u;
   }

}
