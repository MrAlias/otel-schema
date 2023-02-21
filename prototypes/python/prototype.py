#!/usr/bin/env python3

import logging
import sys

from opentelemetry.trace import get_tracer
from opentelemetry.metrics import get_meter

import otel


def main():
    logging.basicConfig(level=logging.DEBUG)
    otel.configure(otel.parse_and_validate_from_config_file(sys.argv[1], sys.argv[2]))

    tracer = get_tracer("config-prototype")
    meter = get_meter("config-prototype")

    counter = meter.create_counter("work", unit="1")

    with tracer.start_as_current_span("operation-a"):
        with tracer.start_as_current_span("operation-b"):
            with tracer.start_as_current_span("operation-c"):
                logging.debug("you should see telemetry after this line")
                counter.add(1)
    


if __name__ == "__main__":
    main()