package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonInclude(Include.NON_NULL)
public interface PrefixSetExpr extends Serializable {

   boolean matches(Environment environment,
         AbstractRouteBuilder<?> outputRoute);

}
