package com.elasticsearch.service;

import com.elasticsearch.model.QueryInfo;
import com.elasticsearch.model.Search;

import java.util.List;

public interface ElasticsearchService {
    List<QueryInfo> searchIndex(Search keyword);


}
