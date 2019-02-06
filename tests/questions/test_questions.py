#!/usr/bin/env python3
import re
from glob import glob
from json import load
from os import pardir, path

import pytest

REPO = path.abspath(path.join(path.dirname(__file__), pardir, pardir))
QUESTIONS = glob(REPO + '/questions*/**/*.json', recursive=True)

CAMEL_CASE_PATTERN = re.compile(r'^[a-z][a-z0-9]*([A-Z][a-z0-9]+)*$')

@pytest.fixture(scope='module', params=QUESTIONS)
def question_path(request):
    yield path.relpath(request.param, REPO)


@pytest.fixture(scope='module')
def question_text(question_path):
    with open(path.join(REPO, question_path), 'r') as qfile:
        return qfile.read()


@pytest.fixture(scope='module')
def question(question_path):
    with open(path.join(REPO, question_path), 'r') as qfile:
        q = load(qfile)  # Throws if the question is not valid JSON.

    assert 'class' in q
    assert 'instance' in q
    return q


def test_name_and_filename_match(question_path, question):
    """Tests that all questions have filenames that match their instance name."""
    assert 'instanceName' in question['instance']
    name = question['instance']['instanceName']
    filename = path.splitext(path.basename(question_path))[0]
    assert name == filename


def test_instance_vars_present(question, question_text):
    """Tests that all questions with instance variables use those variables."""
    instance = question['instance']
    for v in instance.get('variables', {}):
        v_pattern = '${' + v + '}'
        assert v_pattern in question_text


def test_instance_vars_have_valid_names(question):
    """Tests that variable names are conformant."""
    instance = question['instance']
    for name in instance.get('variables', {}).keys():
        assert CAMEL_CASE_PATTERN.match(name), 'variable {} not slouchingCamelCase'.format(name)


def test_instance_vars_have_valid_display_names(question):
    """Tests that variables have display names."""
    instance = question['instance']
    for name, var in instance.get('variables', {}).items():
        assert 'displayName' in var, 'variable {} missing displayName'.format(name)
        display_name = var['displayName']
        assert display_name != name, 'variable {} has eponymous displayName'.format(name)


def test_instance_vars_with_values(question):
    """Tests that variables with allowed values have descriptions."""
    whitelist = {
        ('edges', 'edgeType'),
        ('neighbors', 'neighborTypes'),
        ('neighbors', 'style'),
        ('routes', 'rib'),
    }
    instance = question['instance']
    qname = instance['instanceName']
    for name, var in instance.get('variables', {}).items():
        assert 'allowedValues' not in var, 'variable {} should migrate to values'.format(name)
        if (qname, name) in whitelist:
            # Whitelisted, skip check that description is present
            continue

        for value in var.get('values', []):
            assert 'description' in value, 'add description to {} or whitelist it'.format(name)


def test_types(question):
    """Tests (partially) that instance variable properties have the correct types."""
    instance = question['instance']
    for name, data in instance.get('variables', {}).items():
        assert 'optional' not in data or isinstance(data['optional'], bool)
        if data.get('type') == 'boolean':
            assert 'value' not in data or isinstance(data['value'], bool)
        elif data.get('type') in ['integer', 'long']:
            assert 'value' not in data or isinstance(data['value'], int)


NO_ORDERED_VARIABLE_NAMES_QUESTIONS = {
    'questions/stable/definedStructures.json',
    'questions/stable/edges.json',
    'questions/stable/ipsecSessionStatus.json',
    'questions/stable/referencedStructures.json',
    'questions/stable/routes.json',
    'questions/stable/testFilters.json',
    'questions/experimental/bgpPeerConfiguration.json',
    'questions/experimental/bgpProcessConfiguration.json',
    'questions/experimental/bgpSessionCompatibility.json',
    'questions/experimental/bgpSessionStatus.json',
    'questions/experimental/bidirectionalTraceroute.json',
    'questions/experimental/differentialReachability.json',
    'questions/experimental/filterLineReachability.json',
    'questions/experimental/filterTable.json',
    'questions/experimental/interfaceMtu.json',
    'questions/experimental/interfaceProperties.json',
    'questions/experimental/mlagProperties.json',
    'questions/experimental/multipathConsistency.json',
    'questions/experimental/namedStructures.json',
    'questions/experimental/neighbors.json',
    'questions/experimental/nodeProperties.json',
    'questions/experimental/nodes.json',
    'questions/experimental/ospfProperties.json',
    'questions/experimental/prefixTracer.json',
    'questions/experimental/resolveFilterSpecifier.json',
    'questions/experimental/resolveInterfaceSpecifier.json',
    'questions/experimental/searchFilters.json',
    'questions/experimental/switchedVlanProperties.json',
    'questions/experimental/vxlanVniProperties.json',
}

def test_ordered_variable_names_is_valid(question, question_path):
    """Tests that if orderedVariableNames is present, it includes all instance variables."""
    instance = question['instance']
    ordered_variable_names = instance.get('orderedVariableNames', [])
    set_of_variable_names = frozenset(instance.get('variables', {}).keys())

    if not ordered_variable_names:
        if len(set_of_variable_names) <= 1:
            assert question_path not in NO_ORDERED_VARIABLE_NAMES_QUESTIONS
            return
        assert question_path in NO_ORDERED_VARIABLE_NAMES_QUESTIONS
        pytest.skip('Whitelisted question with no orderedVariableNames')

    assert question_path not in NO_ORDERED_VARIABLE_NAMES_QUESTIONS
    set_of_ordered_variable_names = frozenset(ordered_variable_names)
    # ordered variable names should not contain duplicates
    assert len(set_of_ordered_variable_names) == len(ordered_variable_names)
    assert set_of_ordered_variable_names == set_of_variable_names


def test_indented_with_spaces(question_text, question_path):
    """Tests that JSON is indented with spaces, and not tabs."""
    if '\t' in question_text:
        raise ValueError(
            "Found tab indentation in question {}. Please run \"sed -i '' 's/\\\\t/    /g' {}\" to switch to spaces.".format(
                    question_path, path.join(REPO, question_path)))


if __name__ == '__main__':
    pytest.main()
