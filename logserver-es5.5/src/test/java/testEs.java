import com.inforefiner.cloud.log.utils.FileUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
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
//        Settings settings = Settings.builder()
//                .put("cluster.name", "elasticsearch").build();
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
//                .put("client.transport.sniff", false)
                .put("xpack.security.user", "elastic:changeme")
                .build();
        client =
                new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("info5"), 9303));

    }

    @Test
    public void testCreate() {
        String mapping = FileUtil.loadFromClassPath("/sync_task_log.mapping");
        client.admin().indices().prepareCreate("sync_task_log1").addMapping("sync_task_log", mapping).get();
    }

    @Test
    public void testSearch(){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("taskId", "cf1642e6-7f80-4aef-b7a6-5a4e238c8840"));
        boolQuery.must(QueryBuilders.termQuery("logType", 0));
        SearchResponse response =
                client.prepareSearch("sync_task_log").setTypes("sync_task_log").setQuery(boolQuery).setFrom(0).setSize(1000).addSort("logTime", SortOrder.ASC).get();
        Map<String, Object> ret = new HashMap();
        List<String> logs = new ArrayList();
        SearchHit[] hits = response.getHits().getHits();
        for(SearchHit hit: hits){
            System.out.println(hit.getSource().get("logText"));
        }
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
