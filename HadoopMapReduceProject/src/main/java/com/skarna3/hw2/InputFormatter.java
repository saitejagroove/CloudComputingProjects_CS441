/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skarna3.hw2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Reads records that are delimited by a specific begin/end tag.
 */
public class InputFormatter extends TextInputFormat {
	
  private Logger log = LoggerFactory.getLogger(this.getClass());

  public static final String START_TAGS = "xmlinput.start";
  public static final String END_TAGS = "xmlinput.end";

  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
    try {
      return new XmlRecordReader((FileSplit) split, context.getConfiguration());
    } catch (IOException ioe) {
      log.warn("Error while creating XmlRecordReader", ioe);
      return null;
    }
  }

  /**
   * XMLRecordReader class to read through a given xml document to output xml blocks as records as specified
   * by the start tag and end tag
   * 
   */
  public static class XmlRecordReader extends RecordReader<LongWritable, Text> {

    //private final byte[] startTag;
    //private final byte[] endTag;
    private String[] startTags;
    private String[] endTags;
    private final long start;
    private final long end;
    private final FSDataInputStream fsin;
    private final DataOutputBuffer buffer = new DataOutputBuffer();
    private LongWritable currentKey;
    private Text currentValue;

    public XmlRecordReader(FileSplit split, Configuration conf) throws IOException {
      startTags = conf.get(START_TAGS).split(",");
      endTags = conf.get(END_TAGS).split(",");

      // open the file and seek to the start of the split
      start = split.getStart();
      end = start + split.getLength();
      Path file = split.getPath();
      FileSystem fs = file.getFileSystem(conf);
      fsin = fs.open(split.getPath());
      fsin.seek(start);
    }

    private boolean next(LongWritable key, Text value) throws IOException {

      if (fsin.getPos() < end && readUntilStartTag2(startTags, false)) {
        try {
          buffer.write(startTags[tag].getBytes(StandardCharsets.UTF_8));
          if (readUntilMatch(endTags[tag].getBytes(StandardCharsets.UTF_8), true)) {
            key.set(fsin.getPos());
            value.set(buffer.getData(), 0, buffer.getLength());
            return true;
          }
        } finally {
          buffer.reset();
        }
      }
      return false;
    }

    @Override
    public void close() throws IOException {
      fsin.close();
    }

    @Override
    public float getProgress() throws IOException {
      return (fsin.getPos() - start) / (float) (end - start);
    }


    /**
     * @param currentTag tag buffer until now
     * @param tags tags array in which we can search current tag
     * @return position of matching start tag in tags array
     */
    private int returnEndTagPosition(String currentTag, String[] tags) {
        for (int j=0; j<tags.length; j++) {
          if (tags[j].equals(currentTag)) {
            return j;
          }
      }
      return -1;
    }

    /**
     *
     * This method has been heavily modified w.r.t. original method in the Mahout class to allow for multiple start tags.
     *
     * @param tags array of possible matching tags
     * @return position of matching start tag in tags array
     * @throws IOException
     */

    private static Integer tag = null;
    private boolean readUntilStartTag2(String[] tags, boolean withinBlock) throws IOException {

      int[] tagIndexes = new int[tags.length];
      //Fill with 0
      for (int i=0; i<tagIndexes.length; i++) {
        tagIndexes[i] = 0;
      }

      while (true) {
        int b = fsin.read();
        // end of file:
        if (b == -1) {
          return false;
        }
        // save to buffer:
        if (withinBlock) {
          buffer.write(b);
        }

        for (int j=0; j<tags.length; j++) {

          String currentTag = tags[j];
          byte[] currentTagBytes = currentTag.getBytes(Constants.UTF8());
          int currenti = tagIndexes[j];

          // check if we're matching:
          if (b == currentTagBytes[currenti]) {
            tagIndexes[j]++;
            if (tagIndexes[j] >= currentTagBytes.length) {
              tag = j;
              return true;
            }
          } else {
            tagIndexes[j] = 0;
          }
        }

        // see if we've passed the stop point:
        if (!withinBlock && fsin.getPos() >= end) {

          int cc = 0;
          for (int k=0; k<tags.length; k++) {
            if (tagIndexes[k] == 0) {
              cc++;
            }
          }

          //They are all 0
          if (cc==tags.length) {
            return false;
          }
        }
      }
    }

    private int readUntilStartTag(String[] tags) throws IOException {
      StringBuilder currentTag = new StringBuilder();

      while (true) {
        int b = fsin.read();
        // end of file:
        if (b == -1) {
          return -1;
        }

        //Over the stop point *** terminate ***
        if (fsin.getPos() >= end) {
          return -1;
        }

        char c = (char)b;

        if (c == '<') {
          currentTag = null;
          currentTag = new StringBuilder();
        }

        currentTag.append(c);

        if ((c == ' ')) {
          for (int j=0; j<tags.length; j++) {
            if (currentTag.toString().contains(tags[j])) {
              currentTag = null;
              return j;
            }
          }
        }
      }
    }

    private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
      int i = 0;
      while (true) {
        int b = fsin.read();
        // end of file:
        if (b == -1) {
          return false;
        }
        // save to buffer:
        if (withinBlock) {
          buffer.write(b);
        }

        // check if we're matching:
        if (b == match[i]) {
          i++;
          if (i >= match.length) {
            return true;
          }
        } else {
          i = 0;
        }
        // see if we've passed the stop point:
        if (!withinBlock && i == 0 && fsin.getPos() >= end) {
          return false;
        }
      }
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
      return currentKey;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
      return currentValue;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
      currentKey = new LongWritable();
      currentValue = new Text();
      return next(currentKey, currentValue);
    }
  }
}
