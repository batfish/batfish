package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class Comment extends Statement {

  @JsonCreator
  private Comment() {}

  public Comment(String text) {
    setComment(text);
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitComment(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == getClass();
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
