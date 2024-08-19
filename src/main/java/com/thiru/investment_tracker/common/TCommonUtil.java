package com.thiru.investment_tracker.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TCommonUtil {

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static <T, R, U> Map<R, U> toMap(Collection<T> source, Function<T, R> keyMapper,
			Function<T, U> valueMapper) {
		return stream(source).collect(Collectors.toMap(keyMapper, valueMapper));
	}

	public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
		return stream(source).map(mapper).toList();
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> consumer) {
		return stream(source).filter(consumer).toList();
	}

	public static <T, R> List<R> applyMap(Collection<T> source, Predicate<T> consumer, Function<T, R> mapper) {
		return stream(source).filter(consumer).map(mapper).toList();
	}

	public static <T> T findFirst(Collection<T> source, Predicate<T> consumer, T defaultValue) {
		return stream(source).filter(consumer).findFirst().orElse(defaultValue);
	}

	public static <T> T findFirst(Collection<T> source, Predicate<T> consumer) {
		return stream(source).filter(consumer).findFirst().orElse(null);
	}

	public static <T, R> void mapAndApply(Collection<T> source, Function<T, R> mapper, Consumer<R> consumer) {
		stream(source).map(mapper).forEach(consumer);
	}

	public static <T> List<T> flatMap(Collection<List<T>> source) {
		return stream(source).flatMap(TCommonUtil::stream).toList();
	}

	public static <T> LongStream mapToLong(List<T> list, ToLongFunction<T> mapper) {
		return stream(list).mapToLong(mapper);
	}

	public static <T> DoubleStream mapToDouble(List<T> list, ToDoubleFunction<T> mapper) {
		return stream(list).mapToDouble(mapper);
	}

	private static <T> Stream<T> stream(Collection<T> list) {
		return list.stream();
	}
}
