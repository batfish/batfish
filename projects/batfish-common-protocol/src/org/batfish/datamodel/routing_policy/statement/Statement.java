package org.batfish.datamodel.routing_policy.statement;

import java.io.Serializable;
import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface Statement extends Serializable {

   Result execute(Environment environment);

   List<Statement> simplify();

}
