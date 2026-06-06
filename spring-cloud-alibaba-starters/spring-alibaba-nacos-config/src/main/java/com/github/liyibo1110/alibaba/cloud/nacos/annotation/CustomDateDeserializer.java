package com.github.liyibo1110.alibaba.cloud.nacos.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * java.util.Date类的反序列化组件。
 * @author liyibo
 * @date 2026-06-05 13:28
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private static final long serialVersionUID = 1L;

    /** String -> Date */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CustomDateDeserializer() {
        super();
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String date = node.textValue();
        try {
            return dateFormat.parse(date);
        } catch (Exception e) {
            throw new IOException("Invalid date format");
        }
    }
}
