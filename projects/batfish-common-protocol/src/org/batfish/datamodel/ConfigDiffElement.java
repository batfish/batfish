package org.batfish.datamodel;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigDiffElement implements AnswerElement {

   protected static final String IDENTICAL_VAR = "identical";
   protected static final String IN_A_ONLY_VAR = "inAOnly";
   protected static final String IN_B_ONLY_VAR = "inBOnly";

   private Set<String> _a;
   private Set<String> _b;

   protected Set<String> _identical;

   protected Set<String> _inAOnly;
   protected Set<String> _inBOnly;

   @JsonCreator
   public ConfigDiffElement() {
   }

   public ConfigDiffElement(Set<String> a, Set<String> b) {
      _a = a;
      _b = b;
      _identical = new TreeSet<>();
      _inAOnly = CommonUtil.inAOnly(a, b);
      _inBOnly = CommonUtil.inBOnly(a, b);
   }

   public Set<String> common() {
      return CommonUtil.intersection(_a, _b);
   }

   @JsonProperty(IDENTICAL_VAR)
   public Set<String> getIdentical() {
      return _identical;
   }

   @JsonProperty(IN_A_ONLY_VAR)
   public Set<String> getInAOnly() {
      return _inAOnly;
   }

   @JsonProperty(IN_B_ONLY_VAR)
   public Set<String> getInBOnly() {
      return _inBOnly;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setIdentical(Set<String> identical) {
      _identical = identical;
   }

   public void setInAOnly(Set<String> inAOnly) {
      _inAOnly = inAOnly;
   }

   public void setInBOnly(Set<String> inBOnly) {
      _inBOnly = inBOnly;
   }

}
