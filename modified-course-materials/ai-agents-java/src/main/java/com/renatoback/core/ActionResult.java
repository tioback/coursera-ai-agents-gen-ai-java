package com.renatoback.core;

import java.util.HashMap;
import java.util.Map;

public record ActionResult(Object result, String error) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (error == null) {
            map.put("result", result);
        } else {
            map.put("error", error);
        }
        return map;
    }
}
