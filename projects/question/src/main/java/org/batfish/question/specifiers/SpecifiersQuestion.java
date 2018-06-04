package org.batfish.question.specifiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;

/**
 * Allows users to see how {@link LocationSpecifier location} and {@link IpSpaceSpecifier ipSpace}
 * specifiers are resolved.
 */
public final class SpecifiersQuestion extends Question {
  private static final String PROP_IP_SPACE_SPECIFIER_FACTORY = "ipSpaceSpecifierFactory";

  private static final String PROP_IP_SPACE_SPECIFIER_INPUT = "ipSpaceSpecifierInput";

  private static final String PROP_LOCATION_SPECIFIER_FACTORY = "locationSpecifierFactory";

  private static final String PROP_LOCATION_SPECIFIER_INPUT = "locationSpecifierInput";

  private String _ipSpaceSpecifierFactory;

  private String _ipSpaceSpecifierInput;

  private String _locationSpecifierFactory;

  private String _locationSpecifierInput;

  SpecifiersQuestion() {}

  IpSpaceSpecifier getIpSpaceSpecifier() {
    Objects.requireNonNull(
        _ipSpaceSpecifierFactory, String.format("%s is required", PROP_IP_SPACE_SPECIFIER_FACTORY));

    IpSpaceSpecifierFactory ipSpaceSpecifierFactory =
        IpSpaceSpecifierFactory.load(_ipSpaceSpecifierFactory);

    return ipSpaceSpecifierFactory.buildIpSpaceSpecifier(_ipSpaceSpecifierInput);
  }

  LocationSpecifier getLocationSpecifier() {
    Objects.requireNonNull(
        _locationSpecifierFactory,
        String.format("%s is required", PROP_LOCATION_SPECIFIER_FACTORY));

    LocationSpecifierFactory locationSpecifierFactory =
        LocationSpecifierFactory.load(_locationSpecifierFactory);

    return locationSpecifierFactory.buildLocationSpecifier(_locationSpecifierInput);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "specifiers";
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_FACTORY)
  public String getIpSpaceSpecifierFactory() {
    return _ipSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public String getIpSpaceSpecifierInput() {
    return _ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_FACTORY)
  public String getSourceSpecifierFactory() {
    return _locationSpecifierFactory;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public String getSourceSpecifierInput() {
    return _locationSpecifierInput;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_FACTORY)
  public void setIpSpaceSpecifierFactory(String ipSpaceSpecifierFactory) {
    _ipSpaceSpecifierFactory = ipSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public void setIpSpaceSpecifierInput(String ipSpaceSpecifierInput) {
    _ipSpaceSpecifierInput = ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_FACTORY)
  public void getLocationSpecifierFactory(String locationSpecifierFactory) {
    _locationSpecifierFactory = locationSpecifierFactory;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public void getLocationSpecifierInput(String locationSpecifierInput) {
    _locationSpecifierInput = locationSpecifierInput;
  }
}
