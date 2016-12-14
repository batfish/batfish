package org.batfish.datamodel.routing_policy.statement;

import java.io.Serializable;
import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonSubTypes({ @JsonSubTypes.Type(value = AddCommunity.class),
      @JsonSubTypes.Type(value = BufferedStatement.class),
      @JsonSubTypes.Type(value = CallStatement.class),
      @JsonSubTypes.Type(value = Comment.class),
      @JsonSubTypes.Type(value = DeleteCommunity.class),
      @JsonSubTypes.Type(value = If.class),
      @JsonSubTypes.Type(value = PrependAsPath.class),
      @JsonSubTypes.Type(value = RetainCommunity.class),
      @JsonSubTypes.Type(value = SetCommunity.class),
      @JsonSubTypes.Type(value = SetIsisLevel.class),
      @JsonSubTypes.Type(value = SetIsisMetricType.class),
      @JsonSubTypes.Type(value = SetLocalPreference.class),
      @JsonSubTypes.Type(value = SetMetric.class),
      @JsonSubTypes.Type(value = SetNextHop.class),
      @JsonSubTypes.Type(value = SetOrigin.class),
      @JsonSubTypes.Type(value = SetOspfMetricType.class),
      @JsonSubTypes.Type(value = SetTag.class),
      @JsonSubTypes.Type(value = SetVarMetricType.class),
      @JsonSubTypes.Type(value = SetWeight.class) })
public interface Statement extends Serializable {

   Result execute(Environment environment);

   List<Statement> simplify();

}
