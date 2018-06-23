package org.batfish.datamodel.matchers;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class ConvertConfigurationAnswerElementMatchers {

  static final class HasRedFlagWarning
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final @Nonnull Matcher<? super String> _subMatcher;

    private final @Nonnull String _hostname;

    HasRedFlagWarning(@Nonnull String hostname, @Nonnull Matcher<? super String> subMatcher) {
      _hostname = hostname;
      _subMatcher = subMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description
          .appendText("A ConvertConfigurationAnswerElement with a red-flag warning with text:")
          .appendDescriptionOf(_subMatcher);
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      Warnings warnings = item.getWarnings().get(_hostname);
      if (warnings == null) {
        mismatchDescription.appendText(String.format("No warnings for host '%s'", _hostname));
        return false;
      }
      if (warnings
          .getRedFlagWarnings()
          .stream()
          .map(Warning::getText)
          .noneMatch(_subMatcher::matches)) {
        mismatchDescription.appendText(
            String.format("No red-flag warnings for host '%s' match", _hostname));
        return false;
      }
      return true;
    }
  }

  static final class HasDefinedStructure
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _hostname;

    private final String _structureName;

    private final String _type;

    HasDefinedStructure(
        @Nonnull String hostname, @Nonnull StructureType type, @Nonnull String structureName) {
      _hostname = hostname;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which host '%s' has a defined structure "
                  + "of type '%s' named '%s'",
              _hostname, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byHostname =
          item.getDefinedStructures();
      if (!byHostname.containsKey(_hostname)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structures", _hostname));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byHostname.get(_hostname);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structure of type '%s'", _hostname, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no defined structure of type '%s' named '%s'",
                _hostname, _type, _structureName));
        return false;
      }
      return true;
    }
  }

  static final class HasDefinedStructureWithDefinitionLines
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final Matcher<? super Set<Integer>> _subMatcher;

    private final String _hostname;

    private final String _structureName;

    private final String _type;

    HasDefinedStructureWithDefinitionLines(
        @Nonnull String hostname,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull Matcher<? super Set<Integer>> subMatcher) {
      _subMatcher = subMatcher;
      _hostname = hostname;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which host '%s' has a defined structure "
                  + "of type '%s' named '%s' with definition lines '%s'",
              _hostname, _type, _structureName, _subMatcher));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byHostname =
          item.getDefinedStructures();
      if (!byHostname.containsKey(_hostname)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structures", _hostname));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byHostname.get(_hostname);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structure of type '%s'", _hostname, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no defined structure of type '%s' named '%s'",
                _hostname, _type, _structureName));
        return false;
      }
      if (!_subMatcher.matches(byStructureName.get(_structureName).getDefinitionLines())) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no defined structure of type '%s' named '%s' matching definition lines '%s'",
                _hostname, _type, _structureName, _subMatcher));
        return false;
      }
      return true;
    }
  }

  static final class HasUndefinedReference
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _hostname;

    private final String _structureName;

    private final String _type;

    HasUndefinedReference(
        @Nonnull String hostname, @Nonnull StructureType type, @Nonnull String structureName) {
      _hostname = hostname;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which host '%s' has an undefined reference "
                  + "to a structure of type '%s' named '%s'",
              _hostname, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byHostname = item.getUndefinedReferences();
      if (!byHostname.containsKey(_hostname)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no undefined references", _hostname));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byHostname.get(_hostname);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no undefined references to structures of type '%s'",
                _hostname, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no undefined references to structures of type '%s' named '%s'",
                _hostname, _type, _structureName));
        return false;
      }
      return true;
    }
  }

  static final class HasUndefinedReferenceWithUsage
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _hostname;

    private final String _structureName;

    private final String _type;

    private final String _usage;

    HasUndefinedReferenceWithUsage(
        @Nonnull String hostname,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull StructureUsage usage) {
      _hostname = hostname;
      _type = type.getDescription();
      _structureName = structureName;
      _usage = usage.getDescription();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which host '%s' has an undefined reference "
                  + "to a structure of type '%s' named '%s'",
              _hostname, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byHostname = item.getUndefinedReferences();
      if (!byHostname.containsKey(_hostname)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no undefined references", _hostname));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byHostname.get(_hostname);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no undefined references to structures of type '%s'",
                _hostname, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no undefined references to structures of type '%s' named '%s'",
                _hostname, _type, _structureName));
        return false;
      }
      SortedMap<String, SortedSet<Integer>> byUsage = byStructureName.get(_structureName);
      if (!byUsage.containsKey(_usage)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no undefined references to structures of type '%s' named '%s' of "
                    + "usage '%s'",
                _hostname, _type, _structureName, _usage));
        return false;
      }
      return true;
    }
  }

  static final class HasNumReferrers
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _hostname;

    private final int _numReferrers;

    private final String _structureName;

    private final String _type;

    HasNumReferrers(
        @Nonnull String hostname,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        int numReferrers) {
      _hostname = hostname;
      _numReferrers = numReferrers;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which host '%s' has defined structure of "
                  + "type '%s' named '%s' with %d referrers",
              _hostname, _type, _structureName, _numReferrers));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byHostname =
          item.getDefinedStructures();
      if (!byHostname.containsKey(_hostname)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structures", _hostname));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byHostname.get(_hostname);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("Host '%s' has no defined structures of type '%s'", _hostname, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "Host '%s' has no defined structures of type '%s' named '%s'",
                _hostname, _type, _structureName));
        return false;
      }
      if (byStructureName.get(_structureName).getNumReferrers() != _numReferrers) {
        mismatchDescription.appendText(
            String.format(
                "On host '%s', defined structure of type '%s' named '%s' has %d referrers",
                _hostname,
                _type,
                _structureName,
                byStructureName.get(_structureName).getNumReferrers()));
        return false;
      }
      return true;
    }
  }

  private ConvertConfigurationAnswerElementMatchers() {}
}
