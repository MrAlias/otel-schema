package main

import (
	"fmt"
	"io/ioutil"
	"path/filepath"

	"github.com/xeipuuv/gojsonschema"
	"sigs.k8s.io/yaml"
)

func main() {

	path, err := filepath.Abs("../schema/schema.json")
	if err != nil {
		panic(err.Error())
	}
	jsonSchema := gojsonschema.NewReferenceLoader(fmt.Sprintf("file://%s", path))

	buf, err := ioutil.ReadFile("../../config.yaml")
	if err != nil {
		panic(err.Error())
	}
	buf, err = yaml.YAMLToJSON(buf)
	if err != nil {
		panic(err.Error())
	}
	configuration := gojsonschema.NewBytesLoader(buf)

	result, err := gojsonschema.Validate(jsonSchema, configuration)
	if err != nil {
		panic(err.Error())
	}

	if result.Valid() {
		fmt.Printf("The document is valid\n")
	} else {
		fmt.Printf("The document is not valid. see errors :\n")
		for _, desc := range result.Errors() {
			fmt.Printf("- %s\n", desc)
		}
	}
}
