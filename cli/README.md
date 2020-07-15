
# streamctl

## Example Usage

Use help to see all available commands and options:

```shell script
java -jar cli/target/stream-registry-cli-*-shaded.jar --help
```

Delete a domain:

```shell script
java -jar cli/target/stream-registry-cli-*-shaded.jar \
  delete domain \
  --bootstrapServers=kafka:9092 \
  --topic=_streamregistry \
  --schemaRegistryUrl=http://schema-registry \
  --domain=my_domain
```

Delete a streamBinding status:

```shell script
java -jar cli/target/stream-registry-cli-*-shaded.jar \
  delete streamBinding \
  --bootstrapServers=kafka:9092 \
  --topic=_streamregistry \
  --schemaRegistryUrl=http://schema-registry \
  --domain=my_domain \
  --stream=my_stream \
  --version=1 \
  --zone=my_zone \
  --infrastructure=my_infrastructure \
  --statusName=agentStatus
```
