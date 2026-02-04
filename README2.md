# Learning-Orbit
### Command I ran to create the network
```
docker network create learning-network
```
----------------------------
```
PS C:\Users\priysh.rai\Desktop\Learning-Orbit-local> docker network create learning-network
fca4922eef06a4b32e0e9a2c29f358777aecd765a919954955f0f837582a448f
PS C:\Users\priysh.rai\Desktop\Learning-Orbit-local> 
```



### something to look into
### 1.

Here is the breakdown of why we are keeping it simple for now and the trade-offs between the two.

### The "Simple" Approach (Development Mode)

```Dockerfile
FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app
CMD ["tail", "-f", "/dev/null"]
```

Why we use it: Since you are in a "Practice" phase, you will be changing code every 5 minutes.How it works: This container stays alive like a virtual computer. You "enter" it, run mvn spring-boot:run, see the output, change your code in Windows (VS Code), and the container sees the change immediately because of the volumes mapping.The Maven Factor: Maven is already inside this image. You don't need to do anything extra; when you run mvn commands inside the container, it uses the Maven pre-installed in the maven:3.9.6 image.

### The "Multi-stage" Approach (Production Mode)

```Dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
...
FROM eclipse-temurin:21-jre
...
ENTRYPOINT ["java", "-jar", "app.jar"]
```

The "Problem" for learning: Every time you change a single line of code, you have to run docker compose up --build. This triggers a full re-compile and re-package of the JAR file. It is slow and frustrating when you are just trying to learn Redis Strings.Why it's great for DevOps: It creates a tiny, secure image containing only the "Run" environment (jre) and your JAR file, leaving the heavy build tools (maven) behind.


### My Recommendation

Stick with the Simple Approach for your SpringBoot_Implementation folders. It allows you to use the container as a "Java-enabled Terminal". Since you can't install Maven or Java on your laptop, this container is your development environment.Later, when you are ready to "deploy" your project to your AWS architecture (which I see you've worked on previously), that is when you should switch to the Multi-stage Dockerfile.