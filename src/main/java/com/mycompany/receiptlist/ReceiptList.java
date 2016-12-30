/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.receiptlist;

import com.rabbitmq.client.AMQP;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
/**
 *
 * @author Buhrkall
 */
public class ReceiptList {

    private final static String QUEUE_NAME = "RecipListQueue";
    private final static String EXCHANGE_NAME = "TranslatorExchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setVirtualHost("student");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        Channel listeningChannel = connection.createChannel();
        final Channel sendingChannel = connection.createChannel();

        listeningChannel.queueDeclare(QUEUE_NAME, false, false, false, null);
        sendingChannel.exchangeDeclare(EXCHANGE_NAME, "direct");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(listeningChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                String[] arr = message.split(",");

                for (int i = 0; i < arr.length; i++) {
                    switch (arr[i]) {
                        case "DreamTeamBankXML":
                            sendingChannel.basicPublish(EXCHANGE_NAME, "DreamTeamBankXML", null, message.getBytes());
                            break;
                        case "DreamTeamBankJSON":
                            sendingChannel.basicPublish(EXCHANGE_NAME, "DreamTeamBankJSON", null, message.getBytes());
                            break;
                    }
                }
            }
        };

        listeningChannel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
