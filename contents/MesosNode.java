
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlos on 28/02/18.
 */
public class MesosNode {
    private List<Integer> ports;
    private String host;
    private String appId;

    public List<Integer> getPorts() {
        return ports != null ? ports : new ArrayList<>();
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
