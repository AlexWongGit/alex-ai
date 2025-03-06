package com.alex.vec.service;


import com.alex.entity.ArchiveDto;

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

    void createIndex(String collectionName, String indexName);

    Boolean insert(List<ArchiveDto> data);

    void loadCollection(String collectionName);

    void loadPartitions(String collectionName, String partitionsName);

    void releaseCollection(String collectionName);

    void releasePartition(String collectionName, String partitionsName);

    void deleteEntity(String collectionName, String partitionName, String expr);

    String searchSimilarity(float[] arcsoftFeature, Integer orgId, String question);

}
