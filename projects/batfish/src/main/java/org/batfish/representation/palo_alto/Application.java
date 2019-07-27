package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

/** Represents a Palo Alto application object */
@ParametersAreNonnullByDefault
public final class Application implements Serializable, Comparable<Application> {

  public static final class Builder {
    private String _name;
    private String _description;
    private Map<IpProtocol, Integer[]> _ports;

    private Builder() {
      _ports = ImmutableMap.of();
    }

    public @Nonnull Application build() {
      checkArgument(_name != null, "Application is missing name");
      Application app = new Application(_name, _ports);
      if (_description != null) {
        app.setDescription(_description);
      }
      return app;
    }

    public Builder setDescription(String description) {
      _description = description;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setPorts(Map<IpProtocol, Integer[]> ports) {
      _ports = ports;
      return this;
    }
  }

  @Nullable
  private String _description;

  @Nonnull
  private final String _name;

  @Nonnull
  private final SortedSet<HeaderSpace> _applicationHeaderSpaces;

  private Application(String name, Map<IpProtocol, Integer[]> ports) {
    _name = name;
    SortedSet<HeaderSpace> spaces = new TreeSet<HeaderSpace>();
    ports.forEach((k, v) -> spaces.add(HeaderSpace.builder().setIpProtocols(Arrays.asList(k))
        .setDstPorts(Arrays.stream(v).map(p -> new SubRange(p, p))::iterator).build()));
    _applicationHeaderSpaces = ImmutableSortedSet.copyOf(spaces);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public SortedSet<HeaderSpace> getHeaderSpaces() {
    return _applicationHeaderSpaces;
  }

  public int compareTo(Application o) {
    return _name.compareTo(o.getName());
  }
}
