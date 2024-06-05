package org.batfish.datamodel.answers;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessage;
import static org.batfish.datamodel.answers.InputValidationUtils.validateIp;
import static org.batfish.datamodel.answers.InputValidationUtils.validatePrefix;
import static org.batfish.datamodel.answers.InputValidationUtils.validateSourceLocation;
import static org.batfish.specifier.parboiled.InternetLocationAstNode.INTERNET_LOCATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.InputValidationNotes.Validity;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.Variable;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.InterfaceLocation;
import org.junit.Test;

/** Tests for {@link org.batfish.datamodel.answers.InputValidationUtils} */
public class InputValidationUtilsTest {

  private static final CompletionMetadata emptyCompletionMetadata =
      CompletionMetadata.builder().build();
  private static final NodeRolesData emptyNodeRolesData = NodeRolesData.builder().build();
  private static final ReferenceLibrary emptyReferenceLibrary = new ReferenceLibrary(null);

  private static InputValidationNotes validateQuery(String query, Variable.Type varType) {
    return InputValidationUtils.validate(
        varType, query, emptyCompletionMetadata, emptyNodeRolesData, emptyReferenceLibrary);
  }

  @SuppressWarnings("ReturnValueIgnored")
  private static IllegalArgumentException getException(String query, Function<String, ?> getter) {
    try {
      getter.apply(query);
    } catch (IllegalArgumentException e) {
      return e;
    }
    return null;
  }

