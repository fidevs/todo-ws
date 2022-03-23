package com.fidev.todo;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidev.todo.views.TaskDetailsDTO;

class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static byte[] toJson(Object object) throws IOException { // Convert DTO to byte[]
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    static List<TaskDetailsDTO> fromJson(String json) throws IOException { // Convert JSON to List DTO
        return mapper.readValue(json, new TypeReference<List<TaskDetailsDTO>>(){});
    }
}
