package com.elasticsearch;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


@SpringBootApplication
public class ElasticsearchApplication {


    public static void create() throws Exception {
//        // 原始数据文件目录
        File files = new File("G:elasticsearch/lucene/data");
//        // 指定索引文件在硬盘中的位置
        Directory directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
        // 创建索引的写出工具类。
        // 配置类，指定分词器
//        SmartChineseAnalyzer ik = new SmartChineseAnalyzer(); // 标准分词器
        IKAnalyzer ik = new IKAnalyzer();  // ik分词器
        // 创建写索引的配置对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ik);
        indexWriterConfig.setUseCompoundFile(false);
        // 创建写索引对象。参数：索引文件目录，分词器
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        for(File f: Objects.requireNonNull(files.listFiles())){
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


//    public static void updateIndex() throws Exception{
//        // 指定索引文件在硬盘中的位置
//        Directory directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
//        // 创建索引的写出工具类。
//        // 配置类，指定分词器
//        SmartChineseAnalyzer ik = new SmartChineseAnalyzer(); // 标准分词器
////        IKAnalyzer ik = new IKAnalyzer();  // ik分词器
//        // 创建写索引的配置对象
//        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ik);
//        indexWriterConfig.setUseCompoundFile(true);
//        // 创建写索引对象。参数：索引文件目录，分词器
//        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
//            // 创建文档对象
//            Document document = new Document();
//            // 给文档添加file.这里选Store.YES代表存储到文档列表。Store.NO代表不存储.
//            // file类型：StringField、TextField、LongFiled.....
//            document.add(new TextField("content", "上海交通大学哈哈哈,爱学习!!!!", Field.Store.YES));
//            document.add(new StringField("path", "G:elasticsearch/lucene/data", Field.Store.YES));
//            document.add(new TextField("title","上海交通大学.txt", Field.Store.YES));
//            // 添加到索引文件中去
//            indexWriter.addDocument(document);
//            indexWriter.commit();
//            indexWriter.close();
//        System.out.println("更新完毕");
//    }
//
    public static void search() throws Exception{

        // 获取lucene索引文件
        Directory directory = FSDirectory.open(Paths.get("G:/elasticsearch/lucene/index"));
        //  创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
        // 创建查询对象
        IndexSearcher indexSearch = new IndexSearcher(indexReader);
        // 构造查询条件
        // 方式一:此项搜索
        // 指定查询名，以及查询关键字分词
//        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(); // 标准分词器
//        IKAnalyzer analyzer = new IKAnalyzer();  // ik分词器
//        QueryParser parser = new QueryParser("content",analyzer);
//        Query query = parser.parse("武汉");

        // 方式二:布尔查询
//        Term term1 = new Term("content", "北京");
//        Query query1 = new TermQuery(term1);
//        Term term2 = new Term("content", "武汉");
//        Query query2 = new TermQuery(term2);
//        // 合集查询
//        BooleanQuery query = new BooleanQuery.Builder()
//                .add(query1, BooleanClause.Occur.MUST)
//                .add(query2, BooleanClause.Occur.MUST)
//                .build();

        // 方式三:加权查询
//        BoostQuery query1 = new BoostQuery(new TermQuery(new Term("content", "北京")), 1.5f);
//        BoostQuery query2 = new BoostQuery(new TermQuery(new Term("content", "武汉")), 1.0f);
//
//        // 合集查询
//        BooleanQuery query = new BooleanQuery.Builder()
//                .add(query1, BooleanClause.Occur.SHOULD)
//                .add(query2, BooleanClause.Occur.SHOULD)
//                .build();

        // FuzzyQuery 模糊查询
        Term term = new Term("content","屋汉");
        FuzzyQuery query = new FuzzyQuery(term,1);


        // 执行查询
        TopDocs search = indexSearch.search(query, 10); // 匹配条件的前N个结果
        // 返回的是Document的id
        ScoreDoc[] scoreDocs = indexSearch.search(query, 10).scoreDocs;// 执行分页查询
        for(ScoreDoc ss: scoreDocs){
            // 遍历所有文档id，获取具体文档内容。
            Document doc = indexSearch.doc(ss.doc);
            System.out.print(doc.get("title")+":    ");
            System.out.println(doc.get("path"));
            System.out.println(doc.get("content"));

        }
        indexReader.close();
        directory.close();
    }

    public static void main(String[] args) throws Exception {
//        SpringApplication.run(ElasticsearchApplication.class, args);
//        create(); // 创建
//        search(); // 搜索

    }

}
