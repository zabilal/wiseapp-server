package com.nova.simplechat.simplechat;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.util.EC2MetadataUtils;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Raji Zakariyya
 * <p>
 * Handles events from the backend and emits events to the backend.
 */
public class EventVerticle extends AbstractVerticle {

    private static final Integer CONNECTOR_PORT = 5030;
    private Vertx vertx;
    private HttpClient client;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        connectToBackend();
    }

    // todo reconnect must be set so that operation may resume.
    private void connectToBackend() {
        client = vertx.createHttpClient();

        client.websocket(CONNECTOR_PORT, "localhost", "/", event -> {

            System.out.println("CONNECTED TO THE SERVER BRIDGE " );

            // listen for events from the backend connector service.
            event.handler(data -> {
                vertx.eventBus().send(Configuration.DOWNSTREAM, data);
            });

            // forward emitted events onto the connector.
            vertx.eventBus().consumer(Configuration.UPSTREAM, handler -> {
                vertx.eventBus().send(event.textHandlerID(), handler.body().toString());
            });

//            String chatServerIp =

            // register this server to the connector for events.
            vertx.eventBus().send(event.textHandlerID(),
                    Serializer.pack(new Register(Configuration.REGISTER_NAME, Configuration.LISTEN_PORT, getLocalIpAddress())));
        });


    }


    public static String getLocalIpAddress() {
//        This guy beloow returns the private IP of the server
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        }
        // Getting instance Id
        String instanceId = EC2MetadataUtils.getInstanceId();
//
//    // Getting EC2 private IP
//        String privateIP = EC2MetadataUtils.getInstanceInfo().getPrivateIp();
//
//    // Getting EC2 public IP
        AWSCredentialsProvider provider = new InstanceProfileCredentialsProvider();
        AmazonEC2 awsEC2client = AmazonEC2ClientBuilder.standard().withCredentials(provider).build();
        String publicIP = awsEC2client.describeInstances(new DescribeInstancesRequest()
            .withInstanceIds(instanceId))
            .getReservations()
            .stream()
            .map(Reservation::getInstances)
            .flatMap(List::stream)
            .findFirst()
            .map(Instance::getPublicIpAddress)
            .orElse(null);

        return publicIP;
//        return null;
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        client.close();
    }
}
