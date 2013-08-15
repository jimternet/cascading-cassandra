package com.ifesdjeen.cascading.cassandra.sinks;

import cascading.tuple.TupleEntry;
import com.ifesdjeen.cascading.cassandra.hadoop.SerializerHelper;
import org.apache.hadoop.mapred.OutputCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CqlSink implements Serializable,IConfigurableSink {

  private static final Logger logger = LoggerFactory.getLogger(CqlSink.class);

  private List<String> cqlKeys;
  private List<String> cqlValues;
  private Map<String,String> columnMappings;

  @Override
  public void configure(Map<String, Object> settings) {
    this.columnMappings = (Map<String, String>) settings.get("mappings.cql");
    this.cqlKeys = (List<String>) settings.get("mappings.cqlKeys");
    this.cqlValues = (List<String>) settings.get("mappings.cqlValues");
  }

  public void sink(TupleEntry tupleEntry, OutputCollector outputCollector)
          throws IOException {
    Map<String, ByteBuffer> keys = new LinkedHashMap<String, ByteBuffer>();
    List<ByteBuffer> values = new ArrayList<ByteBuffer>();

    for (String key: cqlKeys) {
      keys.put(key, SerializerHelper.serialize(tupleEntry.getObject(columnMappings.get(key))));
    }

    // Unfortunately we can't just use hashmap because of order-dependent CQL queries. OP
    for (String value: cqlValues) {
      values.add(SerializerHelper.serialize(tupleEntry.getObject(columnMappings.get(value))));
    }

    outputCollector.collect(keys, values);
  }
}