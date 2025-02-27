package org.alex.service.impl;

import cn.hutool.core.util.PrimitiveArrayUtil;
import com.google.common.collect.Lists;
import io.milvus.Response.SearchResultsWrapper;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetIndexBuildProgressResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.GetIndexBuildProgressParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.LoadPartitionsParam;
import io.milvus.param.partition.ReleasePartitionsParam;
import lombok.extern.slf4j.Slf4j;
import org.alex.constant.FaceArchive;
import org.alex.entity.ArchiveDto;
import org.alex.entity.SearchSimilarityDto;
import org.alex.service.MilvusService;
import org.alex.uitls.MilvusUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
@Service
@Slf4j
public class MilvusServiceImpl implements MilvusService {

    private final MilvusServiceClient milvusServiceClient;

    public MilvusServiceImpl(MilvusServiceClient milvusServiceClient) {
        this.milvusServiceClient = milvusServiceClient;
    }

    @Override
    public Boolean hasCollection(String collectionName) {
        R<Boolean> response = milvusServiceClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        return response.getData();
    }

    @Override
    public String createCollection(String collectionName, Integer featureDim) {
        FieldType archiveId = FieldType.newBuilder()
                .withName(FaceArchive.Field.ARCHIVE_ID)
                .withDescription("主键id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType orgId = FieldType.newBuilder()
                .withName(FaceArchive.Field.ORG_ID)
                .withDescription("组织id")
                .withDataType(DataType.Int32)
                .build();
        FieldType archiveFeature = FieldType.newBuilder()
                .withName(FaceArchive.Field.ARCHIVE_FEATURE)
                .withDescription("档案特征值")
                .withDataType(DataType.FloatVector)
                .withDimension(FaceArchive.FEATURE_DIM)
                .build();
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                .withDescription("档案集合")
                .withShardsNum(FaceArchive.SHARDS_NUM)
                .addFieldType(archiveId)
                .addFieldType(orgId)
                .addFieldType(archiveFeature)
                .build();
        R<RpcStatus> response = milvusServiceClient.createCollection(createCollectionReq);
        return response.getData().getMsg();
    }

    @Override
    public String createPartition(String collectionName, String partitionName) {
        R<RpcStatus> response = milvusServiceClient.createPartition(CreatePartitionParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .build());
        return response.getData().getMsg();
    }

    @Override
    public String createIndex(String collectionName, String indexName, String metricType) {
        R<RpcStatus> response = milvusServiceClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                .withFieldName(FaceArchive.Field.ARCHIVE_FEATURE)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                //nlist 建议值为 4 × sqrt(n)，其中 n 指 segment 最多包含的 entity 条数。
                .withExtraParam("{\"nlist\":16384}")
                .withSyncMode(Boolean.FALSE)
                .build());
        log.info("createIndex-------------------->{}", response.toString());
        R<GetIndexBuildProgressResponse> indexResp = milvusServiceClient.getIndexBuildProgress(
                GetIndexBuildProgressParam.newBuilder()
                        .withCollectionName(FaceArchive.COLLECTION_NAME)
                        .build());
        log.info("getIndexBuildProgress---------------------------->{}", indexResp.toString());
        return response.getData().getMsg();
    }

    @Override
    public Boolean insert(List<ArchiveDto> data) {
        Map<Integer, List<ArchiveDto>> map =
                data.stream().filter(item -> PrimitiveArrayUtil.isNotEmpty(item.getArcsoftFeature())).collect(Collectors.groupingBy(ArchiveDto::getOrgId));
        map.forEach((orgId, list) -> {
            //插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            List<Long> archiveIds = Lists.newArrayList();
            List<Integer> orgIds = Lists.newArrayList();
            List<List<Float>> floatVectors = Lists.newArrayList();
            for (ArchiveDto dto : list) {
                archiveIds.add(dto.getArchiveId());
                orgIds.add(dto.getOrgId());
                //虹软特征值转Float向量
                floatVectors.add(MilvusUtil.arcsoftToFloat(dto.getArcsoftFeature()));
            }
            //档案ID
            fields.add(new InsertParam.Field(FaceArchive.Field.ARCHIVE_ID, DataType.Int64, archiveIds));
            //小区id
            fields.add(new InsertParam.Field(FaceArchive.Field.ORG_ID, DataType.Int32, orgIds));
            //特征值
            fields.add(new InsertParam.Field(FaceArchive.Field.ARCHIVE_FEATURE, DataType.FloatVector, floatVectors));
            //插入
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(FaceArchive.COLLECTION_NAME)
                    .withPartitionName(FaceArchive.getPartitionName(orgId))
                    .withFields(fields)
                    .build();
            R<MutationResult> insert = milvusServiceClient.insert(insertParam);
            log.info("插入:{}", insert);
        });
        return true;
    }

