package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Represents Juniper screens */
public final class Screen implements Serializable {

  private final String _name;
  private final List<ScreenOption> _screenOptions;
  private ScreenAction _action;

  public Screen(String name) {
    _name = name;
    _action = ScreenAction.DROP;
    _screenOptions = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public List<ScreenOption> getScreenOptions() {
    return _screenOptions;
  }

  public ScreenAction getAction() {
    return _action;
  }

  public void setAction(ScreenAction action) {
    _action = action;
  }
}
