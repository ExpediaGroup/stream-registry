
# streamctl

## Warning
This is a low level tool that bypasses Stream Registry model validation and constraints. Use with extreme caution. Breaking the internal state has the potential for prevent all agent and registry processing. Some considerations:

### Triple check what you are deleting
* Check
** Target environemnt (Stream Registry URL)
** Entity key

### Do NOT create orphans
This is a sure way to make state inconsistent. Do not delete parent entities without also deleting their children.

* **Delete a `Schema`:**
  * also delete in order `ConsumerBindings`, `Consumers`, `ProducerBindings`, `Producers`, `StreamBindings`, `Stream`
* **Delete a `Stream`:**
  * also delete in order `ConsumerBindings`, `Consumers`, `ProducerBindings`, `Producers`, `StreamBindings`
  * also delete `Schema` after if no longer needed
* **Delete a `StreamBinding`:**
  * also delete `ConsumerBindings`, `ProducerBindings`
* **Delete a `Consumer`:**
  * also delete `ConsumerBinding`
* **Delete a `Producer`:**
  * also delete `ProducerBinding`
  
**BEWARE OF AGENTS** Agents create bindings from other entities. If you delete a binding, it may be rapdidly recreated by an agent from the parent entity. If this is a problem, delete the parent first, but don't forget to delete the children.

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
