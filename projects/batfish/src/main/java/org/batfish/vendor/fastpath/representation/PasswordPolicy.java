package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * FastPath password-policy settings ({@code passwords ...}): lockout count, aging, history depth,
 * minimum length, and the {@code passwords strength ...} rules.
 */
public final class PasswordPolicy implements Serializable {

  private @Nullable Integer _lockOut;
  private @Nullable Integer _aging;
  private @Nullable Integer _history;
  private @Nullable Integer _minLength;
  private boolean _strengthCheck;
  private @Nullable Integer _maxConsecutiveCharacters;
  private @Nullable Integer _maxRepeatedCharacters;
  private @Nullable Integer _minUppercaseLetters;
  private @Nullable Integer _minLowercaseLetters;
  private @Nullable Integer _minNumericCharacters;
  private @Nullable Integer _minSpecialCharacters;
  private @Nullable Integer _minCharacterClasses;
  private final @Nonnull List<String> _excludeKeywords = new ArrayList<>();

  public @Nullable Integer getLockOut() {
    return _lockOut;
  }

  public void setLockOut(@Nullable Integer lockOut) {
    _lockOut = lockOut;
  }

  public @Nullable Integer getAging() {
    return _aging;
  }

  public void setAging(@Nullable Integer aging) {
    _aging = aging;
  }

  public @Nullable Integer getHistory() {
    return _history;
  }

  public void setHistory(@Nullable Integer history) {
    _history = history;
  }

  public @Nullable Integer getMinLength() {
    return _minLength;
  }

  public void setMinLength(@Nullable Integer minLength) {
    _minLength = minLength;
  }

  public boolean getStrengthCheck() {
    return _strengthCheck;
  }

  public void setStrengthCheck(boolean strengthCheck) {
    _strengthCheck = strengthCheck;
  }

  public @Nullable Integer getMaxConsecutiveCharacters() {
    return _maxConsecutiveCharacters;
  }

  public void setMaxConsecutiveCharacters(@Nullable Integer maxConsecutiveCharacters) {
    _maxConsecutiveCharacters = maxConsecutiveCharacters;
  }

  public @Nullable Integer getMaxRepeatedCharacters() {
    return _maxRepeatedCharacters;
  }

  public void setMaxRepeatedCharacters(@Nullable Integer maxRepeatedCharacters) {
    _maxRepeatedCharacters = maxRepeatedCharacters;
  }

  public @Nullable Integer getMinUppercaseLetters() {
    return _minUppercaseLetters;
  }

  public void setMinUppercaseLetters(@Nullable Integer minUppercaseLetters) {
    _minUppercaseLetters = minUppercaseLetters;
  }

  public @Nullable Integer getMinLowercaseLetters() {
    return _minLowercaseLetters;
  }

  public void setMinLowercaseLetters(@Nullable Integer minLowercaseLetters) {
    _minLowercaseLetters = minLowercaseLetters;
  }

  public @Nullable Integer getMinNumericCharacters() {
    return _minNumericCharacters;
  }

  public void setMinNumericCharacters(@Nullable Integer minNumericCharacters) {
    _minNumericCharacters = minNumericCharacters;
  }

  public @Nullable Integer getMinSpecialCharacters() {
    return _minSpecialCharacters;
  }

  public void setMinSpecialCharacters(@Nullable Integer minSpecialCharacters) {
    _minSpecialCharacters = minSpecialCharacters;
  }

  public @Nullable Integer getMinCharacterClasses() {
    return _minCharacterClasses;
  }

  public void setMinCharacterClasses(@Nullable Integer minCharacterClasses) {
    _minCharacterClasses = minCharacterClasses;
  }

  public @Nonnull List<String> getExcludeKeywords() {
    return _excludeKeywords;
  }
}
