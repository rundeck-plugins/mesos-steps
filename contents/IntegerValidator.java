import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;

public class IntegerValidator implements PropertyValidator {
    @Override
    public boolean isValid(final String value) throws ValidationException {
        if(value.equals("") || value.startsWith("$")) return true;

        try{
            Integer.parseInt(value);
        } catch (NumberFormatException e){
            throw new ValidationException("Must be a integer value");
        }

        return true;
    }
}
