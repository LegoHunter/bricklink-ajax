package com.bricklink.api.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class Filters implements Predicate<Object> {
    private final List<FieldPredicate> predicates;

    @Override
    public boolean test(Object o) {
        final AtomicBoolean test = new AtomicBoolean(true);
        predicates.forEach(p -> {
            test.compareAndSet(true, p.test(o));
        });
        return test.get();
    }

    public static FiltersBuilder builder() {
        return new FiltersBuilder();
    }

    public static class FiltersBuilder {
        private final List<FieldPredicate> predicates = new ArrayList<>();

        public FiltersBuilder lte(String fieldName, Double max) {
            predicates.add(new LTE(fieldName, max));
            return this;
        }

        public Filters build() {
            return new Filters(predicates);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    private static abstract class FieldPredicate implements Predicate<Object> {
        private final String fieldName;

        public Object value(Object object) {
            Object v = null;
            Optional<Method> method = Arrays.stream(object.getClass()
                                                          .getMethods())
                                            .filter(m -> m.getName()
                                                          .toLowerCase()
                                                          .equals("get" + fieldName.toLowerCase()))
                                            .findFirst();
            return method.map(m -> {
                             try {
                                 return m.invoke(object);
                             } catch (Exception e) {
                                 log.error("{}", e.getMessage(), e);
                             }
                             return v;
                         })
                         .orElse(v);
        }
    }

    private static class LTE extends FieldPredicate {
        private final Double max;

        public LTE(String fieldName, Double max) {
            super(fieldName);
            this.max = max;
        }

        @Override
        public boolean test(Object object) {
            return Double.parseDouble(String.valueOf(value(object))) <= max;
        }
    }
}
