
import com.google.common.base.Function;

import java.util.List;

/**
 * interface for making http requests and parsing results
 */
public interface HTTP {
    /**
     * Make a request to a path, use the parser for the result
     *
     * @param path        path
     * @param parser      parser of data
     * @param errResponse result if any error
     * @param name        logger name
     * @param <T>         result type
     *
     * @return result
     */
    List<MesosNode> makeRequest(final String path, String errResponse, String name);
}
