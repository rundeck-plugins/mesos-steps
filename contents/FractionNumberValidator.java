import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;

public class FractionNumberValidator implements PropertyValidator {
    @Override
    public boolean isValid(final String value) throws ValidationException {
        if(value.equals("")) return true;

        try{
            Double.parseDouble(value);
        } catch (NumberFormatException e){
            throw new ValidationException("Must be a decimal value");
        }

        return true;
    }
}
