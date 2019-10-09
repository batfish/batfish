package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents a Palo Alto tag */
public class Tag implements Serializable {
  private final String _name;
  private @Nullable String _comments;

  public Tag(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  public void setComments(String comments) {
    _comments = comments;
  }
}
