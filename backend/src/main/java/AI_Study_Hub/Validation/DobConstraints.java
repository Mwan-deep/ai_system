package AI_Study_Hub.Validation;

import jakarta.validation.Payload;

public @interface DobConstraints {

    String message() default "Invalid date of birth , You are not enough age to sign yet!!!";

    int min();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
