See yaml output from a cue config file:

```
cue export --out yaml kitchen-sink.cue schema/sdk.cue schema/common.cue -d '#SDK'
```

Verify a yaml config:

```
cue vet -v kitchen-sink.yaml schema/sdk.cue schema/common.cue -d '#SDK'
```
