package org.alex.vec.service;


import org.alex.common.bean.dto.ArchiveDto;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

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


    /**-------------------spring ai milvus 的api---------------------**/

    List<Document> searchSimilarity(String question);
    List<Document> searchSimilarity(SearchRequest request);

    void add(List<Document> documents);

    void delete(List<String> ids);
}
