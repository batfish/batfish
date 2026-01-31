package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Cisco FTD policy-map stanza. */
public class FtdPolicyMap implements Serializable {
  private final @Nonnull String _name;
  private @Nullable String _type;
  private final @Nonnull List<String> _classNames;
  private final @Nonnull List<String> _parameterLines;
  private final @Nonnull Map<String, List<String>> _classActionLines;

  public FtdPolicyMap(@Nonnull String name) {
    _name = name;
    _classNames = new ArrayList<>();
    _parameterLines = new ArrayList<>();
    _classActionLines = new HashMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getType() {
    return _type;
  }

  public void setType(@Nullable String type) {
    _type = type;
  }

  public @Nonnull List<String> getClassNames() {
    return _classNames;
  }

  public void addClassName(@Nonnull String className) {
    _classNames.add(className);
  }

  public @Nonnull List<String> getParameterLines() {
    return _parameterLines;
  }

  public void addParameterLine(@Nonnull String line) {
    _parameterLines.add(line);
  }

  public @Nonnull Map<String, List<String>> getClassActionLines() {
    return _classActionLines;
  }

  public void addClassActionLine(@Nonnull String className, @Nonnull String line) {
    _classActionLines.computeIfAbsent(className, name -> new ArrayList<>()).add(line);
  }
}
