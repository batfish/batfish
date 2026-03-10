package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface SessionScope {
  <T> T accept(SessionScopeVisitor<T> visitor);
}
