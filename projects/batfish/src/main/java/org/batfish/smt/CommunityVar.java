package org.batfish.smt;


/**
 * <p>Representation of a community variable for the symbolic encoding.
 * Configuration languages allow users match community values using
 * either <b>exact matches</b> or <b>regular expression</b> matches.
 * For example, a regular expression match such as .*:65001 will
 * match any community string that ends with 65001.</p>
 *
 *><p>To encode community semantics, the model introduces a single
 * new boolean variable for every exact match, and two new boolean
 * variables for every regex match. The first variable says whether
 * there is a community value that matches the regex, but is not
 * specified in the configuration (e.g., came from a neighbor).
 * The second variable says if the regex match is successful, which
 * is based on both the communities in the configuration as well
 * as other communities possibly sent by neighbors.</p>
 *
 * @author Ryan Beckett
 */
class CommunityVar {

    enum Type {
        EXACT, REGEX, OTHER
    }

    private Type _type;

    private String _value;

    private Long _long;

    public CommunityVar(Type type, String value, Long l) {
        _type = type;
        _value = value;
        _long = l;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CommunityVar that = (CommunityVar) o;

        if (_type != that._type)
            return false;
        if (_value != null ? !_value.equals(that._value) : that._value != null)
            return false;
        return _long != null ? _long.equals(that._long) : that._long == null;
    }

    @Override
    public int hashCode() {
        int result = _type != null ? _type.ordinal() : 0;
        result = 31 * result + (_value != null ? _value.hashCode() : 0);
        result = 31 * result + (_long != null ? _long.hashCode() : 0);
        return result;
    }

    Type getType() {
        return _type;
    }

    String getValue() {
        return _value;
    }

    Long asLong() {
        return _long;
    }
}