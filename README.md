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
        JavaServer(
            address = "127.0.0.1",
            port = 25565,
            timeoutMs = 15000,
        ).use {
            it.connect()
            status = it.status()
        }
    }
}
```

### Build platform artifacts

#### Android aar

- Run `./gradlew :shared:assembleRelease`
- Output: `/shared/build/outputs/aar/shared-release.aar`

#### JVM jar

- Run `./gradlew :shared:jvmJar`
- Output: `/shared/build/libs/shared-jvm-1.0.jar`

#### iOS Framework

- Run `./gradlew :shared:linkReleaseFrameworkIosArm64`
- Output: `/shared/build/bin/iosArm64/releaseFramework/shared.framework`

#### macOS Framework

- Run `./gradlew :shared:linkReleaseFrameworkMacosArm64`
- Output: `/shared/build/bin/macosArm64/releaseFramework/shared.framework`

#### Linux static library

- Run `./gradlew :shared:linkReleaseStaticLinuxX64`
- Output: `/shared/build/bin/linuxX64/releaseStatic/libshared.a`
