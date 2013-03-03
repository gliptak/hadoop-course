package mr.reviews.fsstruct.avro;

import java.io.IOException;

import mr.reviews.fsstruct.avro.model.ReviewAvro;
import mr.reviews.fsstruct.avro.model.ReviewReportAvro;
import mr.reviews.fsstruct.support.ConfHelper;
import mr.reviews.model.ReviewKeyWritable;
import mr.reviews.model.ReviewWritable;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class ReviewAvroInputOutputJob extends Configured implements Tool {
    
    public final static String PROP_FIND_VALUE  = "report.value";
    public final static String PROP_INPUT_PATH = "report.input.path";
    public final static String PROP_OUTPUT_PATH = "report.output.path";
    
	@Override
	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), this.getClass().getName());
		job.setJarByClass(getClass());
		configureJob(job);
		return job.waitForCompletion(true) ? 0 : 1;
	}

    private void configureJob(Job job) throws IOException {
        Configuration jobConf = job.getConfiguration();
        ConfHelper confHelper = new ConfHelper(jobConf);
		
		job.setInputFormatClass(AvroKeyInputFormat.class);
		AvroKeyInputFormat.addInputPath(job, confHelper.getInput());
		
		job.setOutputFormatClass(AvroKeyOutputFormat.class);
		AvroKeyOutputFormat.setOutputPath(job, confHelper.getOutputPath());
		
		// **********************************************
//      AvroKeyOutputFormat.setOutputCompressorClass(job, SnappyCodec.class);
        // This will not work as avro uses its own property but the method still
        // exists since Avro's output format(s) extend FileOutputFormat; this is
        // one downfall of inheritance
        // **********************************************
		
		AvroKeyOutputFormat.setCompressOutput(job, true);
		jobConf.set(AvroJob.CONF_OUTPUT_CODEC, CodecFactory.snappyCodec().toString());
		job.setOutputKeyClass(ReviewAvro.class);
        job.setOutputValueClass(NullWritable.class);
        AvroJob.setOutputKeySchema(job, ReviewReportAvro.SCHEMA$);
		
		// configure mapper and reducer
		job.setMapperClass(ReviewAvroInputMapper.class);
		job.setReducerClass(ReviewAvroOutputReducer.class);
		
		// configure keys used between mappers and reducers
		job.setMapOutputKeyClass(ReviewKeyWritable.class);
		job.setMapOutputValueClass(ReviewWritable.class);
    }

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new ReviewAvroInputOutputJob(), args);
		System.exit(exitCode);
	}
}
