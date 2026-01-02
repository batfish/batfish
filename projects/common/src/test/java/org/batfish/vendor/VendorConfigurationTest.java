package org.batfish.vendor;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlag;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasReferencedStructure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.junit.Test;

/** Test for {@link VendorConfiguration}. */
public final class VendorConfigurationTest {

  private static final String FILENAME = "filename";

  private enum TestStructureType implements StructureType {
    TEST_STRUCTURE_TYPE1("type1"),
    TEST_STRUCTURE_TYPE2("type2");

    private final String _description;

    TestStructureType(String description) {
      _description = description;
    }

    @Override
    public String getDescription() {
      return _description;
    }
  }

  private enum TestStructureUsage implements StructureUsage {
    TEST_STRUCTURE_USAGE1("usage1");

    private final String _description;

    TestStructureUsage(String description) {
      _description = description;
    }

    @Override
    public String getDescription() {
      return _description;
    }
  }

  private static final TestStructureType _testStructureType1 =
      TestStructureType.TEST_STRUCTURE_TYPE1;
  private static final TestStructureType _testStructureType2 =
      TestStructureType.TEST_STRUCTURE_TYPE2;
  private static final TestStructureUsage _testStructureUsage =
      TestStructureUsage.TEST_STRUCTURE_USAGE1;

  private static final class TestVendorConfiguration extends VendorConfiguration {
    @Override
    public String getHostname() {
      return "hostname";
    }

    @Override
    public void setHostname(String hostname) {}

    @Override
    public void setVendor(ConfigurationFormat format) {}

    @Override
    public List<Configuration> toVendorIndependentConfigurations()
        throws VendorConversionException {
      return ImmutableList.of();
    }
  }

  private static VendorConfiguration buildVendorConfiguration() {
    VendorConfiguration c = new TestVendorConfiguration();
    c.setFilename(FILENAME);
    c.setWarnings(new Warnings(true, true, true));
    return c;
  }

