package AI_Study_Hub.Validation;

import com.nimbusds.jose.Payload;

public @interface EmailContraints {
    String message() default "Your email no valid , please check your email again!!!";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
