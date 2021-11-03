package com.example.cybs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @Description Json序列化、反序列化工具类
 * @Author Kim
 * @Date 2021/8/25 14:48
 */
@Slf4j
@Component
public class JsonUtil {

    private static ObjectMapper mapper;

    public static String objectToString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("json objectToString occur error {} ", e);
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T stringToObject(String json, Class<T> obj) {
        try {
            return mapper.readValue(json,obj);
        } catch (JsonProcessingException e) {
            log.error("json stringToObject occur error {} ", e);
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T stringToObject(String jsonStr, TypeReference<T> type) {
        if(!StringUtils.hasLength(jsonStr)) {
            return null;
        }
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        try {
            return mapper.readValue(jsonStr, type);
        } catch (IOException e) {
            log.error("json stringToObject occur error {} ", e);
            return null;
        }
    }

    @Autowired
    public void setMapper(ObjectMapper mapper) {
        JsonUtil.mapper = mapper;
    }
}
