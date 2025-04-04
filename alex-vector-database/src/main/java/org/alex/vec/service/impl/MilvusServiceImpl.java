package org.alex.vec.service.impl;

import cn.hutool.core.util.PrimitiveArrayUtil;
import org.alex.common.bean.dto.ArchiveDto;
import org.alex.common.constant.MilvusConstants;
import org.alex.common.utils.VectorUtil;
import org.alex.vec.service.MilvusService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.partition.request.ReleasePartitionsReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author wangzf
 * @Date 2025/2/27
 */
@Service
@Slf4j
public class MilvusServiceImpl implements MilvusService {

    private final MilvusClientV2 client;

    //private final VectorStore vectorStore;


    private final Gson gson = new Gson();

    public MilvusServiceImpl(MilvusClientV2 client
        //,VectorStore vectorStore
    ) {
        this.client = client;
        //this.vectorStore = vectorStore;
    }

    @Override
    public Boolean hasCollection(String collectionName) {
        return client.hasCollection(
                HasCollectionReq.builder()
                        .collectionName(collectionName)
                        .build());
    }

    @Override
    public void createCollection(String collectionName, Integer featureDim) {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_ID)
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .description("主键id").build());
        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.ORG_ID)
                .dataType(DataType.Int32)
                .description("组织id").build());

        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .dataType(DataType.FloatVector)
                .dimension(featureDim)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("sparse")
                .dataType(DataType.SparseFloatVector)
                .build());

        Map<String, Object> analyzerParamsBuiltin = new HashMap<>();
        analyzerParamsBuiltin.put("type", "chinese");
        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.TEXT)
                .dataType(DataType.VarChar)
                .enableAnalyzer(true)
                .analyzerParams(analyzerParamsBuiltin)
                .enableMatch(true)
                .maxLength(65535)
                .build());

        // 自定义分词器
        /*Map<String, Object> analyzerParams = new HashMap<>();
        analyzerParams.put("tokenizer", "standard");
        analyzerParams.put("filter",
                Arrays.asList("lowercase",
                        new HashMap<String, Object>() {{
                            put("type", "length");
                            put("max", 40);
                        }},
                        new HashMap<String, Object>() {{
                            put("type", "stop");
                            put("stop_words", Arrays.asList("a", "an", "for"));
                        }}
                )
        );*/

        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.FILE_NAME)
                .dataType(DataType.VarChar)
                .maxLength(256)
                .build());

        // 定义一个函数，将文本转换为稀疏向量表示
        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25)
                .name("text_bm25_emb")
                .inputFieldNames(Collections.singletonList(MilvusConstants.Field.TEXT))
                .outputFieldNames(Collections.singletonList("sparse"))
                .build());

        Map<String, Object> extraParams = new HashMap<>(1);
        extraParams.put("nlist", 16384);

        List<IndexParam> indexes = new ArrayList<>();
        indexes.add(IndexParam.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                //.extraParams(extraParams)
                .build());
        indexes.add(IndexParam.builder()
                .fieldName("sparse")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                .build());

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionName(MilvusConstants.COLLECTION_NAME)
                .description("档案集合")
                .numShards(MilvusConstants.SHARDS_NUM)
                .primaryFieldName(MilvusConstants.Field.ARCHIVE_ID)
                .vectorFieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexParams(indexes)
                .collectionSchema(schema)
                .build();
        client.createCollection(createCollectionReq);
    }

    @Override
    public void createPartition(String collectionName, String partitionName) {
        client.createPartition(CreatePartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build());
    }

    @Override
    public void createIndex(String collectionName, String indexName) {
        Map<String, Object> extraParams = new HashMap<>(1);
        extraParams.put("nlist", 16384);
        IndexParam indexParam = IndexParam.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                //.extraParams(extraParams)
                .build();

         client.createIndex(CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(Collections.singletonList(indexParam))
                .build());
        log.info("createIndex-------------------->");
    }

    @Override
    public Boolean insert(List<ArchiveDto> data) {
        Map<Integer, List<ArchiveDto>> map =
                data.stream().filter(item -> PrimitiveArrayUtil.isNotEmpty(item.getArcsoftFeature())).collect(Collectors.groupingBy(ArchiveDto::getOrgId));
        map.forEach((orgId, list) -> {
            List<JsonObject> insertDatas = new ArrayList<>();
            for (ArchiveDto dto : list) {
                JsonObject dict = new JsonObject();
                //dict.addProperty(MilvusConstants.Field.ARCHIVE_ID, dto.getArchiveId());
                dict.add(MilvusConstants.Field.ORG_ID, new JsonPrimitive(dto.getOrgId()));
                List<Float> vectors = VectorUtil.arcsoftToFloat(dto.getArcsoftFeature());
                dict.add(MilvusConstants.Field.ARCHIVE_FEATURE, gson.toJsonTree(vectors));
                dict.addProperty(MilvusConstants.Field.TEXT, dto.getText());
                dict.addProperty(MilvusConstants.Field.FILE_NAME, dto.getFileName());
                insertDatas.add(dict);
            }
            InsertReq insertReq = InsertReq.builder()
                    .collectionName(MilvusConstants.COLLECTION_NAME)
                    .partitionName(MilvusConstants.getPartitionName(orgId))
                    .data(insertDatas)
                    .build();
            if (Boolean.FALSE.equals(hasCollection(MilvusConstants.COLLECTION_NAME))) {
                createCollection(MilvusConstants.COLLECTION_NAME, MilvusConstants.FEATURE_DIM);
                createIndex(MilvusConstants.COLLECTION_NAME, MilvusConstants.Field.ARCHIVE_FEATURE);
                loadCollection(MilvusConstants.COLLECTION_NAME);
                createPartition(MilvusConstants.COLLECTION_NAME, MilvusConstants.getPartitionName(orgId));
                loadPartitions(MilvusConstants.COLLECTION_NAME, MilvusConstants.getPartitionName(orgId));
            }
            InsertResp insertResp = client.insert(insertReq);
            log.info("插入:{}", insertResp);
        });
        return true;
    }



    @Override
    public void loadCollection(String collectionName) {
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.loadCollection(loadCollectionReq);
        log.info("loadCollection");
    }

    @Override
    public void loadPartitions(String collectionName, String partitionsName) {
        client.loadPartitions(
                LoadPartitionsReq.builder()
                        .collectionName(collectionName)
                        .partitionNames(Lists.newArrayList(partitionsName))
                        .build());
    }

    @Override
    public void releaseCollection(String collectionName) {
         client.releaseCollection(ReleaseCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        log.info("releaseCollection");
    }

    @Override
    public void releasePartition(String collectionName, String partitionsName) {
        client.releasePartitions(ReleasePartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(Lists.newArrayList(partitionsName))
                .build());
        log.info("releasePartition");
    }

    @Override
    public void deleteEntity(String collectionName, String partitionName, String expr) {
        DeleteResp deleteResp = client.delete(
                DeleteReq.builder()
                        .collectionName(collectionName)
                        .partitionName(partitionName)
                        .filter(expr)
                        .build()
        );
        log.info("deleteEntity------------->{}", deleteResp);
    }

    @Override
    public String searchSimilarity(float[] arcsoftFeature, Integer orgId, String question) {
        //List<Float> arcsoftToFloat = VectorUtil.arcsoftToFloat(arcsoftFeature);
        //BaseVector baseVector = new FloatVec(arcsoftToFloat);
        BaseVector baseVector = new EmbeddedText(question);
        Map<String,Object> searchParams = new HashMap<>();
        searchParams.put("drop_ratio_search", 0.2);
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(MilvusConstants.COLLECTION_NAME)
                .data(Collections.singletonList(baseVector))
                .annsField("sparse")
                .topK(4)
                .searchParams(searchParams)
                // 指定搜索的过滤条件
                //.filter("archive_id>100")
                .metricType(IndexParam.MetricType.BM25)
                // 指定返回的字段
                .outputFields(Collections.singletonList(MilvusConstants.Field.TEXT));

        if (orgId != null) {
            //如果只需要搜索某个分区的数据,则需要指定分区
            builder.partitionNames(Lists.newArrayList(MilvusConstants.getPartitionName(orgId)));
        }
        SearchReq searchReq = builder.build();
        SearchResp searchResp = client.search(searchReq);

        if (searchResp != null
                && searchResp.getSearchResults() != null
                && !searchResp.getSearchResults().get(0).isEmpty()
                && searchResp.getSearchResults().get(0).get(0).getEntity() != null
        ) {
            List<SearchResp.SearchResult> searchResults = searchResp.getSearchResults().get(0);
            HashMap<String, Object> retMap = new HashMap<>();
            for (SearchResp.SearchResult searchResult : searchResults) {
                log.info("搜索结果:{}", searchResult.getEntity());
                Map<String, Object> entity = searchResult.getEntity();
                if (retMap.containsKey(MilvusConstants.Field.TEXT)) {
                    retMap.put(MilvusConstants.Field.TEXT, retMap.get(MilvusConstants.Field.TEXT) + "," + entity.get(MilvusConstants.Field.TEXT));
                } else {
                    retMap.put(MilvusConstants.Field.TEXT, entity.get(MilvusConstants.Field.TEXT));
                }
            }
            return gson.toJson(retMap);
        }
        return null;
    }

/*    @Override
    public List<Document> searchSimilarity(String question) {
        return vectorStore.similaritySearch(question);
    }

    @Override
    public List<Document> searchSimilarity(SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }

    @Override
    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public void delete(List<String> ids) {
        vectorStore.delete(ids);
    }*/
}
