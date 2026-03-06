package com.renatoback.core;

import java.util.Map;

public record Action(String toolName, Map<String, Object> args) {}