package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.common.util.ComparableStructure;

public class VrrpGroup extends ComparableStructure<Integer> {

  public static final int MAX_PRIORITY = 255;

  public static class Builder {
    private int _name;

    private int _priority;

    private ConcreteInterfaceAddress _virtualAddress;

    private boolean _preempt;

    public VrrpGroup build() {
      return new VrrpGroup(this);
    }

    public Builder setName(int name) {
      _name = name;
      return this;
    }

    public Builder setPriority(int priority) {
      _priority = priority;
      return this;
    }

    public Builder setVirtualAddress(ConcreteInterfaceAddress virtualAddress) {
      _virtualAddress = virtualAddress;
      return this;
    }

    public @Nonnull Builder setPreempt(boolean preempt) {
      _preempt = preempt;
      return this;
    }
  }

  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";

  private static final String PROP_VIRTUAL_ADDRESS = "virtualAddress";

  private boolean _preempt;

  private int _priority;

  private ConcreteInterfaceAddress _virtualAddress;

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  public VrrpGroup(@JsonProperty(PROP_NAME) Integer name) {
    super(name);
  }

  private VrrpGroup(Builder builder) {
    super(builder._name);
    _preempt = builder._preempt;
    _priority = builder._priority;
    _virtualAddress = builder._virtualAddress;
  }

  @JsonProperty(PROP_PREEMPT)
  public boolean getPreempt() {
    return _preempt;
  }

  @JsonProperty(PROP_PRIORITY)
  public int getPriority() {
    return _priority;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESS)
  public ConcreteInterfaceAddress getVirtualAddress() {
    return _virtualAddress;
  }

  @JsonProperty(PROP_PREEMPT)
  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }

  @JsonProperty(PROP_PRIORITY)
  public void setPriority(int priority) {
    _priority = priority;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESS)
  public void setVirtualAddress(ConcreteInterfaceAddress virtualAddress) {
    _virtualAddress = virtualAddress;
  }
}
