package org.batfish.datamodel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class PolicyMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   @JsonIgnore
   public abstract PolicyMapSetType getType();

}
