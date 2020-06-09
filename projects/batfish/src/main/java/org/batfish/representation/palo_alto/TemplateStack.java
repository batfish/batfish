package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a Palo Alto configuration template stack, which associates multiple templates with
 * devices
 */
@ParametersAreNonnullByDefault
public class TemplateStack implements Serializable {
  private final String _name;
  private @Nullable String _description;
  private final List<String> _devices;
  private final List<String> _templates;

  public TemplateStack(String name) {
    _name = name;
    _devices = new ArrayList<>();
    _templates = new ArrayList<>();
  }

  public void addDevice(String deviceName) {
    _devices.add(deviceName);
  }

  public void addTemplate(String templateName) {
    _templates.add(templateName);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public List<String> getDevices() {
    return ImmutableList.copyOf(_devices);
  }

  public String getName() {
    return _name;
  }

  public List<String> getTemplates() {
    return ImmutableList.copyOf(_templates);
  }

  public void setDescription(String description) {
    _description = description;
  }
}
