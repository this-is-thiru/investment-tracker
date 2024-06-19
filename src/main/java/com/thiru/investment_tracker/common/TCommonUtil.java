package com.thiru.investment_tracker.common;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TCommonUtil {

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
		return stream(source).map(mapper).toList();
	}

	public static <T> List<T> filter(Collection<T> source, Predicate<T> consumer) {
		return stream(source).filter(consumer).toList();
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

	private static <T> Stream<T> stream(Collection<T> list) {
		return list.stream();
	}
}