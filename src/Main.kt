import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val isServer = args.contains("-s")  // server mode
    val hostname = if (args.contains("-h")) args[1 + args.indexOf("-h")] else "127.0.0.1"
    val port = if (args.contains("-p")) args[1 + args.indexOf("-p")].toInt() else 9999
    if (isServer) {
        val server = ServerSocket(port)
        println("Server is running on port ${server.localPort}")
        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")
            thread { Server(client).run() }
        }
    } else { // client mode
        val client = Client(hostname, port)
        client.run()
    }

}

class Client(address: String, port: Int) {
    private val connection: Socket = Socket(address, port)
    private var connected: Boolean = true
    private val reader: Scanner = Scanner(connection.getInputStream())
    private val writer: OutputStream = connection.getOutputStream()

    init {
        println("Connected to server at $address on port $port")
    }

    fun run() {
        thread { read() }
        while (true) {
            val input = readLine() ?: ""
            write(input)
        }

    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun read() {
        while (connected)
            try {
                println(reader.nextLine())
            } catch (ex: Exception) {
                connected = false
                reader.close()
                writer.close()
                connection.close()
                exitProcess(0)
            }
    }
}

class Server(_server: Socket) {
    private val server: Socket = _server
    private val reader: Scanner = Scanner(server.getInputStream())
    private val writer: OutputStream = server.getOutputStream()
    private val fibonachi: Fibonachi = Fibonachi()
    private var running: Boolean = false

    fun run() {
        running = true

        while (running) {
            try {
                val text = reader.nextLine()
                val values = text.split(' ')
                val result = fibonachi.get(values[0].toInt())
                write(result)
            } catch (ex: Exception) {
                shutdown()
            }
        }
    }

    private fun write(message: Long) {
        writer.write((message.toString() + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        server.close()
        println("${server.inetAddress.hostAddress} closed the connection")
    }

}

class Fibonachi {

    fun get(n: Int): Long {
        return fib(n)
    }

    fun fib(n: Int): Long  {
        if (n <= 1) return n.toLong()
        return fib(n - 1 ) + fib(n - 2 )
    }
}
