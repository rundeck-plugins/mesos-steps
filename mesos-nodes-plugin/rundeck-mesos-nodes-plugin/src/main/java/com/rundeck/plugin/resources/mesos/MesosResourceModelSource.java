package com.rundeck.plugin.resources.mesos;

import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.rundeck.plugin.resources.mesos.client.MesosNode;

import java.util.*;

/**
 * Created by carlos on 27/02/18.
 */
public class MesosResourceModelSource implements ResourceModelSource {
    private final String NODE_NAME = "mesosNode";

    private String nodeHostname;
    private String nodeUsername;
    private List<MesosNode> mesosNodeList;

    public MesosResourceModelSource(String nodeHostname, String nodeUsername, List<MesosNode> mesosNodeList) {
        this.nodeHostname = nodeHostname;
        this.nodeUsername = nodeUsername;
        this.mesosNodeList = mesosNodeList;
    }

    @Override
    public INodeSet getNodes() throws ResourceModelSourceException {
        // create a new instance
        final NodeSetImpl nodeSet = new NodeSetImpl();
        for(MesosNode mesosNode : this.mesosNodeList){
            for(Integer portNum : mesosNode.getPorts()){
                nodeSet.putNode(createNodeEntry(mesosNode.getAppId(), portNum));
            }
        }
        return nodeSet;
    }

    private NodeEntryImpl createNodeEntry(String appId, Integer nodePort){
        final NodeEntryImpl nodeEntry = newNodeTreeImpl();
        nodeEntry.setNodename(NODE_NAME + "_" + nodePort);
        nodeEntry.setTags(createTags(appId));
        nodeEntry.setHostname(nodeHostname + ":" + nodePort);
        nodeEntry.setUsername(nodeUsername);

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
