import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by carlos on 28/02/18.
 */
public class MesosAPI {
    private static final String UTF8 = StandardCharsets.UTF_8.toString();

    private static final Logger LOG = Logger.getLogger(MesosAPI.class);

    private final HTTP http;
    private static final Gson GSON = new Gson();

    public MesosAPI(final HTTP http) {
        this.http = http;
    }

    public List<MesosNode> getNodes(final String marathonContext) {
        final String path = marathonContext != null && !marathonContext.isEmpty() ?
                format("%s/v2/tasks", marathonContext) : "v2/tasks";

        return http.makeRequest(path, "", "getNodes()");
    }
}
