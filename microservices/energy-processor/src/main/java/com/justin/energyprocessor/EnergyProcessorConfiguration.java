/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energyprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

import com.justin.energyprocessor.dto.cloud.RegisterDefinitionDto;
import com.justin.energyprocessor.dto.cloud.RegisterValueDto;
import com.justin.energyprocessor.dto.device.GatewayUsageDto;
import com.justin.energyprocessor.dto.device.MeterUsageDto;
import com.justin.energyprocessor.dto.device.RegistryDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
@EnableBinding(Processor.class)
public class EnergyProcessorConfiguration {
  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(EnergyProcessorConfiguration.class);

  @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
  public Map<String, Object> transform(final GatewayUsageDto gatewayUsages) {
    log.info("Receive the energy raw data to transform, gateway {}", gatewayUsages.getGatewayId());
    final Map<String, Object> usages = new HashMap<>();
    usages.put("gatewayId", gatewayUsages.getGatewayId());
    usages.put("energyUsages", gatewayUsages.getMeterUsages().stream().map(this::parseMeterUsage)
        .collect(Collectors.toList()));

    return usages;
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

  private Map<String, Object> parseEnergyParameters(final RegistryDto meterUsage) {
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
    usages.putAll(meterUsage.getEnergyUsages().stream().map(this::parseEnergyParameters)
        .reduce(new HashMap<String, Object>(), (param1, param2) -> {
          param1.putAll(param2);
          return param1;
        }));
    return usages;
  }
}
