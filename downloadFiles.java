import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.Scanner;
public class downloadFiles {

	
    public static void readSave(String fileURL, int fileIndex)throws IOException, TikaException, SAXException{
        try {
            URL url = new URL(fileURL);
            InputStream in = url.openStream();
            String fileName = "./pdfFiles/file";
            fileName = fileName.concat(Integer.toString(fileIndex));
            fileName = fileName.concat(".pdf");
            System.out.println("new file name: " + fileName);
            OutputStream fos = new FileOutputStream(fileName);

            int length = -1;

            byte[] buffer = new byte[1024];

            while((length = in.read(buffer)) != -1) {

                fos.write(buffer, 0, length);

            }
            parse(fileName, fileIndex);
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static void parseFeed(Document doc1)
    {
        try {

            doc1.getDocumentElement().normalize();

            FileWriter fileWriter = new FileWriter("pdfLinks.txt",true);

            BufferedWriter out = new BufferedWriter(fileWriter);

            System.out.println("root of xml file" + doc1.getDocumentElement().getNodeName());

            int linkCount = 0;
            NodeList nodeList = doc1.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // do something with the current element
                    String nodeType = node.getNodeName();
                    if(nodeType.equals("link"))
                    {
                        NamedNodeMap attrs = node.getAttributes();
                        String href = "";
                        boolean pdfLink = false;
                        for(int j = 0 ; j<attrs.getLength() ; j++) {
                            Attr attribute = (Attr)attrs.item(j);
                            String attrName = attribute.getName();
                            if(attrName.equals("href")) //save the link here and then check if it is a link to a PDF
                            {
                                href = attribute.getValue();
                            }
                            if(attrName.equals("title"))
                            {
                                String attrVal = attribute.getValue();
                                if(attrVal.equals("pdf"))
                                {
                                    pdfLink = true;
                                }
                            }
                            if(pdfLink == true)
                            {
                                String appendedHREF = href.replaceAll("http", "https");
                                System.out.println("new HREF value: " + appendedHREF);
                                //save the link here, append the right https as well
                                pdfLink = false;
                                linkCount++;
                                out.append(appendedHREF);
                                out.newLine();
                            }
                            //System.out.println(" " + attribute.getName()+" = "+attribute.getValue());
                        }
                    }
                }
            }
            System.out.println("Number of links: " + linkCount);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
    public static void parse(String filename, int fileIndex) throws IOException, TikaException, SAXException
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        String parsedFileName = (filename);
        FileInputStream inputStream = new FileInputStream(new File(parsedFileName));
        ParseContext pContext = new ParseContext();

        //parsing the document using PDF parser
        PDFParser pdfparser = new PDFParser();
        pdfparser.parse(inputStream, handler, metadata,pContext);

        String filenameOut = "./textFiles/file";
        filenameOut = filenameOut.concat(Integer.toString(fileIndex));
        filenameOut = filenameOut.concat(".txt");

        FileWriter fileWriter = new FileWriter(filenameOut,true);
        BufferedWriter out = new BufferedWriter(fileWriter);
        out.append(handler.toString());
        out.close();
    }
    public static void main() throws FileNotFoundException, IOException, TikaException, SAXException{

        URL oURL;
        URLConnection oConnection;
        BufferedReader oReader;
        String sLine;
        StringBuilder sbResponse;
        String sResponse = null;

        Scanner input = new Scanner(System.in);
        System.out.print("Enter a search Query: ");
        String searchQuery = input.nextLine();
        input.close();


        try
        {
            String StringQuery = "http://export.arxiv.org/api/query?search_query=all:%22electron%22&start=0&max_results=50";
            StringQuery = StringQuery.replaceAll("electron", searchQuery);
            //System.out.println(StringQuery);
            oURL = new URL(StringQuery);
            oConnection = oURL.openConnection();
            oReader = new BufferedReader(new InputStreamReader(oConnection.getInputStream()));
            sbResponse = new StringBuilder();

            while((sLine = oReader.readLine()) != null)
            {
                sbResponse.append(sLine);
            }

            sResponse = sbResponse.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;
        try
        {
            builder = factory.newDocumentBuilder();
            document = builder.parse( new InputSource( new StringReader( sResponse ) ) );
        } catch (Exception e) {
            e.printStackTrace();
        }

        parseFeed(document);

        int fileCount = 0;
        String URLfile = "pdfLinks.txt";

            Scanner linReader = new Scanner(new File(URLfile));

            while (linReader.hasNext())
            {
                String line = linReader.nextLine();
                //System.out.println(line);
                readSave(line, fileCount);
                fileCount++;
            }
            linReader.close();
    }
}