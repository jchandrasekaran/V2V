package model.collectedsample;

import java.util.Arrays;

import model.CustomDateFormatter;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class CollectedSampleBackingFormValidator implements Validator {

  private Validator validator;

  public CollectedSampleBackingFormValidator(Validator validator) {
    super();
    this.validator = validator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return Arrays.asList(FindCollectedSampleBackingForm.class, CollectedSampleBackingForm.class, CollectedSample.class).contains(clazz);
  }

  @Override
  public void validate(Object obj, Errors errors) {
    if (obj == null || validator == null)
      return;
    ValidationUtils.invokeValidator(validator, obj, errors);
    CollectedSampleBackingForm form = (CollectedSampleBackingForm) obj;
    String collectedOn = form.getCollectedOn();
    if (!CustomDateFormatter.isDateStringValid(collectedOn))
      errors.rejectValue("collectedSample.collectedOn", "dateFormat.incorrect",
          CustomDateFormatter.getErrorMessage());
  }
}
