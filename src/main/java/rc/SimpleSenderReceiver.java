package rc;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Random;

public class SimpleSenderReceiver implements MessageListener {

    private static boolean runReceiver = true;
    private Connection connection;
    private Session sendSession;
    private Session receiveSession;
    private MessageProducer sender;
    private MessageConsumer receiver;
    private static Random randomGenerator = new Random();

    public SimpleSenderReceiver() throws Exception {
        // Configure JNDI environment
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, 
                   "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        env.put(Context.PROVIDER_URL, "servicebus.properties");
        Context context = new InitialContext(env);
        

        // Look up ConnectionFactory and Queue
        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
        Destination queue = (Destination) context.lookup("QUEUE");

        // Create Connection
        connection = cf.createConnection(context.lookup("SASPolicyName").toString(), context.lookup("SASPolicyKey").toString());

        System.out.println("connection :"+connection);

        // Create sender-side Session and MessageProducer
        sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        System.out.println("Session open.");

        sender = sendSession.createProducer(queue);
        System.out.println(sender.getDestination());
        System.out.println("sender:"+sender);

        if (runReceiver) {
            // Create receiver-side Session, MessageConsumer,and MessageListener
            receiveSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            receiver = receiveSession.createConsumer(queue);
            receiver.setMessageListener(this);
            connection.start();
        }
    }

    public static void main(String[] args) {
        try {

            if ((args.length > 0) && args[0].equalsIgnoreCase("sendonly")) {
                //runReceiver = false;
            }

            SimpleSenderReceiver simpleSenderReceiver = new SimpleSenderReceiver();
            System.out.println("Press [enter] to send a message. Type 'exit' + [enter] to quit.");
            BufferedReader commandLine = new java.io.BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.println("\n--- Enter a single word to send a message, or 'exit' to quit: ");
                String s = commandLine.readLine();
                if (s.equalsIgnoreCase("exit")) {
                    simpleSenderReceiver.close();
                    System.exit(0);
                } else {
                    simpleSenderReceiver.sendMessage(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String messageText) throws JMSException {
        TextMessage message = sendSession.createTextMessage();
        message.setText(messageText);
        long randomMessageID = randomGenerator.nextLong() >>>1;
        message.setStringProperty("TenantId", "klant");
        message.setStringProperty("EventType", "bericht");
        message.setStringProperty("EventTypeVersion", "1.0");
        message.setStringProperty("MessageType", "DocumentMessage");
        message.setStringProperty("OperationType", "Create");
        message.setStringProperty("SourceSystem", "sis_sender");
        message.setStringProperty("EnterpriseKey", "sis_sender-klant-bericht");
        message.setJMSMessageID("ID:" + randomMessageID);
        sender.send(message);
        System.out.println("Sent message with JMSMessageID = " + message.getJMSMessageID());
        System.out.println("Sent message with Text = " + message.getText());
    }

    public void close() throws JMSException {
        connection.close();
    }

    public void onMessage(Message message) {
        try {
            System.out.println(">>Received message with JMSMessageID = " + message.getJMSMessageID());
            TextMessage txtmessage = (TextMessage) message;
            System.out.println(">>Received message with Text = " + txtmessage.getText());
            message.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}  