package org.batfish.referencelibrary;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IpWildcard;
import org.junit.jupiter.api.Test;

/** Tests for {@link ReferenceBook} */
class ReferenceBookTest {

  /** check that we deserialize a basic object correctly */
  @Test
  void testBookDeserializationBasic() throws IOException {
    ReferenceBook book =
        BatfishObjectMapper.mapper()
            .readValue(
                readResource("org/batfish/referencelibrary/bookBasic.json", UTF_8),
                ReferenceBook.class);

    assertThat(book.getAddressGroups(), hasSize(2));
    assertThat(book.getFilterGroups(), hasSize(2));
    assertThat(book.getInterfaceGroups(), hasSize(2));
    assertThat(book.getServiceEndpoints(), hasSize(2));
    assertThat(book.getServiceObjects(), hasSize(2));
    assertThat(book.getServiceObjectGroups(), hasSize(2));
  }

  /** check that we throw an error for duplicate address groups */
  @Test
  void testBookDeserializationDupAddressGroup() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource(
                            "org/batfish/referencelibrary/bookDupAddressGroup.json", UTF_8),
                        ReferenceBook.class));
    assertThat(exception.getMessage(), containsString("Duplicate"));
  }

  /** check that we throw an error for a non-existent child address group name */
  @Test
  void testBookUndefChildAddressGroup() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource(
                            "org/batfish/referencelibrary/bookUndefChildAddressGroup.json", UTF_8),
                        ReferenceBook.class));
    assertThat(
        exception.getMessage(),
        containsString("Following child address group names are not defined: [ag3]"));
  }

  /** check that we throw an error when the same name is used in a service object and group */
  @Test
  void testBookDeserializationDupServiceName() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource("org/batfish/referencelibrary/bookDupServiceName.json", UTF_8),
                        ReferenceBook.class));
    assertThat(exception.getMessage(), containsString("Duplicate"));
  }

  /** check that we throw an error for undefined address groups */
  @Test
  void testBookDeserializationUndefAddressGroup() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource(
                            "org/batfish/referencelibrary/bookUndefAddressGroup.json", UTF_8),
                        ReferenceBook.class));
    assertThat(exception.getMessage(), containsString("Undefined"));
  }

  /** check that we throw an error for undefined service name */
  @Test
  void testBookDeserializationUndefServiceName() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource(
                            "org/batfish/referencelibrary/bookUndefServiceName.json", UTF_8),
                        ReferenceBook.class));
    assertThat(exception.getMessage(), containsString("Undefined"));
  }

  @Test
  void testAddGroupAndDescendantNamesNonExistentGroup() {
    assertThrows(
        NoSuchElementException.class,
        () -> {
          ReferenceBook book = ReferenceBook.builder("book").build();
          Set<String> groupNames = new HashSet<>();
          book.addGroupAndDescendantNames("a", groupNames);
        });
  }

  @Test
  void testAddGroupAndDescendantNamesCycles() {
    // a -> b -> a
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup("a", null, ImmutableSortedSet.of("b")),
                    new AddressGroup("b", null, ImmutableSortedSet.of("a"))))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a", "b")));
  }

  @Test
  void testAddGroupAndDescendantNamesNoChildren() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(ImmutableList.of(new AddressGroup("a", null, null)))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a")));
  }

  @Test
  void testAddGroupAndDescendantNamesDeepChildren() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup("a", null, ImmutableSortedSet.of("b", "c")),
                    new AddressGroup("b", null, ImmutableSortedSet.of("d")),
                    new AddressGroup("c", null, null),
                    new AddressGroup("d", null, null)))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a", "b", "c", "d")));
  }

  @Test
  void testGetAddressesRecursive() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(
                        "a", ImmutableSortedSet.of("1.1.1.1"), ImmutableSortedSet.of("b", "c")),
                    new AddressGroup("b", ImmutableSortedSet.of("2.2.2.2"), null),
                    new AddressGroup("c", null, null)))
            .build();
    // should include addresses from a and b; no addresses in c shouldn't matter
    assertThat(
        book.getAddressesRecursive("a"),
        equalTo(ImmutableSet.of(IpWildcard.parse("1.1.1.1"), IpWildcard.parse("2.2.2.2"))));
  }

  @Test
  void testJavaSerialization() {
    ReferenceBook book = ReferenceBook.builder("book").build();
    assertThat(SerializationUtils.clone(book), equalTo(book));
  }
}
