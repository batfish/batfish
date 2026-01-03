package org.batfish.datamodel.matchers;

import java.util.Map.Entry;
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
      if (warnings.getRedFlagWarnings().stream()
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

    private final String _filename;

    private final String _structureName;

    private final String _type;

    HasDefinedStructure(
        @Nonnull String filename, @Nonnull StructureType type, @Nonnull String structureName) {
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has a defined structure "
                  + "of type '%s' named '%s'",
              _filename, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byFile =
          item.getDefinedStructures();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structures", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structure of type '%s'", _filename, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no defined structure of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      return true;
    }
  }

  static final class HasDefinedStructureWithDefinitionLines
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final Matcher<? super Set<Integer>> _subMatcher;

    private final String _filename;

    private final String _structureName;

    private final String _type;

    HasDefinedStructureWithDefinitionLines(
        @Nonnull String filename,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull Matcher<? super Set<Integer>> subMatcher) {
      _subMatcher = subMatcher;
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has a defined structure "
                  + "of type '%s' named '%s' with definition lines '%s'",
              _filename, _type, _structureName, _subMatcher));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byFile =
          item.getDefinedStructures();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structures", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structure of type '%s'", _filename, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no defined structure of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      Set<Integer> definitionLines =
          byStructureName.get(_structureName).getDefinitionLines().enumerate();
      if (!_subMatcher.matches(definitionLines)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' defined structure of type '%s' named '%s' definition lines were %s",
                _filename, _type, _structureName, definitionLines));
        return false;
      }
      return true;
    }
  }

  static final class HasUndefinedReference
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _filename;

    private final String _structureName;

    private final String _type;

    HasUndefinedReference(
        @Nonnull String filename, @Nonnull StructureType type, @Nonnull String structureName) {
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has an undefined reference "
                  + "to a structure of type '%s' named '%s'",
              _filename, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byFile = item.getUndefinedReferences();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no undefined references", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s'",
                _filename, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      return true;
    }
  }

  static final class HasNoUndefinedReferences
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    HasNoUndefinedReferences() {}

    @Override
    public void describeTo(Description description) {
      description.appendText(
          "A ConvertConfigurationAnswerElement which has no undefined references");
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byFile = item.getUndefinedReferences();
      if (byFile.isEmpty()) {
        return true;
      }

      for (Entry<
              String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          fileToTypeMap : byFile.entrySet()) {
        for (Entry<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
            typeToStructNameMap : fileToTypeMap.getValue().entrySet()) {
          for (Entry<String, SortedMap<String, SortedSet<Integer>>> structNameToUsageMap :
              typeToStructNameMap.getValue().entrySet()) {
            SortedMap<String, SortedSet<Integer>> structUsageMap = structNameToUsageMap.getValue();
            if (!structUsageMap.isEmpty()) {
              mismatchDescription.appendText(
                  String.format(
                      "ConvertConfigurationAnswerElement has undefined references, including for"
                          + " file '%s', structure type '%s', named '%s', with usage '%s'",
                      fileToTypeMap.getKey(),
                      typeToStructNameMap.getKey(),
                      structNameToUsageMap.getKey(),
                      structUsageMap.firstKey()));
              return false;
            }
          }
        }
      }
      return true;
    }
  }

  static final class HasUndefinedReferenceWithUsage
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _filename;

    private final String _structureName;

    private final String _type;

    private final String _usage;

    HasUndefinedReferenceWithUsage(
        @Nonnull String filename,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull StructureUsage usage) {
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
      _usage = usage.getDescription();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has an undefined reference "
                  + "to a structure of type '%s' named '%s'",
              _filename, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byFile = item.getUndefinedReferences();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no undefined references", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s'",
                _filename, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      SortedMap<String, SortedSet<Integer>> byUsage = byStructureName.get(_structureName);
      if (!byUsage.containsKey(_usage)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s' named '%s' of "
                    + "usage '%s'",
                _filename, _type, _structureName, _usage));
        return false;
      }
      return true;
    }
  }

  static final class HasReferenceWithUsage
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _filename;

    private final String _structureName;

    private final String _type;

    private final String _usage;

    HasReferenceWithUsage(
        @Nonnull String filename,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull StructureUsage usage) {
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
      _usage = usage.getDescription();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has a reference "
                  + "to a structure of type '%s' named '%s'",
              _filename, _type, _structureName));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byFile = item.getReferencedStructures();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(String.format("File '%s' has no references", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no references to structures of type '%s'", _filename, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no references to structures of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      SortedMap<String, SortedSet<Integer>> byUsage = byStructureName.get(_structureName);
      if (!byUsage.containsKey(_usage)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no references to structures of type '%s' named '%s' of "
                    + "usage '%s'",
                _filename, _type, _structureName, _usage));
        return false;
      }
      return true;
    }
  }

  static final class HasUndefinedReferenceWithReferenceLines
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final Matcher<? super Set<Integer>> _subMatcher;

    private final String _filename;

    private final String _structureName;

    private final String _type;

    private final String _usage;

    HasUndefinedReferenceWithReferenceLines(
        @Nonnull String filename,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        @Nonnull StructureUsage usage,
        @Nonnull Matcher<? super Set<Integer>> subMatcher) {
      _subMatcher = subMatcher;
      _filename = filename;
      _type = type.getDescription();
      _structureName = structureName;
      _usage = usage.getDescription();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has an undefined reference "
                  + "of type '%s' named '%s' with reference lines '%s'",
              _filename, _type, _structureName, _subMatcher));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          byFile = item.getUndefinedReferences();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no undefined references", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> byType =
          byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no undefined reference of type '%s'", _filename, _type));
        return false;
      }
      SortedMap<String, SortedMap<String, SortedSet<Integer>>> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined reference of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      SortedMap<String, SortedSet<Integer>> byUsage = byStructureName.get(_structureName);
      if (!byUsage.containsKey(_usage)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined references to structures of type '%s' named '%s' of "
                    + "usage '%s'",
                _filename, _type, _structureName, _usage));
        return false;
      }
      if (!_subMatcher.matches(byUsage.get(_usage))) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no undefined reference of type '%s' named '%s' of usage '%s'"
                    + " matching reference lines '%s'",
                _filename, _type, _structureName, _usage, _subMatcher));
        return false;
      }
      return true;
    }
  }

  static final class HasNumReferrers
      extends TypeSafeDiagnosingMatcher<ConvertConfigurationAnswerElement> {

    private final String _filename;

    private final int _numReferrers;

    private final String _structureName;

    private final String _type;

    HasNumReferrers(
        @Nonnull String filename,
        @Nonnull StructureType type,
        @Nonnull String structureName,
        int numReferrers) {
      _filename = filename;
      _numReferrers = numReferrers;
      _type = type.getDescription();
      _structureName = structureName;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "A ConvertConfigurationAnswerElement for which file '%s' has defined structure of "
                  + "type '%s' named '%s' with %d referrers",
              _filename, _type, _structureName, _numReferrers));
    }

    @Override
    protected boolean matchesSafely(
        ConvertConfigurationAnswerElement item, Description mismatchDescription) {
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> byFile =
          item.getDefinedStructures();
      if (!byFile.containsKey(_filename)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structures", _filename));
        return false;
      }
      SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = byFile.get(_filename);
      if (!byType.containsKey(_type)) {
        mismatchDescription.appendText(
            String.format("File '%s' has no defined structures of type '%s'", _filename, _type));
        return false;
      }
      SortedMap<String, DefinedStructureInfo> byStructureName = byType.get(_type);
      if (!byStructureName.containsKey(_structureName)) {
        mismatchDescription.appendText(
            String.format(
                "File '%s' has no defined structures of type '%s' named '%s'",
                _filename, _type, _structureName));
        return false;
      }
      if (byStructureName.get(_structureName).getNumReferrers() != _numReferrers) {
        mismatchDescription.appendText(
            String.format(
                "In file '%s', defined structure of type '%s' named '%s' has %d referrers",
                _filename,
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
