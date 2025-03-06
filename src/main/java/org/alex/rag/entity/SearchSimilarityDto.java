package org.alex.rag.entity;

import lombok.Data;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
@Data
public class SearchSimilarityDto {

    private Long archiveId;
    private Float similarity;

    private String result;

    public SearchSimilarityDto(long longID, float similarity, String result) {
        this.archiveId = longID;
        this.similarity = similarity;
        this.result = result;
    }

    @Override
    public String toString() {
        return "SearchSimilarityDto{" +
                "archiveId=" + archiveId +
                ", similarity=" + similarity +
                '}';
    }
}
