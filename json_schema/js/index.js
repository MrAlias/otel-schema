'use strict';

const opentelemetry = require('@opentelemetry/api');
const fs = require('fs');
const path = require('path');
const Configuration = require('./configuration.js')

const schema = JSON.parse(fs.readFileSync(path.join(__dirname, '../schema/schema.json')));
const configData = JSON.parse(fs.readFileSync(path.join(__dirname, './config.json')));

const config = new Configuration(schema);
config.validate(configData);
config.apply(configData)
  .then(test);

// create span to test
function test() {
  const tracer = opentelemetry.trace.getTracer('example-tracer');
  const span1 = tracer.startSpan('span1');
  span1.end();
}
