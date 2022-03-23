# Updating Python requirements

You must have the `pip-compile` command available in order to update requirements.
`pip-compile` is available in the `pip-tools` package from PyPi.
The minimum supported version is `6.2.0`.
To install, run:
```
pip install pip-tools
```

The file `requirements.txt` is a "lockfile" that is automatically generated
from `requirements.in`. To add a new requirement or to change the version of an
existing requirement, you must edit `requirements.in`, then re-run `pip-compile`:

```
pip-compile --generate-hashes --allow-unsafe requirements.in --output-file requirements.txt
```
