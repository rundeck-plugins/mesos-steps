import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import java.io.IOException;
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
    private final MetricRegistry metrics;

    public DefaultHTTP(
            final String mesosHost,
            final String mesosPort,
            final MetricRegistry metrics
            ) {
        this.mesosProtocol = HTTP;
        this.mesosHost = mesosHost;
        this.mesosPort = mesosPort;
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
        final CloseableHttpClient httpclient = getClient();

        LOG.debug(format("GET %s", get.getURI()));
        requestCounter();
        try (final CloseableHttpResponse response = httpclient.execute(get)) {
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

//            return parser.apply(responseBody);
            Gson gson = new GsonBuilder().create();
            MesosTasks p = gson.fromJson(responseBody, MesosTasks.class);
            return p.getTasks();
        } catch (final IOException ex) {
            errorsCounter();
            LOG.warn(name + " exception while trying to request mesos API: " + ex.getMessage(), ex);
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
