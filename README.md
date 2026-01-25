# mcstatuskt

Simple Minecraft Server pinger\
Currently only with support for Java Edition servers

### Credits
Original project - https://github.com/py-mine/mcstatus \
Minecraft Java Edition Server Protocol - https://wiki.vg/Protocol

### Usage

```kotlin
import com.t895.mcstatuskt.JavaServer
import com.t895.mcstatuskt.Status

fun main() {
    lateinit var status: Status
    runBlocking {
        JavaServer.connect(
            address = "127.0.0.1",
            port = 25565,
            timeoutMs = 15000,
        ) { status = it.status() }
    }
}
```

### Build platform artifacts

- Run `./gradlew :shared:assemble`
