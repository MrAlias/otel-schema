#!/usr/bin/env python3

import logging
import sys

from opentelemetry.trace import get_tracer

import otel



def main():
    logging.basicConfig(level=logging.DEBUG)
    otel.configure(otel.parse_and_validate_from_config_file(sys.argv[1], sys.argv[2]))

    tracer = get_tracer("config-prototype")

    with tracer.start_as_current_span("operation-a"):
        with tracer.start_as_current_span("operation-a"):
            with tracer.start_as_current_span("operation-a"):
                logging.debug("you should see traces after this line")

main()