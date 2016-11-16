package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface OriginExpr extends Serializable {

}
