package main

import (
	"github.com/MrAlias/otel-schema/prototype/go/internal/otel"
)

func main() {
	_, err := otel.ParseAndValidateFromConfigFile("../../config.yaml", "../../json_schema/schema/schema.json")
	if err != nil {
		panic(err)
	}
}
