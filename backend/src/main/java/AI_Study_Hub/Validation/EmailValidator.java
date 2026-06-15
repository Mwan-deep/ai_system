package AI_Study_Hub.Validation;

import AI_Study_Hub.Validation.EmailContraints;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

public class EmailValidator implements ConstraintValidator<EmailContraints, String> {
    @Override
    public void initialize(EmailContraints constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isEmpty()){
            return false;
        }

        return value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
