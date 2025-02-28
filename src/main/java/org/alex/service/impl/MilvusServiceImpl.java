package org.alex.service.impl;

import cn.hutool.core.util.PrimitiveArrayUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.alex.constant.MilvusConstants;
import org.alex.entity.ArchiveDto;
import org.alex.service.MilvusService;
import org.alex.uitls.MilvusUtil;
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

    private final Gson gson = new Gson();

    public MilvusServiceImpl(MilvusClientV2 client) {
        this.client = client;
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
                .fieldName(MilvusConstants.Field.TEXT)
                .dataType(DataType.VarChar)
                .maxLength(65535)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName(MilvusConstants.Field.FILE_NAME)
                .dataType(DataType.VarChar)
                .maxLength(256)
                .build());

        Map<String, Object> extraParams = new HashMap<>(1);
        extraParams.put("nlist", 16384);

        IndexParam indexParam = IndexParam.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.IP)
                .extraParams(extraParams)
                .build();

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionName(MilvusConstants.COLLECTION_NAME)
                .description("档案集合")
                .numShards(MilvusConstants.SHARDS_NUM)
                .primaryFieldName(MilvusConstants.Field.ARCHIVE_ID)
                .vectorFieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexParams(Collections.singletonList(indexParam))
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
    public void createIndex(String collectionName, String indexName, String metricType) {
        Map<String, Object> extraParams = new HashMap<>(1);
        extraParams.put("nlist", 16384);
        IndexParam indexParam = IndexParam.builder()
                .fieldName(MilvusConstants.Field.ARCHIVE_FEATURE)
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.IP)
                .extraParams(extraParams)
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
                List<Float> vectors = MilvusUtil.arcsoftToFloat(dto.getArcsoftFeature());
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
                createIndex(MilvusConstants.COLLECTION_NAME, MilvusConstants.Field.ARCHIVE_FEATURE, IndexParam.MetricType.IP.name());
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
    public String searchSimilarity(byte[] arcsoftFeature, Integer orgId) {
        List<Float> arcsoftToFloat = MilvusUtil.arcsoftToFloat(arcsoftFeature);
        BaseVector baseVector = new FloatVec(arcsoftToFloat);
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(MilvusConstants.COLLECTION_NAME)
                .data(Collections.singletonList(baseVector))
                .topK(4)
                // 指定搜索的过滤条件
                //.filter("archive_id>100")
                .metricType(IndexParam.MetricType.IP)
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
}
