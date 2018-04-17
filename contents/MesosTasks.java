
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlos on 28/02/18.
 */
public class MesosTasks {
    private List<MesosNode> tasks;

    public List<MesosNode> getTasks() {
        return tasks != null ? tasks : new ArrayList<>();
    }

    public void setTasks(List<MesosNode> tasks) {
        this.tasks = tasks;
    }
}
