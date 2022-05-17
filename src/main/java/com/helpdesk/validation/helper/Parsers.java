package com.helpdesk.validation.helper;

import lombok.val;

import java.util.Objects;
import java.util.regex.Pattern;

public class Parsers {
    public static boolean notValidUUID(String uuid) {
        if (Objects.isNull(uuid)) {
            return false;
        }

        return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    public static boolean notValidEmailAddress(String emailAddress) {
        if (Objects.isNull(emailAddress)) {
            return false;
        }

        val regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        return Pattern.compile(regex).matcher(emailAddress).matches();
    }
}
