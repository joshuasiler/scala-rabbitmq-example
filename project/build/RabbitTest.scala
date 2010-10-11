import sbt._

class RabbitTestProject(info: ProjectInfo) extends DefaultProject(info)
{
	override def compileOrder = CompileOrder.ScalaThenJava

	val tools = "com.rabbitmq" % "amqp-client" % "1.8.1"

//	val tools = "com.rabbitmq" % "rabbitmq-client" % "1.3.0"


}
