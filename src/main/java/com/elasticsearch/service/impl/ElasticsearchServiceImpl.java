package com.elasticsearch.service.impl;

import com.elasticsearch.model.QueryInfo;
import com.elasticsearch.model.Search;
import com.elasticsearch.service.ElasticsearchService;
import com.elasticsearch.util.ElasticsearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;


    @Override
    public List<QueryInfo> searchIndex(Search s) {
        List<QueryInfo> result = new ArrayList<>();
        try {
            result = elasticsearchUtil.search("content", s.getKeyword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
