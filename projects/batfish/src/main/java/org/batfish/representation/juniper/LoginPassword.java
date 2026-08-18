package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Vendor-specific model of the Junos {@code [edit system login password]} block, which sets
 * complexity and lifetime requirements for locally-configured plain-text passwords.
 */
public class LoginPassword implements Serializable {

  /** Password hashing/authentication algorithm ({@code format}). */
  public enum Format {
    SHA256,
    SHA512,
  }

  /** What {@code minimum-changes} requirement counts ({@code change-type}). */
  public enum ChangeType {
    CHARACTER_SETS,
    SET_TRANSITIONS,
  }

  private @Nullable Format _format;
  private @Nullable ChangeType _changeType;
  private @Nullable Integer _minimumLength;
  private @Nullable Integer _maximumLength;
  private @Nullable Integer _minimumChanges;
  private @Nullable Integer _minimumCharacterChanges;
  private @Nullable Integer _minimumLowerCases;
  private @Nullable Integer _minimumUpperCases;
  private @Nullable Integer _minimumNumerics;
  private @Nullable Integer _minimumPunctuations;
  private @Nullable Integer _minimumReuse;
  private @Nullable Integer _minimumLifetime;
  private @Nullable Integer _maximumLifetime;

  public @Nullable Format getFormat() {
    return _format;
  }

  public void setFormat(@Nullable Format format) {
    _format = format;
  }

  public @Nullable ChangeType getChangeType() {
    return _changeType;
  }

  public void setChangeType(@Nullable ChangeType changeType) {
    _changeType = changeType;
  }

  public @Nullable Integer getMinimumLength() {
    return _minimumLength;
  }

  public void setMinimumLength(@Nullable Integer minimumLength) {
    _minimumLength = minimumLength;
  }

  public @Nullable Integer getMaximumLength() {
    return _maximumLength;
  }

  public void setMaximumLength(@Nullable Integer maximumLength) {
    _maximumLength = maximumLength;
  }

  public @Nullable Integer getMinimumChanges() {
    return _minimumChanges;
  }

  public void setMinimumChanges(@Nullable Integer minimumChanges) {
    _minimumChanges = minimumChanges;
  }

  public @Nullable Integer getMinimumCharacterChanges() {
    return _minimumCharacterChanges;
  }

  public void setMinimumCharacterChanges(@Nullable Integer minimumCharacterChanges) {
    _minimumCharacterChanges = minimumCharacterChanges;
  }

  public @Nullable Integer getMinimumLowerCases() {
    return _minimumLowerCases;
  }

  public void setMinimumLowerCases(@Nullable Integer minimumLowerCases) {
    _minimumLowerCases = minimumLowerCases;
  }

  public @Nullable Integer getMinimumUpperCases() {
    return _minimumUpperCases;
  }

  public void setMinimumUpperCases(@Nullable Integer minimumUpperCases) {
    _minimumUpperCases = minimumUpperCases;
  }

  public @Nullable Integer getMinimumNumerics() {
    return _minimumNumerics;
  }

  public void setMinimumNumerics(@Nullable Integer minimumNumerics) {
    _minimumNumerics = minimumNumerics;
  }

  public @Nullable Integer getMinimumPunctuations() {
    return _minimumPunctuations;
  }

  public void setMinimumPunctuations(@Nullable Integer minimumPunctuations) {
    _minimumPunctuations = minimumPunctuations;
  }

  public @Nullable Integer getMinimumReuse() {
    return _minimumReuse;
  }

  public void setMinimumReuse(@Nullable Integer minimumReuse) {
    _minimumReuse = minimumReuse;
  }

  public @Nullable Integer getMinimumLifetime() {
    return _minimumLifetime;
  }

  public void setMinimumLifetime(@Nullable Integer minimumLifetime) {
    _minimumLifetime = minimumLifetime;
  }

  public @Nullable Integer getMaximumLifetime() {
    return _maximumLifetime;
  }

  public void setMaximumLifetime(@Nullable Integer maximumLifetime) {
    _maximumLifetime = maximumLifetime;
  }
}
