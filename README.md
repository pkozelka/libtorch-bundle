# libtorch-bundle

This library wraps libtorch-jar from the [PyTorch project](https://pytorch.org/get-started/locally/) so that the
consuming application does not have to care about its underlying JNI details.

The caller is only responsible for calling `LibtorchBundle.init();` early enough - prior using any libtorch functionality.
Then wrapper takes care of finding a temporary directory, expanding all necessary native libraries, loading them and cleaning
them once JVM exits.

## Build

This project uses Maven for its build.

By default, it builds only platform artifact for the current platform.

To build all supported platform artifacts, set flag `bundle_all`:

```shell
mvn clean install -Dbundle_all
```

This is also the option used when releasing the repository.

## Output artifacts

The build produces the library itself, plus one artifact per each supported platform. One or more of platform artifacts
need to be added to java classpath in order to have that platform supported.

For example, if you want to support Linux and Mac in your application, your Maven dependencies should contain something like this:

```xml
<dependencies>
    <dependency>
        <groupId>net.kozelka.ai</groupId>
        <artifactId>libtorch-bundle</artifactId>
        <version>${libtorch.version}</version>
    </dependency>
    <dependency>
        <groupId>net.kozelka.ai</groupId>
        <artifactId>libtorch-bundle</artifactId>
        <version>${libtorch.version}</version>
        <classifier>linux_64</classifier>
    </dependency>
    <dependency>
        <groupId>net.kozelka.ai</groupId>
        <artifactId>libtorch-bundle</artifactId>
        <version>${libtorch.version}</version>
        <classifier>osx_64</classifier>
    </dependency>
</dependencies>
```

For Java code example, check out [tests in this repository](https://github.com/pkozelka/libtorch-bundle/tree/main/src/test/java/net/kozelka/libtorch).

## Credits

Most of this logic is handled by existing opensource libraries:
- scijava's [native-lib-loader](https://github.com/scijava/native-lib-loader)
- facebook's [SoLoader](https://github.com/facebook/soloader) and [fbjni](https://github.com/facebookincubator/fbjni)

