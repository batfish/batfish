package org.batfish.representation.cisco;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class NatPool extends ComparableStructure<String> {

  private static final String PROP_FIRST = "first";

  private static final String PROP_LAST = "last";

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private Ip _first;

  private Ip _last;

  public NatPool(String name, int definitionLine) {
    super(name);
    _definitionLine = definitionLine;
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  @JsonProperty(PROP_FIRST)
  public Ip getFirst() {
    return _first;
  }

  @JsonProperty(PROP_LAST)
  public Ip getLast() {
    return _last;
  }

  @JsonProperty(PROP_FIRST)
  public void setFirst(Ip first) {
    _first = first;
  }

  @JsonProperty(PROP_LAST)
  public void setLast(Ip last) {
    _last = last;
  }
}
