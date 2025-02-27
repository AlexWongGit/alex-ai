package org.alex.entity;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
public class SearchSimilarityDto {

    private Long archiveId;
    private Float similarity;

    public SearchSimilarityDto(long longID, float score) {
        this.archiveId = longID;
        this.similarity = score;
    }

    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return "SearchSimilarityDto{" +
                "archiveId=" + archiveId +
                ", similarity=" + similarity +
                '}';
    }
}
