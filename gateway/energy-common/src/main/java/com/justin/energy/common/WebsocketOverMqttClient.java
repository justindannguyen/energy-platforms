/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.common.config.MqttConfiguration;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class WebsocketOverMqttClient implements MqttCallback, Runnable {

  public static enum QOS {
    AT_MOST_ONE, AT_LEAST_ONE, EXACTLY_ONE;
  }

  private static class SubscriberDefinition {
    private final List<SubscriptionDefinition> subscriptions = new ArrayList<>();
    private IMqttMessageListener handler;

    private void addSubcription(final String topic, final QOS qos) {
      if (topic == null || qos == null) {
        throw new IllegalArgumentException("Topic or qos is null");
      }

      for (final SubscriptionDefinition subcription : subscriptions) {
        if (subcription.topic.equals(topic)) {
          return;
        }
      }

      final SubscriptionDefinition subscription = new SubscriptionDefinition();
      subscription.topic = topic;
      subscription.qos = qos;
      subscriptions.add(subscription);
    }
  }

  private static class SubscriptionDefinition {
    private String topic;
    private QOS qos;
  }

  private final MqttClient mqttClient;
  private final MqttConnectOptions mqttConnectOpts;
  private final List<SubscriberDefinition> subscribers = new ArrayList<>();
  private Exception lastError;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private transient boolean running = true;
  private final Thread watchDog;

  public WebsocketOverMqttClient(final String clientId, final MqttConfiguration runConfiguration)
      throws MqttException {
    this.mqttConnectOpts = new MqttConnectOptions();
    this.mqttConnectOpts.setAutomaticReconnect(false);
    this.mqttConnectOpts.setCleanSession(false);
    final String password = runConfiguration.getBrokerPassword();
    if (password != null && password.trim().length() != 0) {
      this.mqttConnectOpts.setPassword(password.toCharArray());
    }
    final String username = runConfiguration.getBrokerUsername();
    if (username != null && username.trim().length() != 0) {
      this.mqttConnectOpts.setUserName(username);
    }

    this.mqttClient = new MqttClient(runConfiguration.getBrokerUrl(), clientId);
    this.mqttClient.setCallback(this);

    // Watch dog is not required if mqtt is auto reconnect
    watchDog = new Thread(this);
    watchDog.setDaemon(true);
    watchDog.setName("MQTT Reconnection Thread");
  }

  /**
   * Asynchronous connect to mqtt server, retries until success.
   */
  public void connect() {
    running = true;
    watchDog.start();
  }

  @Override
  public void connectionLost(final Throwable exception) {
    Logger.error(exception, "Connection lost with broker");
    synchronized (watchDog) {
      watchDog.notify();
    }
  }

  @Override
  public void deliveryComplete(final IMqttDeliveryToken token) {}

  public void disconnect() throws MqttException, InterruptedException {
    running = false;
    subscribers.clear();
    mqttClient.disconnect();
    if (watchDog != null && watchDog.isAlive()) {
      watchDog.interrupt();
      watchDog.join();
    }
    Logger.info("Cloud client terminated!");
  }

  public Exception getLastError() {
    return lastError;
  }

  @Override
  public void messageArrived(final String topic, final MqttMessage message) throws Exception {}

  public boolean publish(final String topic, final QOS qos, final Object message) {
    try {
      return publish(topic, qos.ordinal(), objectMapper.writeValueAsBytes(message));
    } catch (final JsonProcessingException | MqttException ex) {
      lastError = ex;
      Logger.error(ex);
    }
    return false;
  }

  @Override
  public void run() {
    int retryTimes = 0;
    while (running) {
      synchronized (watchDog) {
        while (mqttClient.isConnected()) {
          try {
            watchDog.wait();
          } catch (final InterruptedException ex) {
            break;
          }
        }
      }

      try {
        mqttClient.connect(mqttConnectOpts);
        for (final SubscriberDefinition subscriber : subscribers) {
          for (final SubscriptionDefinition subscription : subscriber.subscriptions) {
            mqttClient.subscribe(subscription.topic, subscription.qos.ordinal(),
                subscriber.handler);
          }
        }
        lastError = null;
        retryTimes = 0;
        Logger.info("Connected to MQTT broker");
      } catch (final Exception ex) {
        lastError = ex;
        retryTimes++;
        try {
          Logger.error("Fail when connect to MQTT broker, rety after {} seconds. Detail: {}",
              retryTimes, ex.getMessage());
          TimeUnit.SECONDS.sleep(retryTimes);
        } catch (final InterruptedException ex1) {
          break;
        }
      }
    }
  }

  /**
   * Subscribe to broker topic if connection is connected. If client is disconnected from server
   * then it will be subscribed later.
   */
  public void subscribe(final String topic, final QOS qos, final IMqttMessageListener handler)
      throws MqttException {
    SubscriberDefinition subscriber =
        subscribers.stream().filter((def) -> def.handler == handler).findFirst().orElse(null);
    if (subscriber == null) {
      subscriber = new SubscriberDefinition();
      subscriber.handler = handler;
      subscribers.add(subscriber);
    }
    subscriber.addSubcription(topic, qos);

    if (mqttClient.isConnected()) {
      mqttClient.subscribe(topic, qos.ordinal(), handler);
    }
  }

  /**
   * Publish the message to server only when connection is connected, otherwise drop the message.
   */
  private boolean publish(final String topic, final int qos, final byte[] payload)
      throws MqttPersistenceException, MqttException {
    boolean success = false;
    if (mqttClient.isConnected()) {
      mqttClient.publish(topic, payload, qos, false);
      success = true;
    }
    return success;
  }
}
