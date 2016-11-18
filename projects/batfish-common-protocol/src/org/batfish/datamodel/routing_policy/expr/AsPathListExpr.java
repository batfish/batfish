package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;
import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface AsPathListExpr extends Serializable {

   List<Integer> evaluate(Environment environment);

}
