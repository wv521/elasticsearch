package com.elasticsearch.queryType;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class SpanQueryDemo {

    public static void createIndex() throws IOException {
        // 索引存放路径
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\span"));

        // 创建IKAnalzyer分析器
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 创建IndexWriterConfig
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        // 是否使用复合索引格式
        iwc.setUseCompoundFile(true);

        // 创建IndexWriter
        IndexWriter indexWriter = new IndexWriter(directory, iwc);

        Document doc = new Document();
        doc.add(new TextField("text", "the quick brown fox jumps over the lazy dog", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new TextField("text", "the quick red fox jumps over the sleepy cat", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new TextField("text", "the quick brown fox jumps over the lazy dog", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new TextField("text", "the quick brown adult slave nice fox winde felt testcase gox quick jumps over the lazy dog", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new TextField("text", "the quick brown fox quick jumps over the lazy dog", Field.Store.YES));

        indexWriter.addDocument(doc);
        indexWriter.close();
    }

    // 跨度查询
    public static void spanNearQuery() throws IOException {
        // 索引存放路径
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\span"));
        // 创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
        // 创建IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        SpanQuery start = new SpanTermQuery(new Term("text", "dog"));
        SpanQuery end = new SpanTermQuery(new Term("text", "quick"));
        SpanQuery query = new SpanNearQuery(new SpanQuery[]{start, end}, 6,
                false);

        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;

        // 遍历ScoreDoc
        for (ScoreDoc scoreDoc : hits) {
            // 根据Document的id找到document对象
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.get("text"));
        }
        indexReader.close();
        directory.close();
    }

    // 排除span查询
    public static void spanNotQuery() throws IOException {
        // 索引存放路径
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\span"));
        // 创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
        // 创建IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        // TermQuery
        SpanQuery start = new SpanTermQuery(new Term("text", "dog"));
        SpanQuery end = new SpanTermQuery(new Term("text", "quick"));
        SpanQuery exclude = new SpanTermQuery(new Term("text", "fox"));
        SpanQuery nearQuery = new SpanNearQuery(
                new SpanQuery[]{start, end}, 12, false); //12
        SpanNotQuery query = new SpanNotQuery(nearQuery, exclude, 4, 3);

        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;

        // 遍历ScoreDoc
        for (ScoreDoc scoreDoc : hits) {
            // 根据Document的id找到document对象
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.get("text"));
        }
        indexReader.close();
        directory.close();
    }

    // 合并查询
    public static void spanOrQuery() throws IOException {
        // 索引存放路径
        Directory directory = FSDirectory.open(Paths.get("G:\\elasticsearch\\lucene\\span"));
        // 创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);
        // 创建IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        SpanQuery start = new SpanTermQuery(new Term("text", "dog"));
        SpanQuery end = new SpanTermQuery(new Term("text", "quick"));
        SpanQuery exclude = new SpanTermQuery(new Term("text", "fox"));
        SpanQuery spanNearQuery = new SpanNearQuery(
                new SpanQuery[]{start, end}, 12, false);

        SpanNotQuery spanNotQuery = new SpanNotQuery(spanNearQuery, exclude, 4, 3);
        SpanQuery spanTermQuery = new SpanTermQuery(new Term("text", "sick"));
        SpanOrQuery query = new SpanOrQuery(spanNotQuery, spanTermQuery);

        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;

        // 遍历ScoreDoc
        for (ScoreDoc scoreDoc : hits) {
            // 根据Document的id找到document对象
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.get("text"));
        }
        indexReader.close();
        directory.close();
    }

    public static void main(String[] args) throws IOException {
//         createIndex();
//        spanNearQuery();
//        spanNotQuery();
//        spanOrQuery();
    }

}
