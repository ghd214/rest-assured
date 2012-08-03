/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.restassured.internal.mapping

import com.jayway.restassured.mapper.ObjectMapper
import com.jayway.restassured.mapper.ObjectMapperDeserializationContext
import com.jayway.restassured.mapper.ObjectMapperSerializationContext
import org.codehaus.jackson.JsonEncoding
import org.codehaus.jackson.JsonGenerator
import org.codehaus.jackson.map.type.TypeFactory
import org.codehaus.jackson.type.JavaType

class JacksonMapper implements ObjectMapper {
    private static ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory()

    def static register(ObjectMapperFactory objectMapperFactory) {
        JacksonMapper.objectMapperFactory = objectMapperFactory
    }

    private org.codehaus.jackson.map.ObjectMapper createJacksonObjectMapper(Class cls, String charset) {
        return JacksonMapper.objectMapperFactory.createJacksonObjectMapper(cls, charset)
    }

    def String serialize(ObjectMapperSerializationContext context) {
        def object = context.getObjectToSerialize()
        JsonEncoding jsonEncoding = getEncoding(context.getCharset())
        def mapper = createJacksonObjectMapper(object.getClass(), context.getCharset())
        def stream = new ByteArrayOutputStream()
        JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(stream, jsonEncoding)
        mapper.writeValue(jsonGenerator, object)
        return stream.toString()
    }

    def Object deserialize(ObjectMapperDeserializationContext context) {
        def object = context.getObjectToDeserializeAs(String.class)
        def cls = context.getType()
        def mapper = createJacksonObjectMapper(cls, context.getCharset())
        JavaType javaType = TypeFactory.type(cls)
        return mapper.readValue(object, javaType)
    }

    private JsonEncoding getEncoding(String charset) {
        def foundEncoding = JsonEncoding.UTF8
        if(charset != null) {
            for (JsonEncoding encoding : JsonEncoding.values()) {
                if (charset.equals(encoding.getJavaName())) {
                    foundEncoding = encoding
                    break
                }
            }
        }
        return foundEncoding
    }
}