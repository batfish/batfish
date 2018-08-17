#!/usr/bin/env python3
from glob import glob
from json import load, loads
from os import pardir, path, walk
from pytest import fixture, skip
import re

REPO = path.abspath(path.join(path.dirname(__file__), pardir, pardir))
QUESTIONS = glob(REPO + '/questions*/**/*.json', recursive=True)


@fixture(scope='module', params=QUESTIONS)
def question(request):
    yield path.relpath(request.param, REPO)


def test_proper_json(question):
    """Tests that questions are properly formatted."""
    with open(path.join(REPO, question), 'r') as qfile:
        q = load(qfile)  # Throws if the question is not valid JSON.

    assert 'class' in q


def test_name_and_filename_match(question):
    """Tests that all questions have filenames that match their instance name."""
    with open(path.join(REPO, question), 'r') as qfile:
        q = load(qfile)

    assert 'instance' in q
    assert 'instanceName' in q['instance']

    name = q['instance']['instanceName']
    filename = path.splitext(path.basename(question))[0]
    assert name == filename


def test_instance_vars_present(question):
    """Tests that all questions with instance variables use those variables."""
    with open(path.join(REPO, question), 'r') as qfile:
        text = qfile.read()
        q = loads(text)

    assert 'instance' in q
    instance = q['instance']
    for v in instance.get('variables', {}):
        v_pattern = '${' + v + '}'
        assert v_pattern in text


def test_types(question):
    """Tests (partially) that instance variable properties have the correct types."""
    with open(path.join(REPO, question), 'r') as qfile:
        text = qfile.read()
        q = loads(text)

    assert 'instance' in q
    instance = q['instance']
    for name, data in instance.get('variables', {}).items():
        assert 'optional' not in data or isinstance(data['optional'], bool)
        if data.get('type') == 'boolean':
            assert 'value' not in data or isinstance(data['value'], bool)
        elif data.get('type') in ['integer', 'long']:
            assert 'value' not in data or isinstance(data['value'], int)


def test_indented_with_spaces(question):
    """Tests that JSON is indented with spaces, and not tabs."""
    pattern = re.compile(r"\t+")
    with open(path.join(REPO, question), 'r') as qfile:
        for line in qfile:
            if re.search(pattern, line) is not None:
                raise ValueError(
                    "Found tab indentation in question {}. Please run \"sed -i '' 's/\\\\t/    /g' {}\" to switch to spaces.".format(question, path.join(REPO, question)))

if __name__ == '__main__':
    pytest.main()

