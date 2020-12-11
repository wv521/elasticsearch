package com.elasticsearch.queryType;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class GroupQuery {
    public static void createIndex() throws Exception {
        // 索引存放路径
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\facet\\index"));
        // 创建IKAnalzyer分析器
        IKAnalyzer analyzer = new IKAnalyzer();
        // 创建IndexWriterConfig
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        // 是否使用复合索引格式
        iwc.setUseCompoundFile(true);
        // 创建IndexWriter
        IndexWriter indexWriter = new IndexWriter(directory, iwc);

        // DirectoryTaxonomyWriter写入Taxonomy（分类索引）索引
        Directory taxioDirectory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\facet\\taxo"));
        DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxioDirectory);
        // facet配置，类似IndexWriterConfig 构建分类索引配置类
        FacetsConfig config = new FacetsConfig();

        Document doc = new Document();
        doc.add(new TextField("taxomony", "电脑", Field.Store.YES));
        doc.add(new TextField("product", "MacBook", Field.Store.YES));
        doc.add(new FacetField("brand", "Apple"));
        doc.add(new FacetField("shopper", "shanghai"));
        // 写入索引的同时写入分类索引
        // config.build(taxoWriter, doc) 构建分类索引
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "电脑", Field.Store.YES));
        doc.add(new TextField("product", "Huawei Book", Field.Store.YES));
        doc.add(new FacetField("brand", "Huawei"));
        doc.add(new FacetField("shopper", "shanghai"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "电脑", Field.Store.YES));
        doc.add(new TextField("product", "小米Book", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        doc.add(new FacetField("shopper", "beijing"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "手机", Field.Store.YES));
        doc.add(new TextField("product", "iPhone11", Field.Store.YES));
        doc.add(new FacetField("brand", "Apple"));
        doc.add(new FacetField("shopper", "shenzhen"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "手机", Field.Store.YES));
        doc.add(new TextField("product", "Huawei mate30", Field.Store.YES));
        doc.add(new FacetField("brand", "Huawei"));
        doc.add(new FacetField("shopper", "shenzhen"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "手机", Field.Store.YES));
        doc.add(new TextField("product", "小米9", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        doc.add(new FacetField("shopper", "wuhan"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("taxomony", "电视", Field.Store.YES));
        doc.add(new TextField("product", "小米电视", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        doc.add(new FacetField("shopper", "wuhan"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        indexWriter.close();
        taxoWriter.close();
    }

    public static void facetIndex() throws IOException {
        // 获取普通索引文件位置
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\facet\\index"));
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        // 分类读取
        Directory taxoDirectory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\facet\\taxo"));
        TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDirectory);
        FacetsConfig config = new FacetsConfig(); // Facet配置类
        // 收集器。Collector主要用于从搜索中收集原始结果，并进行排序或对结果进行过滤，整理等操作
        FacetsCollector facetsCollector = new FacetsCollector();

        // 按照电脑这个维度查询
        TermQuery query = new TermQuery(new Term("taxomony", "电脑"));
        TopDocs docs = FacetsCollector.search(searcher, query, 10, facetsCollector); // 进行收集
        // 将收集的内容构建成一个Facet对象
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, facetsCollector);
        List<FacetResult> results = facets.getAllDims(10); // 获取结果集前N条
        // 打印维度信息
        for (FacetResult tmp : results) {
             System.out.println(tmp);
        }

        // 下钻查询
//        DrillDownQuery drillDownQuery = new DrillDownQuery(config, query);
//        drillDownQuery.add("brand", "Huawei");
//        // new collector，否则会进行累加
//        FacetsCollector facetsCollector1 = new FacetsCollector();
//        docs = FacetsCollector.search(searcher, drillDownQuery, 10, facetsCollector1);
//        facets = new FastTaxonomyFacetCounts(taxoReader, config, facetsCollector1);
//        results = facets.getAllDims(10);
//        for (FacetResult tmp : results) {
//            // System.out.println(tmp);
//        }
//
//        // 下钻查询
//        drillDownQuery.add("shopper", "shanghai");
//        FacetsCollector facetsCollector2 = new FacetsCollector();
//        docs = FacetsCollector.search(searcher, drillDownQuery, 10, facetsCollector2);
//        facets = new FastTaxonomyFacetCounts(taxoReader, config, facetsCollector2);
//        results = facets.getAllDims(10);
//        for (FacetResult tmp : results) {
//            // System.out.println(tmp);
//        }
//
//        // 查询平行维度信息
//        DrillSideways ds = new DrillSideways(searcher, config, taxoReader);
//        DrillDownQuery drillQuery = new DrillDownQuery(config, query);
//        drillQuery.add("brand", "Huawei");
//        DrillSideways.DrillSidewaysResult result = ds.search(drillQuery, 10);
//        docs = result.hits;
//        results = result.facets.getAllDims(10);
//        for (FacetResult tmp : results) {
//            System.out.println(tmp);
//        }

        indexReader.close();
        taxoReader.close();
    }

    public static void main(String[] args) {
        try {
            // 使用分组是Field必须是 FacetField
//             createIndex();
             facetIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
