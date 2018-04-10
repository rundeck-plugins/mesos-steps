import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Use apache http client
 */
public class DefaultHTTP implements HTTP {
    private static final Logger LOG = Logger.getLogger(DefaultHTTP.class);
    private static final String HTTPS = "https";
    public static final String HTTP = "http";
    private static final String USERAGENT = "Rundeck Node Plugin";
    
    private final String mesosProtocol;
    private final String mesosHost;
    private final String mesosPort;
    private final String mesosApiToken;
    private final MetricRegistry metrics;

    public DefaultHTTP(
            final String mesosHost,
            final String mesosPort,
            final String mesosApiToken,
            final MetricRegistry metrics
            ) {
        this.mesosProtocol = HTTP;
        this.mesosHost = mesosHost;
        this.mesosPort = mesosPort;
        this.mesosApiToken = mesosApiToken;
        this.metrics = metrics;
    }

    public String getBaseUrl(final String path) {
        final String baseUrl = format("%s://%s:%s/%s", mesosProtocol, mesosHost, mesosPort, path);
        LOG.info(format("baseUrl is: %s", baseUrl));
        return baseUrl;
    }

    private HttpGet mkGet(final String path) {
        return new HttpGet(getBaseUrl(path));
    }

    @Override
    public List<MesosNode> makeRequest(final String path, String errResult, final String name)
    {
        HttpGet get = mkGet(path);

        if(this.mesosApiToken != null){
            get.setHeader("Authorization", "token=" + this.mesosApiToken);
        }

        final CloseableHttpClient httpclient = getClient();

        LOG.info("HEADERS:");
        for (Header header : get.getAllHeaders()) {
            LOG.info(format("%s: %s", header.getName(), header.getValue()));
        }
        LOG.info(format("GET %s", get.getURI()));
        requestCounter();
        try {
            final CloseableHttpResponse response = httpclient.execute(get);

            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                errorsCounter();
                LOG.warn(format(name + " ended with status code: %d msg: %s", statusCode,
                        EntityUtils.toString(response.getEntity())
                        ));
                return null;
            }

            final String responseBody = EntityUtils.toString(response.getEntity());

            Gson gson = new GsonBuilder().create();
            LOG.info(format("Making Parsing JSON of response: %s", responseBody));
            MesosTasks p = gson.fromJson(responseBody, MesosTasks.class);
            List<MesosNode> mesosNodeList = p.getTasks();
            LOG.info("Mesos node list created");
            return mesosNodeList != null ? mesosNodeList : new ArrayList<MesosNode>();
        } catch (final Exception ex) {
            errorsCounter();
            LOG.error(name + " exception while trying to request mesos API: " + ex.getMessage(), ex);
        }
        return null;
    }

    CloseableHttpClient getClient() {
        return HttpClients.custom().setUserAgent(USERAGENT).build();
    }

    private void errorsCounter() {
        metrics.counter(MetricRegistry.name(Object.class, "http.errors.count")).inc();
    }

    private void requestCounter() {
        metrics.counter(MetricRegistry.name(Object.class, "http.request.count")).inc();
    }

}
