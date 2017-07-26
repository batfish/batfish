package org.batfish.common;

public class Warning extends Pair<String, String> {

  /** */
  private static final long serialVersionUID = 1L;

  public Warning(String text, String tag) {
    super(text, tag);
  }

  public String getTag() {
    return _second;
  }

  public String getText() {
    return _first;
  }
}
