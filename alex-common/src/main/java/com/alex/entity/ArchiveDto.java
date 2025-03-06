package com.alex.entity;

import lombok.Data;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/2/27
 */
@Data
public class ArchiveDto {

    private Integer orgId;

    private String archiveId;
    private float[] arcsoftFeature;

    private String text;
    private String fileName;
}
