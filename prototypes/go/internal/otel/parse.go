package otel

import (
	"fmt"
	"io/ioutil"
	"path/filepath"

	"github.com/xeipuuv/gojsonschema"
	"sigs.k8s.io/yaml"
)

type Config struct{}

var NoOpConfig = Config{}

func ParseAndValidateFromConfigFile(filename string, schema string) (Config, error) {
	path, err := filepath.Abs(schema)
	if err != nil {
		return NoOpConfig, err
	}
	jsonSchema := gojsonschema.NewReferenceLoader(fmt.Sprintf("file://%s", path))

	buf, err := ioutil.ReadFile(filename)
	if err != nil {
		return NoOpConfig, err
	}
	buf, err = yaml.YAMLToJSON(buf)
	if err != nil {
		return NoOpConfig, err
	}
	configuration := gojsonschema.NewBytesLoader(buf)

	result, err := gojsonschema.Validate(jsonSchema, configuration)
	if err != nil {
		return NoOpConfig, err
	}

	if result.Valid() {
		fmt.Printf("The document is valid\n")
	} else {
		fmt.Printf("The document is not valid. see errors :\n")
		for _, desc := range result.Errors() {
			fmt.Printf("- %s\n", desc)
		}
	}
	return Config{}, nil
}
