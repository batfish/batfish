package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class InitInfoAnswerElement extends InitInfo implements AnswerElement {

  private static final String PROP_EXTRA_COMPONENTS = "extraComponents";

  private SortedMap<InitInfoComponent, InitInfo> _extraComponents;

  @JsonCreator
  public InitInfoAnswerElement() {
    _extraComponents = new TreeMap<>();
  }

  @JsonProperty(PROP_EXTRA_COMPONENTS)
  public SortedMap<InitInfoComponent, InitInfo> getExtraComponents() {
    return _extraComponents;
  }

  @Override
  public String prettyPrint() {
    final StringBuilder sb = new StringBuilder();
    final AtomicBoolean empty = new AtomicBoolean(true);
    if (!_parseStatus.isEmpty()) {
      sb.append(super.prettyPrint());
      empty.set(false);
    }
    _extraComponents.forEach(
        (initInfoComponent, initInfo) -> {
          if (!initInfo._parseStatus.isEmpty()) {
            empty.set(false);
            sb.append(String.format("\n%s:\n%s\n", initInfoComponent, initInfo.prettyPrint()));
          }
        });
    if (empty.get()) {
      sb.append("WARNING: All requested init info components are empty!\n");
    }
    return sb.toString();
  }

  @JsonProperty(PROP_EXTRA_COMPONENTS)
  public void setExtraComponents(SortedMap<InitInfoComponent, InitInfo> extraComponents) {
    _extraComponents = extraComponents;
  }
}
