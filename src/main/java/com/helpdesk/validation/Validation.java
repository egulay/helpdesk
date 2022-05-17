package com.helpdesk.validation;

import lombok.val;

@FunctionalInterface
public interface Validation<K> {

    ValidationResult test(K param);

    default Validation<K> and(Validation<K> other) {
        return (param) -> {
            val firstResult = this.test(param);
            return !firstResult.isValid() ? firstResult : other.test(param);
        };
    }

    default Validation<K> or(Validation<K> other) {
        return (param) -> {
            val firstResult = this.test(param);
            return firstResult.isValid() ? firstResult : other.test(param);
        };
    }

}
