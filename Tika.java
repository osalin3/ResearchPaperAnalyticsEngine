import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Tika {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>
    {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            //replace this with proccedure to extract plain text
            //StringTokenizer itr = new StringTokenizer(value.toString());
            InputStream is = new FileInputStream("en-ner-person.bin");
            TokenNameFinderModel model = new TokenNameFinderModel(is);
            is.close();

            NameFinderME nameFinder = new NameFinderME(model);
            //split the input
            String[] names = value.toString().split(" ");
            //find the names with opennlp model
            Span nameSpans[] = nameFinder.find(names);

            for(Span s: nameSpans)
            {
                int start = s.getStart();
                int end = s.getEnd();
                String temp = "";
                for (int i = start; i < end; i++) {
                    temp = names[i];
                    //System.out.println("TEMP: " + temp);
                    //write the names
                    context.write(new Text("string"), one);
                }
            }
        }
    }
    public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable>
    {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static String parse(String directory, String filename) throws IOException, TikaException, SAXException
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        String parsedFileName = directory;
        parsedFileName = parsedFileName.concat(filename);
        FileInputStream inputStream = new FileInputStream(new File(parsedFileName));
        ParseContext pContext = new ParseContext();

        //parsing the document using PDF parser
        PDFParser pdfparser = new PDFParser();
        pdfparser.parse(inputStream, handler, metadata,pContext);

        //getting the content of the document
        //System.out.println("Contents of the PDF :" + handler.toString());
        return handler.toString();
    }
    public static void findNameTokens (String textFile) throws IOException {
        InputStream is = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();

        NameFinderME nameFinder = new NameFinderME(model);

        String[] textArray = textFile.split(" ");
        for(int i = 0; i < textArray.length; i++)
        {
            System.out.println("String in array: " + textArray[i]);
        }


        Span nameSpans[] = nameFinder.find(textArray);

        for(Span s: nameSpans)
        {
            int start = s.getStart();
            int end = s.getEnd();
            String temp = "";
            for (int i = start; i < end; i++) {
                temp = textArray[i];
                //System.out.println("TEMP: " + temp);
                /*at this point we can create the key values necc.
                  to create the key/pair values.
                * */
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        downloadTest newTest = new downloadTest();
        newTest.main();
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "PDFS");
        job.setJarByClass(Tika.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
