package org.batfish.representation.palo_alto;

import java.io.Serializable;

/** Represents a Palo Alto tag */
public class Tag implements Serializable {
  private final String _name;
  private String _comments;

  public Tag(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public String getComments() {
    return _comments;
  }

  public void setComments(String comments) {
    _comments = comments;
  }
}
