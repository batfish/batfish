package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * A type of {@link Statement policy statement} that signifies an action should be taken on a
 * packet/flow
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface Action extends Serializable {

  <T> T accept(ActionVisitor<T> visitor);
}