  @Test
  public void testDeleteStructureUndefined() {
    String origName = "origName";
    int origLine = 1;

    String otherName = "otherName";
    int otherLine = 2;

    // Same-named structure of a different type exists
    {
      VendorConfiguration c = buildVendorConfiguration();
      c.defineSingleLineStructure(_testStructureType2, origName, origLine);
      c.referenceStructure(_testStructureType2, origName, _testStructureUsage, 11);

      // Try to delete a structure defined in the same namespace, but of a different type
      assertFalse(c.deleteStructure(origName, _testStructureType1));

      // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());
      // Should produce an appropriate warning and indicate the rename did not succeed
      assertThat(
          c.getWarnings(),
          hasRedFlag(hasText("Cannot delete structure origName (type1): origName is undefined.")));

      // Reference should be unaffected since the delete did not succeed
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType2, origName, _testStructureUsage));
    }

    // Different structure of the same type exists
    {
      VendorConfiguration c = buildVendorConfiguration();
      c.defineSingleLineStructure(_testStructureType1, otherName, otherLine);
      c.referenceStructure(_testStructureType1, otherName, _testStructureUsage, 11);

      // Try to delete a structure that doesn't exist
      assertFalse(c.deleteStructure(origName, _testStructureType1));

      // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());
      // Should produce an appropriate warning and indicate the rename did not succeed
      assertThat(
          c.getWarnings(),
          hasRedFlag(hasText("Cannot delete structure origName (type1): origName is undefined.")));

      // Reference should be unaffected since the delete did not succeed
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType1, otherName, _testStructureUsage));
    }
  }

  @Test
  public void testDeleteStructureValid() {
    String origName = "origName";
    int origLine = 1;
    int origRefLine = 11;

    String unaffectedName = "unaffectedName";
    int otherLine = 2;
    int otherRefLine = 22;

    VendorConfiguration c = buildVendorConfiguration();

    c.defineSingleLineStructure(_testStructureType1, origName, origLine);
    c.defineSingleLineStructure(_testStructureType1, unaffectedName, otherLine);
    c.referenceStructure(_testStructureType1, origName, _testStructureUsage, origRefLine);
    c.referenceStructure(_testStructureType1, unaffectedName, _testStructureUsage, otherRefLine);
    assertTrue(c.deleteStructure(origName, _testStructureType1));

    // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    c.getStructureManager().saveInto(ccae, c.getFilename());
    // No warnings
    assertThat(c.getWarnings(), hasRedFlags(emptyIterable()));
    // Has no definition or reference for the old name
    assertThat(ccae, not(hasDefinedStructure(FILENAME, _testStructureType1, origName)));
    assertThat(
        ccae,
        not(hasReferencedStructure(FILENAME, _testStructureType1, origName, _testStructureUsage)));

    // Unaffected reference and definition should persist
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            FILENAME, _testStructureType1, unaffectedName, contains(otherLine)));
    assertThat(
        ccae,
        hasReferencedStructure(FILENAME, _testStructureType1, unaffectedName, _testStructureUsage));
  }

  @Test
  public void testRenameStructureUndefined() {
    String origName = "origName";
    int origLine = 1;
    String newName = "newName";

    // Same-named structure exists in a different namespace
    {
      VendorConfiguration c = buildVendorConfiguration();

      c.defineSingleLineStructure(_testStructureType2, origName, origLine);

      // Rename an undefined structure
      boolean succeeded =
          c.renameStructure(
              origName, newName, _testStructureType1, ImmutableList.of(_testStructureType1));

      // Should produce an appropriate warning and indicate the rename did not succeed
      assertThat(
          c.getWarnings(),
          hasRedFlag(
              hasText(
                  "Cannot rename structure origName (type1) to newName: origName is undefined.")));
      assertFalse(succeeded);
    }

    // Same-named structure exists in the same namespace, but is a different type
    {
      VendorConfiguration c = buildVendorConfiguration();

      c.defineSingleLineStructure(_testStructureType2, origName, origLine);
      c.referenceStructure(_testStructureType2, origName, _testStructureUsage, 11);

      // Try to rename a structure defined in the same namespace, but of a different type
      boolean succeeded =
          c.renameStructure(
              origName,
              newName,
              _testStructureType1,
              ImmutableList.of(_testStructureType1, _testStructureType2));

      // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());

      // Should produce an appropriate warning and indicate the rename did not succeed
      assertThat(
          c.getWarnings(),
          hasRedFlag(
              hasText(
                  "Cannot rename structure origName (type1) to newName: origName is undefined.")));
      assertFalse(succeeded);
      // Reference should be unaffected since the rename did not succeed
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType2, origName, _testStructureUsage));
    }
  }

  @Test
  public void testRenameStructureClash() {
    String origName = "origName";
    int origLine = 1;
    String newName = "newName";
    int otherLine = 2;

    // Renaming is invalid if it would clash with another structure of the same type
    {
      VendorConfiguration c = buildVendorConfiguration();

      // Both structures are of the same type
      c.defineSingleLineStructure(_testStructureType1, origName, origLine);
      c.defineSingleLineStructure(_testStructureType1, newName, otherLine);

      boolean succeeded =
          c.renameStructure(
              origName, newName, _testStructureType1, ImmutableList.of(_testStructureType1));
      // Produces appropriate warning
      assertThat(
          c.getWarnings(),
          hasRedFlag(
              hasText(
                  "Cannot rename structure origName (type1) to newName: newName is already in use"
                      + " as type1.")));
      assertFalse(succeeded);

      // Both org and new structure defs persist
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, origName, contains(origLine)));
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, newName, contains(otherLine)));
    }

    // Renaming is invalid if it would clash with another structure
    // **Even of a different type**
    {
      VendorConfiguration c = buildVendorConfiguration();

      // Structures are different types
      c.defineSingleLineStructure(_testStructureType2, origName, origLine);
      c.defineSingleLineStructure(_testStructureType1, newName, otherLine);

      boolean succeeded =
          c.renameStructure(
              origName,
              newName,
              _testStructureType2,
              ImmutableList.of(_testStructureType1, _testStructureType2));
      // Produces appropriate warning
      assertThat(
          c.getWarnings(),
          hasRedFlag(
              hasText(
                  "Cannot rename structure origName (type2) to newName: newName is already in use"
                      + " as type1.")));
      assertFalse(succeeded);

      // Both org and new structure defs persist
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType2, origName, contains(origLine)));
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, newName, contains(otherLine)));
    }
  }

  @Test
  public void testRenameStructureValid() {
    String origName = "origName";
    int origLine = 1;
    String newName = "newName";

    int otherLine = 2;
    String unaffectedName = "unaffectedName";

    // Valid structure renaming
    {
      VendorConfiguration c = buildVendorConfiguration();

      c.defineSingleLineStructure(_testStructureType1, origName, origLine);
      c.defineSingleLineStructure(_testStructureType1, unaffectedName, otherLine);
      c.referenceStructure(_testStructureType1, origName, _testStructureUsage, 11);
      c.referenceStructure(_testStructureType1, unaffectedName, _testStructureUsage, 22);
      boolean succeeded =
          c.renameStructure(
              origName, newName, _testStructureType1, ImmutableList.of(_testStructureType1));
      // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());

      // No warnings
      assertThat(c.getWarnings(), hasRedFlags(emptyIterable()));
      assertTrue(succeeded);
      // Has a definition and reference for the new structure name
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, newName, contains(origLine)));
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType1, newName, _testStructureUsage));
      // Unaffected reference and definition should persist
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, unaffectedName, contains(otherLine)));
      assertThat(
          ccae,
          hasReferencedStructure(
              FILENAME, _testStructureType1, unaffectedName, _testStructureUsage));
    }

    // Structure renaming is still valid the new name is in used by a different type of structure
    {
      VendorConfiguration c = buildVendorConfiguration();

      c.defineSingleLineStructure(_testStructureType1, origName, origLine);
      c.defineSingleLineStructure(_testStructureType2, newName, otherLine);
      c.referenceStructure(_testStructureType1, origName, _testStructureUsage, 11);
      c.referenceStructure(_testStructureType2, newName, _testStructureUsage, 22);
      boolean succeeded =
          c.renameStructure(
              origName, newName, _testStructureType1, ImmutableList.of(_testStructureType1));
      // Need to call setAnswerElement to trigger population of CCAE / answerElement (for refs)
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      c.getStructureManager().saveInto(ccae, c.getFilename());

      // No warnings
      assertThat(c.getWarnings(), hasRedFlags(emptyIterable()));
      assertTrue(succeeded);
      // Has a definition and reference for the new structure name
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType1, newName, contains(origLine)));
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType1, newName, _testStructureUsage));
      // Unaffected reference and definition should persist
      assertThat(
          ccae,
          hasDefinedStructureWithDefinitionLines(
              FILENAME, _testStructureType2, newName, contains(otherLine)));
      assertThat(
          ccae,
          hasReferencedStructure(FILENAME, _testStructureType2, newName, _testStructureUsage));
    }
  }
}
