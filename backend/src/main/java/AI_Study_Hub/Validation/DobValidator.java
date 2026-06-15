package AI_Study_Hub.Validation;

import AI_Study_Hub.Validation.DobConstraints;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraints, LocalDate> {
    private int min;
    @Override
    public void initialize(DobConstraints constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if(Objects.isNull(value)){
            return true;
        }
        long year = ChronoUnit.YEARS.between(LocalDate.now() , value);
        return year >= min;
    }
}
