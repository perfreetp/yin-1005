package com.digitaltwin.pipeline.common;

import lombok.Data;

@Data
public class PageQuery {

    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String orderBy;
    private String orderDir = "asc";
}
