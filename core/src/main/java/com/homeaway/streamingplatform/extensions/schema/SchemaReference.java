package com.homeaway.streamingplatform.extensions.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchemaReference {

    private String subject;
    private long id;
    private long version;

}
