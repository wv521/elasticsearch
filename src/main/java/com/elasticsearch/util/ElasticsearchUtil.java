package com.elasticsearch.util;

import com.elasticsearch.model.QueryInfo;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ElasticsearchUtil {

    ElasticsearchUtil(){
        try {
            create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create() throws Exception{
         //原始数据文件目录
        File file = new File("G:elasticsearch/lucene/data");
        // 指定索引文件在硬盘中的位置
        Directory directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
        // 创建索引的写出工具类。
        // 配置类，指定分词器
//        SmartChineseAnalyzer ik = new SmartChineseAnalyzer(); // 标准分词器
        IKAnalyzer ik = new IKAnalyzer();  // ik分词器
        // 创建写索引的配置对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ik);
        indexWriterConfig.setUseCompoundFile(true);
        // 创建写索引对象。参数：索引文件目录，分词器
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        for(File f: Objects.requireNonNull(file.listFiles())){
            String path = f.getPath(); // 路径
            String name = f.getName(); // 名称
            int i = name.lastIndexOf(".");// 名称
            // 创建文档对象
            Document document = new Document();
            // 给文档添加file.这里选Store.YES代表存储到文档列表。Store.NO代表不存储.
            // file类型：StringField、TextField、LongFiled.....
            document.add(new TextField("content", FileUtils.readFileToString(f,"UTF-8"), Field.Store.YES));
            document.add(new StringField("path", path, Field.Store.YES));
            document.add(new TextField("title", name.substring(0,i), Field.Store.YES));
            // 添加到索引文件中去
            indexWriter.addDocument(document);
        }
        // 写入完毕，清理工作
        if (indexWriter != null) {
            indexWriter.close();
            indexWriter = null;
        }

    }

    public static List<QueryInfo> search(String file, String keyword)throws Exception{

        List<QueryInfo> list = new ArrayList<>();
        // 获取lucene索引文件
        Directory directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
        //  创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
       // 创建查询对象
        IndexSearcher indexSearch = new IndexSearcher(indexReader);

         // 构造查询条件
//        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(); // 标准分词器
        IKAnalyzer analyzer = new IKAnalyzer();  // ik分词器
        // 查询字段解析
        QueryParser parser = new QueryParser(file,analyzer);
        Query parse = parser.parse(keyword); // 查询对象。
        QueryScorer score = new QueryScorer(parse, file);
        // 高亮字体颜色
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">",
                "</font>");
        // 高亮显示
        Highlighter highlighter = new Highlighter(formatter, score);
        // 执行查询
        ScoreDoc[] scoreDocs = indexSearch.search(parse, 10).scoreDocs;
        for(ScoreDoc ss: scoreDocs){
            // 获取文档内容。
            Document doc = indexSearch.doc(ss.doc);
            // 获取TokenStream，
            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearch.getIndexReader(), ss.doc, file, analyzer);
            SimpleSpanFragmenter ff = new SimpleSpanFragmenter(score);
            highlighter.setTextFragmenter(ff);
            // 获取高亮
            String fragment = highlighter.getBestFragment(tokenStream, doc.get(file));
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setContent(fragment);
            queryInfo.setTitle(doc.get("title"));
            list.add(queryInfo);

        }
        indexReader.close();
        directory.close();
        return list;
    }


}
