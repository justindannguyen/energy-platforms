/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.integration.annotation.Splitter;

import com.justin.energy.server.stream.dto.cloud.RegisterDefinitionDto;
import com.justin.energy.server.stream.dto.cloud.RegisterValueDto;
import com.justin.energy.server.stream.dto.device.GatewayUsageDto;
import com.justin.energy.server.stream.dto.device.MeterUsageDto;
import com.justin.energy.server.stream.dto.device.RegisterDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
@EnableBinding(EnergyProcessor.class)
public class EnergyProcessorConfiguration {

  @Autowired
  private MongoTemplate mongoTemplate;

  @StreamListener(EnergyProcessor.SINK_INPUT)
  @NewSpan("sinkEnergyDataToDb")
  public void sink(final String gatewayUsages) {
    final Document doc = Document.parse(gatewayUsages);
    // Device sent as long in ms, now convert it to date object
    doc.replace("date", new Date(doc.getLong("date")));
    mongoTemplate.insert(doc, "energy");
  }

  @Splitter(inputChannel = EnergyProcessor.INPUT, outputChannel = EnergyProcessor.OUTPUT)
  @NewSpan("transformRawToEnergyData")
  public List<Map<String, Object>> transform(
      @SpanTag(key = "gatewayId", expression = "gatewayId") final GatewayUsageDto gatewayUsages) {
    final Date currentDate = new Date();
    return gatewayUsages.getMeterUsages().stream().map(this::parseMeterUsage).map(meterUsage -> {
      meterUsage.put("gatewayId", gatewayUsages.getGatewayId());
      // FIXME retrieve date from device.
      meterUsage.put("date", currentDate.getTime());
      return meterUsage;
    }).collect(Collectors.toList());
  }

  private RegisterDefinitionDto getRegisterDefinition(final int registerId) {
    // FIXME query on device management server, for now hard-coding
    final RegisterDefinitionDto definition = new RegisterDefinitionDto();
    definition.setRegisterId(registerId);
    definition.setValues(new ArrayList<>());

    if (registerId == 1) {
      definition.getValues().add(new RegisterValueDto("FLOAT32", "va"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "vb"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "vc"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "vavg"));
    } else if (registerId == 2) {
      definition.getValues().add(new RegisterValueDto("FLOAT32", "ca"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "cb"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "cc"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "cavg"));
    } else if (registerId == 3) {
      definition.getValues().add(new RegisterValueDto("FLOAT32", "pfa"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "pfb"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "pfc"));
      definition.getValues().add(new RegisterValueDto("FLOAT32", "pfavg"));
    } else if (registerId == 4) {
      definition.getValues().add(new RegisterValueDto("FLOAT32", "tap"));
    } else if (registerId == 5) {
      definition.getValues().add(new RegisterValueDto("FLOAT32", "tae"));
    } else {
      throw new IllegalArgumentException("Unsupported register id: " + registerId);
    }
    return definition;
  }

  private Map<String, Object> parseEnergyParameters(final RegisterDto meterUsage) {
    final int registerId = meterUsage.getRegisterId();
    final RegisterDefinitionDto registerDefinition = getRegisterDefinition(registerId);
    final int[] registerValues = meterUsage.getRegisterValues();

    final Map<String, Object> values = new HashMap<>();
    int parsingIndex = 0;
    for (final RegisterValueDto valueDto : registerDefinition.getValues()) {
      final MeterDataType dataType = MeterDataType.fromString(valueDto.getDataType());
      if (dataType == null) {
        // Stop the execution because it will impacted to the bytes order.
        throw new RuntimeException("Unsupported meter data type " + valueDto.getDataType());
      }
      values.put(valueDto.getSymbol(), dataType.getValue(registerValues, parsingIndex));
      parsingIndex += dataType.getWordCount();
    }
    return values;
  }

  private Map<String, Object> parseMeterUsage(final MeterUsageDto meterUsage) {
    final Map<String, Object> usages = new HashMap<>();
    usages.put("meterId", meterUsage.getMeterId());
    usages.putAll(meterUsage.getEnergyUsages()
                            .stream()
                            .map(this::parseEnergyParameters)
                            .reduce(new HashMap<String, Object>(), (param1, param2) -> {
                              param1.putAll(param2);
                              return param1;
                            }));
    return usages;
  }
}
