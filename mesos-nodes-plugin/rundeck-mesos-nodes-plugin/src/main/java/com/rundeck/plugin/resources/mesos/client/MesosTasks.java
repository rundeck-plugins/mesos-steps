package com.rundeck.plugin.resources.mesos.client;

import java.util.List;

/**
 * Created by carlos on 28/02/18.
 */
public class MesosTasks {
    List<MesosNode> tasks;

    public List<MesosNode> getTasks() {
        return tasks;
    }

    public void setTasks(List<MesosNode> tasks) {
        this.tasks = tasks;
    }
}
