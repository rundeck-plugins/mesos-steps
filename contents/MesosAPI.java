import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<MesosNode> getNodes(final String userQuery) {
        final String path = "v2/tasks";
        return http.makeRequest(path, "", "getNodes()");
//        return new ArrayList();
    }
}
