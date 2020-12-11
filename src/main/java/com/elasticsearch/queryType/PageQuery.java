package com.elasticsearch.queryType;

import com.elasticsearch.model.QueryInfo;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class PageQuery {

    public List<QueryInfo> searchIndex(String field, String keyword, int pageNum, int size) {
        List<QueryInfo> list = new ArrayList<>();

        IKAnalyzer ikAnalyzer = new IKAnalyzer();
        // 创建查询
        Query query = null;
        // 查询字段解析
        QueryParser parser = new QueryParser(field, ikAnalyzer);

        int num = (pageNum - 1) * size;
        // 获取lucene索引文件
        Directory directory = null;
        IndexReader indexReader = null;
        try {
            directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
            indexReader = DirectoryReader.open(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  创建IndexReader
        // 创建查询对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        try {
            query = parser.parse(keyword);
            // 查询本页之前的数据
            TopDocs topDocs = indexSearcher.search(query, num);
            if (topDocs.scoreDocs.length >= num) {
                // 获取到上一页最后一条
                ScoreDoc preScore = topDocs.scoreDocs[num - 1];

                // 解析关键字
                query = parser.parse(keyword);
                QueryScorer score = new QueryScorer(query, field);
                SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">",
                        "</font>");
                Highlighter highlighter = new Highlighter(formatter, score);
                // 获取搜索的结果，指定返回document返回的个数
                ScoreDoc[] hits = indexSearcher.searchAfter(preScore, query, size).scoreDocs;

                // 遍历ScoreDoc
                for (ScoreDoc scoreDoc : hits) {
                    // 根据Document的id找到document对象
                    Document doc = indexSearcher.doc(scoreDoc.doc);
                    // 获取TokenStream
                    TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, field, ikAnalyzer);
                    SimpleSpanFragmenter fragmenter = new SimpleSpanFragmenter(score);
                    highlighter.setTextFragmenter(fragmenter);
                    // 获取高亮
                    String bestFragment = highlighter.getBestFragment(tokenStream, doc.get(field));
                    QueryInfo fi = new QueryInfo();
                    fi.setContent(bestFragment);
                    fi.setPath(doc.get("path"));
                    list.add(fi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
