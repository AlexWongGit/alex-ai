package org.alex.service;

import org.alex.entity.ArchiveDto;

import java.util.List;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public interface MilvusService {

    Boolean hasCollection(String collectionName);

    void createCollection(String collectionName, Integer featureDim);

    void createPartition(String collectionName, String partitionName);

    void createIndex(String collectionName, String indexName, String metricType);

    Boolean insert(List<ArchiveDto> data);

    void loadCollection(String collectionName);

    void loadPartitions(String collectionName, String partitionsName);

    void releaseCollection(String collectionName);

    void releasePartition(String collectionName, String partitionsName);

    void deleteEntity(String collectionName, String partitionName, String expr);

    String searchSimilarity(byte[] arcsoftFeature, Integer orgId);

}