  @Test
  public void testBgpPeerPropertySpec() {
    // good case
    assertThat(
        validateQuery(BgpPeerPropertySpecifier.LOCAL_IP, Type.BGP_PEER_PROPERTY_SPEC),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of())));

    // bad case
    assertThat(
        validateQuery("dumdum", Type.BGP_PEER_PROPERTY_SPEC),
        equalTo(
            new InputValidationNotes(
                Validity.INVALID,
                getErrorMessage(Grammar.BGP_PEER_PROPERTY_SPECIFIER.getFriendlyName(), 6),
                6)));
  }

  @Test
  public void testBgpProcessPropertySpec() {
    // good case
    assertThat(
        validateQuery(BgpProcessPropertySpecifier.TIE_BREAKER, Type.BGP_PROCESS_PROPERTY_SPEC),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of())));

    // bad case
    assertThat(
        validateQuery("dumdum", Type.BGP_PROCESS_PROPERTY_SPEC),
        equalTo(
            new InputValidationNotes(
                Validity.INVALID,
                getErrorMessage(Grammar.BGP_PROCESS_PROPERTY_SPECIFIER.getFriendlyName(), 6),
                6)));
  }

  @Test
  public void testInterfacePropertySpec() {
    // good case
    assertThat(
        validateQuery(InterfacePropertySpecifier.DESCRIPTION, Type.INTERFACE_PROPERTY_SPEC),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of())));

    // bad case
    assertThat(
        validateQuery("dumdum", Type.INTERFACE_PROPERTY_SPEC),
        equalTo(
            new InputValidationNotes(
                Validity.INVALID,
                getErrorMessage(Grammar.INTERFACE_PROPERTY_SPECIFIER.getFriendlyName(), 6),
                6)));
  }

  @Test
  public void testIpAddressBad() {
    String query = "1.1.1.345";

    IllegalArgumentException exception = getException(query, Ip::parse);

    assertThat(
        validateQuery(query, Type.IP_SPACE_SPEC),
        equalTo(new InputValidationNotes(Validity.INVALID, getErrorMessage(exception), -1)));
  }

  @Test
  public void testIpAddressGood() {
    String query = "1.1.1.3";
    assertThat(
        validateQuery(query, Type.IP_SPACE_SPEC),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of())));
  }

  @Test
  public void testNodePropertySpec() {
    // good case
    assertThat(
        validateQuery(NodePropertySpecifier.NTP_SERVERS, Type.NODE_PROPERTY_SPEC),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of())));

    // bad case
    assertThat(
        validateQuery("dumdum", Type.NODE_PROPERTY_SPEC),
        equalTo(
            new InputValidationNotes(
                Validity.INVALID,
                getErrorMessage(Grammar.NODE_PROPERTY_SPECIFIER.getFriendlyName(), 6),
                6)));
  }

  @Test
  public void testValidateIpValid() {
    InputValidationNotes notes = validateIp("1.1.1.1");
    assertThat(
        notes, equalTo(new InputValidationNotes(Validity.VALID, Ip.parse("1.1.1.1").toString())));
  }

  @Test
  public void testValidateIpInvalid() {
    InputValidationNotes notes = validateIp("1.1.1.1111");
    assertThat(
        notes,
        equalTo(
            new InputValidationNotes(
                Validity.INVALID, "Invalid IPv4 address: 1.1.1.1111. 1111 is an invalid octet")));
  }

  @Test
  public void testValidatePrefixValid() {
    InputValidationNotes notes = validatePrefix("1.1.1.1/23");
    assertThat(
        notes,
        equalTo(new InputValidationNotes(Validity.VALID, Prefix.parse("1.1.1.1/23").toString())));
  }

  @Test
  public void testValidatePrefixInvalid() {
    InputValidationNotes notes = validatePrefix("1.1.1.1111/23");
    assertThat(
        notes,
        equalTo(
            new InputValidationNotes(
                Validity.INVALID, "Invalid IPv4 address: 1.1.1.1111. 1111 is an invalid octet")));
  }

  @Test
  public void testValidateSourceLocation() {
    CompletionMetadata metadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of(INTERNET_LOCATION.getNodeName(), "node", "trnode"))
            .setLocations(
                ImmutableSet.of(
                    new LocationCompletionMetadata(INTERNET_LOCATION, true),
                    new LocationCompletionMetadata(new InterfaceLocation("node", "iface"), true),
                    new LocationCompletionMetadata(
                        new InterfaceLocation("trnode", "iface"), false, true)))
            .build();

    // shorthand for INTERNET_LOCATION is valid
    {
      InputValidationNotes notes = validateSourceLocation("internet", false, metadata);
      assertEquals(Validity.VALID, notes.getValidity());
    }

    // longhand for INTERNET_LOCATION is invalid
    {
      InputValidationNotes notes = validateSourceLocation("@enter(internet[out])", false, metadata);
      assertEquals(Validity.INVALID, notes.getValidity());
    }

    // exact match is valid
    {
      InputValidationNotes notes = validateSourceLocation("node[iface]", false, metadata);
      assertEquals(Validity.VALID, notes.getValidity());
    }

    // partial match is invalid
    {
      InputValidationNotes notes = validateSourceLocation("node", false, metadata);
      assertEquals(Validity.INVALID, notes.getValidity());
    }

    // trnode does not match
    {
      InputValidationNotes notes = validateSourceLocation("trnode[iface]", false, metadata);
      assertEquals(Validity.INVALID, notes.getValidity());
    }

    // trnode matches
    {
      InputValidationNotes notes = validateSourceLocation("trnode[iface]", true, metadata);
      assertEquals(Validity.VALID, notes.getValidity());
    }
  }

  @Test
  public void testSingleApplicationSpec() {
    // fully matched named application
    assertEquals(Validity.VALID, validateQuery("DNS", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // partially matched named application
    assertEquals(Validity.INVALID, validateQuery("DN", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // matched named application with extra characters
    assertEquals(
        Validity.INVALID, validateQuery("DNS,", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // tcp with port
    assertEquals(
        Validity.VALID, validateQuery("tcp/80", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // tcp with slash
    assertEquals(
        Validity.INVALID, validateQuery("tcp/", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // tcp without slash
    assertEquals(
        Validity.INVALID, validateQuery("tcp", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // icmp with type and code
    assertEquals(
        Validity.VALID, validateQuery("icmp/0/0", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // icmp with type
    assertEquals(
        Validity.INVALID, validateQuery("icmp/0", Type.SINGLE_APPLICATION_SPEC).getValidity());

    // icmp without type
    assertEquals(
        Validity.INVALID, validateQuery("icmp", Type.SINGLE_APPLICATION_SPEC).getValidity());
  }

  @Test
  public void testNodeName() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("n1")).build();

    // node exists --> VALID
    {
      InputValidationNotes result =
          InputValidationUtils.validate(
              Type.NODE_NAME, "n1", completionMetadata, emptyNodeRolesData, emptyReferenceLibrary);
      assertEquals(Validity.VALID, result.getValidity());
    }

    // check is case-insensitive
    {
      InputValidationNotes result =
          InputValidationUtils.validate(
              Type.NODE_NAME, "N1", completionMetadata, emptyNodeRolesData, emptyReferenceLibrary);
      assertEquals(Validity.VALID, result.getValidity());
    }

    // node does not exist --> NO_MATCH
    {
      InputValidationNotes result =
          InputValidationUtils.validate(
              Type.NODE_NAME,
              "asdf",
              completionMetadata,
              emptyNodeRolesData,
              emptyReferenceLibrary);
      assertEquals(Validity.NO_MATCH, result.getValidity());
    }
  }
}
