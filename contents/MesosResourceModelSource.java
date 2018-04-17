import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import org.apache.log4j.Logger;

import java.util.*;

import static java.lang.String.format;

/**
 * Created by carlos on 27/02/18.
 */
public class MesosResourceModelSource implements ResourceModelSource {
    public static final Logger logger = Logger.getLogger(ResourceModelFactory.class);
    private final String NODE_NAME = "mesosNode";

    private String nodeHostname;
    private String nodeUsername;
    private String sshPass;
    private String sshStoragePath;
    private String extraTags;
    private List<MesosNode> mesosNodeList;

    public MesosResourceModelSource(String nodeHostname, String nodeUsername, String sshPass, String sshStoragePath, String extraTags, List<MesosNode> mesosNodeList) {
        this.nodeHostname = nodeHostname;
        this.nodeUsername = nodeUsername;
        this.sshPass = sshPass;
        this.sshStoragePath = sshStoragePath;
        this.extraTags = extraTags;
        this.mesosNodeList = mesosNodeList;
    }

    @Override
    public INodeSet getNodes() throws ResourceModelSourceException {
        logger.info("Creating a new NodeSet instance...");
        final NodeSetImpl nodeSet = new NodeSetImpl();
        logger.info("Iterating on Mesos Node List...");
        if(this.mesosNodeList == null){
            logger.error("The mesos node list is null");
            throw new ResourceModelSourceException("The mesos node list is null");
        }
        for(MesosNode mesosNode : this.mesosNodeList){
            if(mesosNode != null){
                logger.info("Iterating on port list...");
                for(Integer portNum : mesosNode.getPorts()){
                    nodeSet.putNode(createNodeEntry(mesosNode.getAppId(), mesosNode.getHost(), portNum));
                }
            }
        }
        logger.info("NodeSet created");
        return nodeSet;
    }

    private NodeEntryImpl createNodeEntry(String appId, String host, Integer nodePort){
        logger.info(
                format("Creating a new NodeEntry: Name: %s, Host: %s, UserName: %s",
                        appId, host, nodeUsername));

        final NodeEntryImpl nodeEntry = newNodeTreeImpl();
        nodeEntry.setNodename(host + ":" + nodePort);
        nodeEntry.setTags(createTags(appId));
        nodeEntry.setHostname(host);
        nodeEntry.setUsername(nodeUsername);
        nodeEntry.setAttribute("ssh-authentication", "password");
        nodeEntry.setAttribute("ssh-password-option", this.sshPass);
        nodeEntry.setAttribute("ssh-password-storage-path", this.sshStoragePath);
        nodeEntry.setAttribute("tags", this.extraTags);
        logger.info("NodeEntry created");

        return nodeEntry;
    }

    private Set<String> createTags(String appId){
        Set<String> tags = new HashSet<String>();
        tags.add(appId);

        return tags;
    }

    private NodeEntryImpl newNodeTreeImpl() {
        final NodeEntryImpl result = new NodeEntryImpl();

        if (null == result.getTags()) {
            result.setTags(new LinkedHashSet<>());
        }

        if (null == result.getAttributes()) {
            result.setAttributes(new LinkedHashMap<String, String>());
        }

        return result;
    }
}
