package com.bricklink.api.filter;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class FiltersTest {
    private Filters filters;

    @Test
    void builder() {
        Filters filters = Filters.builder()
                                 .lte("value", 17.0)
                                 .build();
        assertThat(filters).isNotNull();

        List<TestObject> testObjects = List.of(
                TestObject.builder()
                          .name("ABC")
                          .value(15.0)
                          .build(),
                TestObject.builder()
                          .name("CDE")
                          .value(1.0)
                          .build(),
                TestObject.builder()
                          .name("XYZ")
                          .value(22.0)
                          .build(),
                TestObject.builder()
                          .name("JKL")
                          .value(11.0)
                          .build(),
                TestObject.builder()
                          .name("MNO")
                          .value(33.0)
                          .build(),
                TestObject.builder()
                          .name("ZZZ")
                          .value(17.0)
                          .build()
        );

        List<TestObject> filteredTestObjects = testObjects.stream()
                                                          .filter(filters)
                                                          .toList();
        filteredTestObjects.forEach(to -> {
            log.info("{}", to);
        });
    }

    @SuperBuilder
    @Data
    private static class TestObject {
        private final String name;
        private final Double value;
    }
}