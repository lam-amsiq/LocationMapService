# LocationMapService

A location map service that uses Google Map to display large quantaties of annotations and group dense areas into heatmaps. 

The library gives the user an easy interface to put and take annotations into the map.

# Installation

To get the Git project into your build:

Step 1. Add the JitPack repository to your build file

Step 2. Add the dependency
- Gradle

1. Add JitPack in your root build.gradle at the end of repositories
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
2. Add the dependency in the app gradle
```gradle
dependencies {
  implementation 'com.github.lam-amsiq:LocationMapService:1.0'
}
```

- Maven

1. Add the JitPack repository to your build file
```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```
2. Add the dependency
```xml
<dependency>
    <groupId>com.github.lam-amsiq</groupId>
    <artifactId>LocationMapService</artifactId>
    <version>1.0</version>
</dependency>
```
- sbt

1. Add JitPack in your build.sbt at the end of resolvers
```sbt
resolvers += "jitpack" at "https://jitpack.io"
```
2. Add the dependency
```sbt
libraryDependencies += "com.github.lam-amsiq" % "LocationMapService" % "1.0"
```
- Leiningen

1. Add JitPack in your project.clj at the end of repositories
```
:repositories [["jitpack" "https://jitpack.io"]]
```
2. Add the dependency
```
:dependencies [[com.github.lam-amsiq/LocationMapService "1.0"]]
```
