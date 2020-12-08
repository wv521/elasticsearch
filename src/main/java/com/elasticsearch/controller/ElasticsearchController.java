package com.elasticsearch.controller;


import com.elasticsearch.model.QueryInfo;
import com.elasticsearch.model.Search;
import com.elasticsearch.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ElasticsearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @RequestMapping("/index")
    public String toIndex(){
        return "/index";
    }

    @RequestMapping("/searchIndex")
    public String searchIndex(Search s, HttpServletRequest httpServletRequest){
        List<QueryInfo> result = elasticsearchService.searchIndex(s);
        httpServletRequest.getSession().setAttribute("result",result);
        return "data";
    }
}
