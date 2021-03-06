package com.ifesdjeen.cascading.cassandra.sources;

import java.util.*;
import java.nio.ByteBuffer;
import java.io.IOException;

import com.ifesdjeen.cascading.cassandra.SettingsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.tuple.Tuple;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.utils.ByteBufferUtil;

import com.ifesdjeen.cascading.cassandra.hadoop.SerializerHelper;


public class DynamicRowSource extends BaseThriftSource implements ISource {

  protected static final Logger logger = LoggerFactory.getLogger(DynamicRowSource.class);

  public Tuple source(Map<String, Object> settings,
                      Object boxedKey,
                      Object boxedColumns) throws IOException {
    SortedMap<ByteBuffer, IColumn> columns = (SortedMap<ByteBuffer, IColumn>) boxedColumns;
    ByteBuffer key = (ByteBuffer) boxedKey;

    Tuple result = new Tuple();
    result.add(ByteBufferUtil.string(key));

    Map<String, String> dataTypes = SettingsHelper.getDynamicTypes(settings);

    if (columns.values().isEmpty()) {
      logger.info("Values are empty.");
    }

    AbstractType columnNameType = SerializerHelper.inferType(dataTypes.get("columnName"));
    AbstractType columnValueType = null;
    if (dataTypes.get("columnValue") != null) {
      columnValueType = SerializerHelper.inferType(dataTypes.get("columnValue"));
    }

    for (IColumn column : columns.values()) {
      try {
        if (columnNameType instanceof CompositeType) {
          List components = (List) SerializerHelper.deserialize(column.name(), columnNameType);
          for (Object component : components) {
            result.add(component);
          }
        } else {
          Object val = SerializerHelper.deserialize(column.name(), columnNameType);
          result.add(val);
        }

        if (columnValueType != null) {
          Object colVal = SerializerHelper.deserialize(column.value(), columnValueType);
          result.add(colVal);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    return result;
  }
}
