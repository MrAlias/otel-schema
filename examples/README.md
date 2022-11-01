# Usage example options

## Specify an environment variable

Users could specify an environment variable to set the configuration file. This would allow implementations to decide on the most appropriate way to use this configuration file. Some possible names for this variable:

* `OTEL_CONFIGURATION_FILE`
* `OTEL_CONFIG_FILE`
* `OTEL_CONF`

## Provide a configurator interface

Languages could implement a common configurator interface, the implementation of which could parse the configuration file. This would require the definition of this interface in the specification, and implementations to adhere to this change. An example configurator may look like this:

```python
from opentelemetry.configuration import Configurator

conf = Configurator(configuration_file="./config.yaml")
```

## .Net

## C++

## Go

## Java

The Java implementation provides a JAR that supports configuring various parameters via system properties. This implementation could leverage a configuration file by supporting its configuration a system property:

```bash
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -Dotel.config=./config.yaml
     -jar myapp.jar
```

## Javascript

## Python

The Python implementation has a command available that allows users to leverage auto-instrumentation. The `opentelemetry-instrument` command could use a `--config` flag to pass in a config file:

```bash
# install the instrumentation package
pip install opentelemetry-instrumentation
# use a --config parameter to pass in the configuration file
# NOTE: this parameter does not currently exist and would need to be added
opentelemetry-instrument --config ./config.yaml ./python/app.py
```

## Ruby
