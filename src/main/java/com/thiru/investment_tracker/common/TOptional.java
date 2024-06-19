package com.thiru.investment_tracker.common;

import java.util.Optional;
import java.util.function.Function;

public class TOptional {

	public static <T> T mapO(T object, T defaultValue) {
		return stream(object).orElse(defaultValue);
	}

	public static <T, U> U map1(T object, Function<T, U> mapper) {
		return stream(object).map(mapper).orElse(null);
	}

	public static <T, U> U map1(T object, Function<T, U> mapper, U defaultValue) {
		return stream(object).map(mapper).orElse(defaultValue);
	}

	private static <T> Optional<T> stream(T object) {
		return Optional.ofNullable(object);
	}
}
