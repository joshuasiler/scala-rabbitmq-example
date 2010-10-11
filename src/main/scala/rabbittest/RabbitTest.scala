package rabbittest
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

object RabbitTest extends Application
{
    println("start")
    var channel = getAMQPChannel("test", "/")
    var consumer: QueueingConsumer = null
    while (true) {
      var task: QueueingConsumer.Delivery = null
      try { task = consumer.nextDelivery() }
      catch {
        case ex: Exception => {
          println("Error in AMQP connection: reconnecting.")//, ex)
		  Thread.sleep(1000)
          channel = getAMQPChannel("test", "/")
        }
      }
	
	  if (task != null && task.getBody() != null) {
        println(new String(task.getBody()))
        try { channel.basicAck(task.getEnvelope().getDeliveryTag(), false) }
        catch {
          case ex: Exception => { println("Error ack'ing message.", ex) }
        }
      }
	}
	
    // Opens up a connection to RabbitMQ, retrying every five seconds
  // if the queue server is unavailable.
  def getAMQPChannel(queue: String, vhost: String) : Channel = {
    var attempts = 0
    var channel: Channel = null
    var connection: Connection = null
    
    println("Opening connection to AMQP " + vhost + " "  + queue + "...")
      try {
        connection = getConnection(queue, "localhost", 5672, "guest", "guest",vhost)
        channel = connection.createChannel()
        consumer = new QueueingConsumer(channel)
        channel.exchangeDeclare(queue, "direct", true)
        channel.queueDeclare(queue, true, false, false, null)
        channel.queueBind(queue, queue, queue)
        channel.basicConsume(queue, false, consumer)
        println("Connected to RabbitMQ")
      } catch {
        case ex: Exception => {
          println(".....cannot connect to AMQP. ")//, ex)
       } 
    }
    channel
  }
  
  // Returns a new connection to an AMQP queue.
  def getConnection(queue: String, host: String, port: Int, username: String, password: String, vhost: String): Connection = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setPort(port)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(vhost)
    factory.newConnection()
  }
}
