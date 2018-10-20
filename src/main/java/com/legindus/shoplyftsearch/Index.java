package com.legindus.shoplyftsearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Index {

    public Analyzer analyser;
    public Directory index;
    public IndexWriterConfig indexWriterConfig;
    public IndexWriter indexWriter;

    public IndexSearcher indexSearcher;

    public Index(List<CategoryDocument> documents) throws IOException {
        analyser = new StandardAnalyzer();
        index = new RAMDirectory();
        indexWriterConfig = new IndexWriterConfig(analyser);
        indexWriter = new IndexWriter(index, indexWriterConfig);
        for(CategoryDocument document : documents) {
            Document doc = new Document();

            doc.add(new TextField("category", document.categoryName, Field.Store.YES));
            StringBuilder builder = new StringBuilder();
            for(String s : document.keywords) {
                builder.append(s);
            }
            doc.add(new StringField("keywords", builder.toString(), Field.Store.YES));
        }

        IndexReader reader = DirectoryReader.open(index);
        indexSearcher = new IndexSearcher(reader);
    }

    public List<String> search(String query) throws IOException {
        QueryParser parser = new QueryParser("keywords", analyser);

        Query q = parser.parse(query);
        System.out.println(q.toString());

        TopDocs hits;
        ScoreDoc[] docs;
        try {
            hits = indexSearcher.search(q, 10);
            docs = hits.scoreDocs;
        } catch (IOException e) {
            System.err.println("Search Error in Index::search(): " + e.getMessage());
        }

        ArrayList<String> list = new ArrayList<>();
        for(ScoreDoc doc: docs) {
            int docId = doc.doc;
            Document d = indexSearcher.doc(docId);
            list.add(d.get("category"));
        }

        System.out.println(list);
        return list;
    }

}