    @Override
    public void loadCollection(String collectionName) {
        R<RpcStatus> response = milvusServiceClient.loadCollection(LoadCollectionParam.newBuilder()
                //集合名称
                .withCollectionName(collectionName)
                .build());
        log.info("loadCollection------------->{}", response);
    }

    @Override
    public void loadPartitions(String collectionName, String partitionsName) {
        R<RpcStatus> response = milvusServiceClient.loadPartitions(
                LoadPartitionsParam
                        .newBuilder()
                        //集合名称
                        .withCollectionName(collectionName)
                        //需要加载的分区名称
                        .withPartitionNames(Lists.newArrayList(partitionsName))
                        .build()
        );
        log.info("loadCollection------------->{}", response);
    }

    @Override
    public void releaseCollection(String collectionName) {
        R<RpcStatus> response = milvusServiceClient.releaseCollection(ReleaseCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        log.info("releaseCollection------------->{}", response);
    }

    @Override
    public void releasePartition(String collectionName, String partitionsName) {
        R<RpcStatus> response = milvusServiceClient.releasePartitions(ReleasePartitionsParam.newBuilder()
                .withCollectionName(collectionName)
                .addPartitionName(partitionsName)
                .build());
        log.info("releasePartition------------->{}", response);
    }

    @Override
    public void deleteEntity(String collectionName, String partitionName, String expr) {
        R<MutationResult> response = milvusServiceClient.delete(
                DeleteParam.newBuilder()
                        //集合名称
                        .withCollectionName(collectionName)
                        //分区名称
                        .withPartitionName(partitionName)
                        //条件 如: id == 1
                        .withExpr(expr)
                        .build()
        );
        log.info("deleteEntity------------->{}", response);
    }

    @Override
    public SearchSimilarityDto searchSimilarity(byte[] arcsoftFeature, Integer orgId) {
        List<Float> arcsoftToFloat = MilvusUtil.arcsoftToFloat(arcsoftFeature);
        List<List<Float>> list = new ArrayList<>();
        list.add(arcsoftToFloat);
        SearchParam.Builder builder = SearchParam.newBuilder()
                //集合名称
                .withCollectionName(FaceArchive.COLLECTION_NAME)
                //计算方式
                // 欧氏距离 (L2)
                // 内积 (IP)
                .withMetricType(MetricType.IP)
                //返回多少条结果
                .withTopK(1)
                //搜索的向量值
                .withVectors(list)
                //搜索的Field
                .withVectorFieldName(FaceArchive.Field.ARCHIVE_FEATURE)
                //https://milvus.io/cn/docs/v2.0.0/performance_faq.md
                .withParams("{\"nprobe\":512}");
        if (orgId != null) {
            //如果只需要搜索某个分区的数据,则需要指定分区
            builder
                    .withExpr(FaceArchive.Field.ORG_ID + " == " + orgId)
                    .withPartitionNames(Lists.newArrayList(FaceArchive.getPartitionName(orgId)));
        }
        R<SearchResults> search = milvusServiceClient.search(builder.build());
        if (search.getData() == null) {
            return null;
        }
        SearchResultsWrapper wrapper = new SearchResultsWrapper(search.getData().getResults());
        for (int i = 0; i < list.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(i);
            if (!scores.isEmpty()) {
                log.info("搜索结果:{}", scores);
                SearchResultsWrapper.IDScore idScore = scores.get(0);
                return new SearchSimilarityDto(idScore.getLongID(), idScore.getScore());
            }
        }
        return null;
    }
}
