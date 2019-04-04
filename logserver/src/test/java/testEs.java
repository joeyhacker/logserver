import com.inforefiner.cloud.log.utils.FileUtil;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class testEs {

    TransportClient client;

    String index = "test_index";
    String indexType = "test_index_type";

    @Before
    public void init() throws Throwable {
        Settings settings = Settings.builder()
                .put("cluster.name", "es5").build();
        client =
                new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.2.196"), 9300));

    }

    @Test
    public void testCreate() {
        String mapping = FileUtil.loadFromClassPath("/sync_task_log.mapping");
        client.admin().indices().prepareCreate("sync_task_log1").addMapping("sync_task_log", mapping).get();
    }

    @Test
    public void testSave() {
        List list = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Map m = new HashMap();
            m.put("id", i);
            m.put("name", "name#" + i);
            list.add(m);
        }
        client.prepareIndex("sync_task_log1", "sync_task_log").setSource(list.toArray()).get();
    }

    @After
    public void close() {
        client.close();
    }
}
