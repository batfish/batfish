package org.batfish.question.specifiers;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
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

  public SpecifiersQuestion() {}

  public IpSpaceSpecifier getIpSpaceSpecifier() {
    if (_ipSpaceSpecifierFactory == null && _ipSpaceSpecifierInput != null) {
      throw new BatfishException("Cannot specify a specifier input without a specifier factory");
    }

    String ipSpaceSpecifierFactory =
        firstNonNull(_ipSpaceSpecifierFactory, "ConstantUniverseIpSpaceSpecifierFactory");

    IpSpaceSpecifierFactory factory = IpSpaceSpecifierFactory.load(ipSpaceSpecifierFactory);

    return factory.buildIpSpaceSpecifier(_ipSpaceSpecifierInput);
  }

  public LocationSpecifier getLocationSpecifier() {
    if (_locationSpecifierFactory == null && _locationSpecifierInput != null) {
      throw new BatfishException("Cannot specify a specifier input without a specifier factory");
    }

    String locationSpecifierFactory =
        firstNonNull(_locationSpecifierFactory, "AllInterfacesLocationSpecifierFactory");

    LocationSpecifierFactory factory = LocationSpecifierFactory.load(locationSpecifierFactory);

    return factory.buildLocationSpecifier(_locationSpecifierInput);
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
  public void setLocationSpecifierFactory(String locationSpecifierFactory) {
    _locationSpecifierFactory = locationSpecifierFactory;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public void setLocationSpecifierInput(String locationSpecifierInput) {
    _locationSpecifierInput = locationSpecifierInput;
  }
}
