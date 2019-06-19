package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class Comment extends Statement {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private Comment() {}

  public Comment(String text) {
    setComment(text);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == this.getClass();
  }

  @Override
  public Result execute(Environment environment) {
    return new Result();
  }

  @Override
  public int hashCode() {
    return 0x12345678;
  }
}
