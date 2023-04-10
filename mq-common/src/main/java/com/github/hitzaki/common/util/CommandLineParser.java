package com.github.hitzaki.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CommandLineParser {
    private Map<String, List<String>> options = new HashMap<>();
    private List<String> arguments = new ArrayList<>();

    public CommandLineParser(String[] args) {
        Stream.of(args).forEach(arg -> {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                String key = parts[0];
                String value = parts.length > 1 ? parts[1] : "";
                options.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            } else {
                arguments.add(arg);
            }
        });
    }

    public List<String> getOptionValues(String key) {
        return options.get(key);
    }

    public String getOptionValue(String key) {
        List<String> values = getOptionValues(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new CommandLineParser(args);

        String value = parser.getOptionValue("myoption");
        System.out.println("myoption: " + value);

        List<String> values = parser.getOptionValues("myoption");
        System.out.println("myoption values: " + values);

        List<String> arguments = parser.getArguments();
        System.out.println("arguments: " + arguments);
    }
}
