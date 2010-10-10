package rabbittest
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

object RabbitTest extends Application
{
    println("start")
    var channel = getAMQPChannel("test", "/")
    
    // Opens up a connection to RabbitMQ, retrying every five seconds
  // if the queue server is unavailable.
  def getAMQPChannel(queue: String, vhost: String) : Channel = {
    var attempts = 0
    var channel: Channel = null
    var connection: Connection = null
    var consumer: QueueingConsumer = null
    
    println("Opening connection to AMQP " + vhost + " "  + queue + "...")

    while (true) {
      attempts += 1
      println("Attempt #" + attempts)

      try {
        connection = getConnection(queue, "localhost", 5768, "guest", "guest",vhost)
        channel = connection.createChannel()
        consumer = new QueueingConsumer(channel)
        channel.exchangeDeclare(queue, "direct", true)
        channel.queueDeclare(queue, true, false, false, null)
        channel.queueBind(queue, queue, queue)
        channel.basicConsume(queue, false, consumer)
        println("Connected to RabbitMQ")
        channel
      } catch {
        case ex: Exception => {
          println("Cannot connect to AMQP. Retrying in 5 sec.", ex)
          Thread.sleep(1000 * 5)
        }
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
