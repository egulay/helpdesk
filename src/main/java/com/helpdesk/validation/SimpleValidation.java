// https://medium.com/@jplanes/lambda-validations-with-java-8-86aa8143bd9f
package com.helpdesk.validation;

import java.util.function.Predicate;

import static com.helpdesk.validation.ValidationResult.fail;

public class SimpleValidation<K> implements Validation<K> {

	private final Predicate<K> predicate;
	private final String onErrorMessage;
	
	public static <K> SimpleValidation<K> from(Predicate<K> predicate, String onErrorMessage) {
		 return new SimpleValidation<K>(predicate, onErrorMessage);
	}
	
	private SimpleValidation(Predicate<K> predicate, String onErrorMessage) {
		this.predicate = predicate;
		this.onErrorMessage = onErrorMessage;
	}
	
	@Override
	public ValidationResult test(K param) {
		return predicate.test(param) ? ValidationResult.ok() : fail(onErrorMessage);
	}

}
