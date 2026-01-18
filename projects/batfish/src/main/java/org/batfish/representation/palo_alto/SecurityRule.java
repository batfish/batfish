package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;

/** PAN datamodel component containing security rule configuration */
@ParametersAreNonnullByDefault
public final class SecurityRule implements Serializable {

  public enum RuleType {
    INTERZONE,
    INTRAZONE,
    UNIVERSAL
  }

  // Name of the rule
  private final @Nonnull String _name;
  // Action of the rule
  private @Nonnull LineAction _action;

  private @Nonnull Set<CustomUrlCategoryReference> _category;

  // Owning Vsys of this rule
  private final @Nonnull Vsys _vsys;

  // Description of the rule
  private @Nullable String _description;

  private boolean _disabled;

  // Zones
  private final @Nonnull SortedSet<String> _from;
  private final @Nonnull SortedSet<String> _to;

  // IPs
  private @Nonnull Set<RuleEndpoint> _source;
  private @Nonnull Set<RuleEndpoint> _destination;
  private boolean _negateSource;
  private boolean _negateDestination;

  // Services
  private final @Nonnull SortedSet<ServiceOrServiceGroupReference> _service;

  // Users
  private final @Nonnull SortedSet<String> _sourceUsers;

  // Applications
  private final @Nonnull SortedSet<ApplicationOrApplicationGroupReference> _applications;

  // Rule type
  private @Nullable RuleType _ruleType;

  private final @Nonnull SortedSet<String> _sourceHips;
  private final @Nonnull SortedSet<String> _destinationHips;
  private final @Nonnull SortedSet<String> _hipProfiles;

  private final @Nonnull Set<String> _tags;

  public SecurityRule(String name, Vsys vsys) {
    _action = LineAction.DENY;
    _applications = new TreeSet<>();
    _category = ImmutableSet.of();
    _destination = ImmutableSet.of();
    _negateDestination = false;
    _disabled = false;
    _from = new TreeSet<>();
    _service = new TreeSet<>();
    _source = ImmutableSet.of();
    _negateSource = false;
    _sourceUsers = new TreeSet<>();
    _to = new TreeSet<>();
    _destinationHips = new TreeSet<>();
    _hipProfiles = new TreeSet<>();
    _sourceHips = new TreeSet<>();
    _tags = new HashSet<>(1);
    _name = name;
    _vsys = vsys;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public void addApplication(String application) {
    _applications.add(new ApplicationOrApplicationGroupReference(application));
  }

  public void addCategory(String category) {
    CustomUrlCategoryReference ref = new CustomUrlCategoryReference(category);
    if (_category.contains(ref)) {
      return;
    }
    _category =
        ImmutableSet.<CustomUrlCategoryReference>builderWithExpectedSize(_category.size() + 1)
            .addAll(_category)
            .add(ref)
            .build();
  }

  public @Nonnull SortedSet<ApplicationOrApplicationGroupReference> getApplications() {
    return ImmutableSortedSet.copyOf(_applications);
  }

  public @Nonnull Set<CustomUrlCategoryReference> getCategory() {
    return _category;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void addDestination(RuleEndpoint endpoint) {
    if (_destination.contains(endpoint)) {
      return;
    }
    _destination =
        ImmutableSet.<RuleEndpoint>builderWithExpectedSize(_destination.size() + 1)
            .addAll(_destination)
            .add(endpoint)
            .build();
  }

  public @Nonnull Set<RuleEndpoint> getDestination() {
    return _destination;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  public @Nonnull SortedSet<String> getFrom() {
    return _from;
  }

  public boolean getNegateDestination() {
    return _negateDestination;
  }

  public void setNegateDestination(boolean negateDestination) {
    _negateDestination = negateDestination;
  }

  public boolean getNegateSource() {
    return _negateSource;
  }

  public void setNegateSource(boolean negateSource) {
    _negateSource = negateSource;
  }

  public @Nonnull SortedSet<ServiceOrServiceGroupReference> getService() {
    return _service;
  }

  public void addSource(RuleEndpoint endpoint) {
    if (_source.contains(endpoint)) {
      return;
    }
    _source =
        ImmutableSet.<RuleEndpoint>builderWithExpectedSize(_source.size() + 1)
            .addAll(_source)
            .add(endpoint)
            .build();
  }

  public @Nonnull Set<RuleEndpoint> getSource() {
    return _source;
  }

  public @Nonnull SortedSet<String> getSourceUsers() {
    return _sourceUsers;
  }

  public void addSourceUser(String sourceUser) {
    _sourceUsers.add(sourceUser);
  }

  public @Nonnull SortedSet<String> getTo() {
    return _to;
  }

  public @Nonnull Set<String> getTags() {
    return _tags;
  }

  public @Nonnull Vsys getVsys() {
    return _vsys;
  }

  public @Nullable RuleType getRuleType() {
    return _ruleType;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }

  public void setRuleType(@Nullable RuleType ruleType) {
    _ruleType = ruleType;
  }

  public @Nonnull SortedSet<String> getDestinationHips() {
    return _destinationHips;
  }

  public void addDestinationHip(String hip) {
    _destinationHips.add(hip);
  }

  public @Nonnull SortedSet<String> getHipProfiles() {
    return _hipProfiles;
  }

  public void addHipProfile(String hipProfile) {
    _hipProfiles.add(hipProfile);
  }

  public @Nonnull SortedSet<String> getSourceHips() {
    return _sourceHips;
  }

  public void addSourceHip(String hip) {
    _sourceHips.add(hip);
  }
}
