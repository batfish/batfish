package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** Enables specification of a set of Ospf interface properties. */
public class OspfInterfacePropertySpecifier extends PropertySpecifier {

  // OSPF interface properties are a subset of interface properties
  public static final String OSPF_AREA_NAME = InterfacePropertySpecifier.OSPF_AREA_NAME;
  public static final String OSPF_PASSIVE = InterfacePropertySpecifier.OSPF_PASSIVE;
  public static final String OSPF_COST = InterfacePropertySpecifier.OSPF_COST;
  public static final String OSPF_POINT_TO_POINT = InterfacePropertySpecifier.OSPF_POINT_TO_POINT;

  // create an ordered list
  private static final List<String> PROPERTIES =
      ImmutableList.of(OSPF_AREA_NAME, OSPF_PASSIVE, OSPF_COST, OSPF_POINT_TO_POINT);

  /** Holds all properties */
  public static final OspfInterfacePropertySpecifier ALL =
      new OspfInterfacePropertySpecifier(ImmutableSet.copyOf(PROPERTIES));

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<Interface> getPropertyDescriptor(String property) {
    checkArgument(PROPERTIES.contains(property), "Property " + property + " does not exist");
    return InterfacePropertySpecifier.getPropertyDescriptor(property);
  }

  @Nonnull private final List<String> _properties;

  /**
   * Create an ospf process property specifier from provided expression. If the expression is null
   * or empty, a specifier with all properties is returned.
   */
  public static OspfInterfacePropertySpecifier create(@Nullable String expression) {
    return new OspfInterfacePropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.OSPF_INTERFACE_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(PROPERTIES)))
            .resolve());
  }

  public OspfInterfacePropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, ImmutableSet.copyOf(PROPERTIES));
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        PROPERTIES);
    // we do this to maintain order
    _properties =
        PROPERTIES.stream().filter(properties::contains).collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OspfInterfacePropertySpecifier)) {
      return false;
    }
    return _properties.equals(((OspfInterfacePropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_properties);
  }
}
