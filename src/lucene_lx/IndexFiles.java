package lucene_lx;


import java.io.*;
import java.io.File; 
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.ParserConfigurationException; 
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.xml.sax.SAXException;

public class IndexFiles {

    //创建索引库IndexWriter
    private static IndexWriter getIndexWriter(String indexPath) throws IOException{
        Analyzer analyzer = new StandardAnalyzer();
        Directory dir=FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(dir, config);
    }
    
    private static String xml_node(String tag, String path) throws IOException, ParserConfigurationException, SAXException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document document = db.parse(path);
        NodeList bookList = document.getElementsByTagName(tag);
        Node book = bookList.item(0);
    	return book.getTextContent();    	
    }
    
    private static void index(IndexWriter writer,String docPath) throws IOException, ParserConfigurationException, SAXException {
        File docFile = new File(docPath);
        String[] docList = docFile.list();
        if (docList != null)
            for (int i = 0; i < docList.length; i++) {
            	String path="/home/leo/xml_demo/" + docList[i];
            	System.out.println(path);
                File fp = new File(path);
                if (fp.isFile()) {
                	Document document = new Document();
                    //Element rootElement = document.getDocumentElement();

                    //index file contents
                    //Field contentField = new TextField("contents", new FileReader(fp));
                    //index file categories
                    Field fileCategoriesField = new StringField("categories",xml_node("categories",path), Field.Store.YES);
                    //index file attributes
                    Field fileAttributesField = new StringField("attributes",xml_node("attributes",path),Field.Store.YES);
                    //index file similar
                    Field fileSimilarField = new StringField("similar",xml_node("similar",path),Field.Store.YES);
                    //index file related
                    Field fileRelatedField = new StringField("related",xml_node("related",path),Field.Store.YES);
                    //index file name
                    Field fileNameField = new StringField("filename", fp.getName(), Field.Store.YES);
                    //index file path
                    Field filePathField = new StringField("filepath", fp.getCanonicalPath(), Field.Store.YES);

                    //document.add(contentField);
                    document.add(fileCategoriesField);
                    document.add(fileAttributesField);
                    document.add(fileSimilarField);
                    document.add(fileRelatedField);
                    document.add(fileNameField);
                    document.add(filePathField);
                    System.out.println("adding " + fp);
                    writer.addDocument(document);
                }
            }
        writer.forceMerge(1);//优化压缩段，大规模添加数据的时候才使用
        writer.commit();//提交数据
        System.out.println("索引添加成功！");
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String indexPath = "/home/leo/index";
        String docPath="/home/leo/xml_demo";
        IndexWriter writer = getIndexWriter(indexPath);
        index(writer,docPath);
        writer.close();
    }

}